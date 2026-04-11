package com.book.aiagent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@SpringBootTest
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void doChat() {
        String chatId = UUID.randomUUID().toString();

        String message = "Hi, my name is Book. I have been dating my partner for two years.";
        String answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

        message = "I want to make my partner feel more loved, but I am not very good at expressing affection. Give me three thoughtful ideas for this weekend.";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);

        message = "What is my name, and based on what I told you, what is one small romantic thing I could do today?";
        answer = loveApp.doChat(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "Hi, my name is Book. I have been dating my partner for two years.";
        LoveApp.LoveReport loveReport = loveApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(loveReport);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "I've been in marriage for 10 years. but we have conflict very often, how can I solve that?";
        String answer = loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(answer);
    }

    @Test
    void doChatWithTools() {
        // Test web search answers
        testMessage("I want to take my girlfriend on a date in Shanghai this weekend. Recommend a few niche spots for couples.");

        // Test web crawling: relationship conflict case analysis
        testMessage("I recently argued with my partner. Check how other couples on Reddit  resolved similar conflicts.");

        // Test resource download: image download
        testMessage("Download a starry-sky couple wallpaper image directly to a file.");

        // Test terminal operation: execute code
        testMessage("Run a Python3 script to generate a data analysis report.");

        // Test file operation: save user profile
        testMessage("Save my relationship profile to a file.");

        // Test PDF generation
        testMessage("Generate a 'Qixi Date Plan' PDF including restaurant booking, activity schedule, and gift checklist.");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = loveApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }

}
