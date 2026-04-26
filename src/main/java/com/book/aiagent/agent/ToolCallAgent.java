package com.book.aiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.book.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;


@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {
    private final ToolCallback[] availableTools;
    private ChatResponse toolCallChatResponse;
    private ToolCallingManager toolCallingManager;
    private String finalResponse;

    private final ChatOptions chatOptions;


    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // Disable Spring AI internal tool execution and handle tool calls with a custom manager.
        this.chatOptions = OpenAiChatOptions.builder().internalToolExecutionEnabled(false).build();
    }

    @Override
    public boolean think() {
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            ChatResponse chatResponse = getChatClient()
                    .prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();

            this.toolCallChatResponse = chatResponse;
            if (chatResponse.getResult() == null || chatResponse.getResult().getOutput() == null) {
                log.warn(getName() + " received an empty chat response result");
                return false;
            }
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            String result = assistantMessage.getText();
            log.info(getName() + " thought: " + result);
            log.info(getName() + " tool calls: " + toolCallList);
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("Tool name: %s. param: %s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            if (StrUtil.isNotBlank(toolCallInfo)) {
                log.info(getName() + " tool call details: \n" + toolCallInfo);
            }

            if (toolCallList.isEmpty()) {
                getMessageList().add(assistantMessage);
                this.finalResponse = StrUtil.blankToDefault(result, "Done.");
                setState(AgentState.FINISHED);
                return false;
            } else {
                this.finalResponse = result;
                return true;
            }
        } catch (Exception e) {
            log.error("Error during thinking process" + e.getMessage());
            getMessageList().add(new AssistantMessage("Error during thinking process: " + e.getMessage()));
            return false;
        }
    }

    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "No tool calls to execute.";
        }
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> response.name().equals("doTerminate"));
        if (terminateToolCalled && StrUtil.isNotBlank(finalResponse)) {
            setState(AgentState.FINISHED);
            return finalResponse;
        }
        if (terminateToolCalled && toolResponseMessage.getResponses().size() == 1) {
            setState(AgentState.FINISHED);
            return "Done.";
        }
        String results = toolResponseMessage.getResponses().stream()
                .filter(response -> !response.name().equals("doTerminate"))
                .map(response -> "Tool" + response.name() + " returned result：" + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);
        return results;
    }

    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return StrUtil.blankToDefault(finalResponse, "Done.");
            }
            return act();
        } catch (Exception e) {
            log.error("Step execution failed", e);
            return "Step execution failed：" + e.getMessage();
        }
    }
}
