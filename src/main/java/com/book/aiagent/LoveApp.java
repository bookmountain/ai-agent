package com.book.aiagent;

import com.book.aiagent.advisor.MyLoggerAdvisor;
import com.book.aiagent.chatmemory.FileBasedChatMemory;
import com.book.aiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.book.aiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class LoveApp {

    private static final int MAX_MEMORY_MESSAGES = 10;

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "You are a romantic assistant who helps users express their feelings to their loved ones. " +
            "You provide creative and heartfelt suggestions for messages, poems, or gestures that users can use to show their love and affection. " +
            "Your responses should be warm, sincere, and tailored to the user's specific situation and relationship.";

    public LoveApp(@Qualifier("openAiChatModel") ChatModel openAiChatModel) {
        String fileDir = System.getProperty("user.dir") + "/tmp/ch at-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
//        ChatMemory chatMemory = MessageWindowChatMemory.builder()
//                .maxMessages(MAX_MEMORY_MESSAGES)
//                .build();
        this.chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        MyLoggerAdvisor.builder().build()
                )
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse response = this.chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        assert response != null;
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions) {
    }

    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = this.chatClient
                .prompt()
                .system(SYSTEM_PROMPT
                        + " Generate a love report for each response. "
                        + "Return a title and a list of actionable suggestions.")
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

//    @Resource
//    private VectorStore pgVectorVectorStore;

    @Resource
    private VectorStore loveAppVectorStore;

//    @Resource
//    private Advisor loveAppRagCloudAdvisor;

    @Resource
    private QueryRewriter queryRewriter;


    public String doChatWithRag(String message, String chatId) {
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient.prompt().user(rewrittenMessage).advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
//                .advisors(loveAppRagCloudAdvisor)
//                .advisors(QuestionAnswerAdvisor.builder(pgVectorVectorStore).build())
//                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())
                .advisors(
                        LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomnAdvisor(loveAppVectorStore, "single")
                )
                .call()
                .chatResponse();

        assert chatResponse != null;
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt().user(message).advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();

        assert chatResponse != null;
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }
}
