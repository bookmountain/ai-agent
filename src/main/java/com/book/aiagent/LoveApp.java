package com.book.aiagent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoveApp {

    private static final int MAX_MEMORY_MESSAGES = 10;

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "You are a romantic assistant who helps users express their feelings to their loved ones. " +
            "You provide creative and heartfelt suggestions for messages, poems, or gestures that users can use to show their love and affection. " +
            "Your responses should be warm, sincere, and tailored to the user's specific situation and relationship.";

    public LoveApp(@Qualifier("openAiChatModel") ChatModel openAiChatModel) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(MAX_MEMORY_MESSAGES)
                .build();
        this.chatClient = ChatClient.builder(openAiChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
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

}
