package com.book.aiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.book.aiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Data
@Slf4j
public abstract class BaseAgent {
    private String name;

    private String systemPrompt;
    private String nextStepPrompt;

    private AgentState state = AgentState.IDLE;

    private int currentStep = 0;
    private int maxSteps = 10;

    private ChatClient chatClient;

    // Memery of the agent, can be used for context
    private List<Message> messageList = new ArrayList<>();

    public SseEmitter runStream(String userPrompt) {
        SseEmitter sseEmitter = new SseEmitter(300000L); // 5 minutes timeout
        // Use thread to do the async to prevent blocking the main thread
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    sseEmitter.send("Error: cannot execute the agent from this state" + this.state);
                    sseEmitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    sseEmitter.send("Error: user prompt cannot be empty");
                    sseEmitter.complete();
                    return;
                }
            } catch (Exception e) {
                sseEmitter.completeWithError(e);
            }

            state = AgentState.RUNNING;
            messageList.add(new UserMessage(userPrompt));
            List<String> results = new ArrayList<>();
            try {
                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("Executing step " + stepNumber + "/" + maxSteps);
                    String stepResult = step();
                    String result = "Step " + stepNumber + ": " + stepResult;
                    results.add(result);
                    sseEmitter.send(result);
                }

                if (currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    results.add("Terminated: Reached max steps (" + maxSteps + ")");
                    sseEmitter.send("Terminated: Reached max steps (" + maxSteps + ")");
                }
                sseEmitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("Error executing agent", e);
                try {
                    sseEmitter.send("Error executing agent: " + e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            } finally {
                // 清理资源
                this.cleanup();
            }
        });

        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timeout");
        });

        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });

        return sseEmitter;
    }

    public abstract String step();

    public void cleanup() {
    }

}
