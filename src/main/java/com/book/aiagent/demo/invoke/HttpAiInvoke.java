package com.book.aiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil; // Used to easily parse the final answer

public class HttpAiInvoke {
    public static void main(String[] args) {
        // 1. Use the INTERNATIONAL OpenAI-compatible endpoint
        String url = DashScopeIntlUrl.URL;

        String apiKey = TestApiKey.API_KEY;

        // 2. Build the JSON request body using Hutool's modern .set() method
        JSONObject requestBody = new JSONObject();
        requestBody.set("model", "qwen3.5-plus");

        JSONArray messages = new JSONArray();

        JSONObject systemMessage = new JSONObject();
        systemMessage.set("role", "system");
        systemMessage.set("content", "You are a helpful assistant.");
        messages.add(systemMessage);

        JSONObject userMessage = new JSONObject();
        userMessage.set("role", "user");
        userMessage.set("content", "Who are you?"); // Translated to English
        messages.add(userMessage);

        requestBody.set("messages", messages);

        // 3. Send the HTTP POST request
        // Using .header() directly avoids the deprecated addHeaders(Map) method
        // Using try-with-resources ensures the HTTP connection is closed properly
        try (HttpResponse response = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(requestBody.toString())
                .execute()) {

            // 4. Handle the response
            if (response.isOk()) {
                System.out.println("Request successful. Raw JSON response:");
                String responseBody = response.body();

                // Pretty-print the full JSON response
                System.out.println(JSONUtil.formatJsonStr(responseBody));

                // 5. Extract just the AI's text reply from the JSON tree
                String aiReply = JSONUtil.parseObj(responseBody)
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getStr("content");

                System.out.println("\n--- Extracted AI Reply ---");
                System.out.println(aiReply);

            } else {
                System.err.println("Request failed. Status code: " + response.getStatus());
                System.err.println("Error details: " + response.body());
            }
        } catch (Exception e) {
            System.err.println("An error occurred during the HTTP call: " + e.getMessage());
        }
    }
}