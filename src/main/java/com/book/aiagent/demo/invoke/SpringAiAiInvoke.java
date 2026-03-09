package com.book.aiagent.demo.invoke;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SpringAiAiInvoke implements CommandLineRunner {
    // 1. Make it final. This prevents the model from being accidentally modified.
    private final ChatModel chatModel;

    // 2. Constructor Injection. Spring automatically finds the active ChatModel (OpenAI)
    public SpringAiAiInvoke(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- Testing Universal ChatModel ---");

        // 3. Call the model just like before
        AssistantMessage output = chatModel.call(new Prompt("Hi, how are you?"))
                .getResult()
                .getOutput();

        System.out.println(output.getText());
    }
}