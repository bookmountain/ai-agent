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
}
