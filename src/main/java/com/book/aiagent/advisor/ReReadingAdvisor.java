package com.book.aiagent.advisor;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;

public class ReReadingAdvisor implements BaseAdvisor {

    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        String userText = chatClientRequest.prompt().getUserMessage().getText();
        String rewrittenUserText = """
                %s
                Read the question again: %s
                """.formatted(userText, userText);

        return chatClientRequest.mutate()
                .prompt(chatClientRequest.prompt().augmentUserMessage(rewrittenUserText))
                .build();
    }

    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        return chatClientResponse;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public String toString() {
        return ReReadingAdvisor.class.getSimpleName();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Builder() {
        }

        public ReReadingAdvisor build() {
            return new ReReadingAdvisor();
        }
    }
}
