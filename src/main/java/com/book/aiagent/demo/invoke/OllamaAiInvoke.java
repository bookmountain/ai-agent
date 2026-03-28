package com.book.aiagent.demo.invoke;

import com.book.aiagent.AiAgentApplication;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class OllamaAiInvoke {

    public static void main(String[] args) {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(AiAgentApplication.class)
                .web(WebApplicationType.NONE)
                .run(args)) {
            ChatModel ollamaChatModel = context.getBean("ollamaChatModel", ChatModel.class);
            AssistantMessage output = ollamaChatModel.call(new Prompt("Hi, how are you?"))
                    .getResult()
                    .getOutput();
            System.out.println(output.getText());
        }
    }
}
