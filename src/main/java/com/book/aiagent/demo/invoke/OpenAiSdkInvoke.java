package com.book.aiagent.demo.invoke;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;

public class OpenAiSdkInvoke {

    public static void main(String[] args) {
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(TestApiKey.API_KEY)
                .baseUrl(DashScopeIntlUrl.URL)
                .build();

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.of("qwen3.5-plus"))
                .addMessage(ChatCompletionMessageParam.ofSystem(
                        ChatCompletionSystemMessageParam.builder()
                                .content("You are a helpful assistant.")
                                .build()
                ))
                .addUserMessage("Who are you？")
                .build();

        try {
            System.out.println("Sending request to Alibaba via OpenAI SDK...");
            ChatCompletion response = client.chat().completions().create(params);

            String aiReply = response.choices().get(0).message().content().orElse("No response");
            
            System.out.println("\n--- AI Reply ---");
            System.out.println(aiReply);
            
        } catch (Exception e) {
            System.err.println("API Call failed: " + e.getMessage());
        }
        
        System.exit(0);
    }
}