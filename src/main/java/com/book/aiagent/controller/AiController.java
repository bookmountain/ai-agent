package com.book.aiagent.controller;

import com.book.aiagent.LoveApp;
import com.book.aiagent.agent.OpenManus;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final LoveApp loveApp;

    private final ToolCallback[] allTools;

    private final ChatModel openAiChatModel;

    public AiController(
            LoveApp loveApp,
            ToolCallback[] allTools,
            @Qualifier("openAiChatModel") ChatModel openAiChatModel
    ) {
        this.loveApp = loveApp;
        this.allTools = allTools;
        this.openAiChatModel = openAiChatModel;
    }

    @GetMapping("/love_app/chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    // SSE
    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId);
    }

    // SSE
    @GetMapping(value = "/love_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId).map(chunk -> ServerSentEvent.<String>builder().data(chunk).build());
    }

    // SSE
    @GetMapping(value = "/love_app/chat/sse_emitter")
    public SseEmitter doChatWithLoveAppServerSseEmitter(String message, String chatId) {
        SseEmitter sseEmitter = new SseEmitter(180000L);
        loveApp.doChatByStream(message, chatId).subscribe(chunk -> {
            try {
                sseEmitter.send(chunk);
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
        }, sseEmitter::completeWithError, sseEmitter::complete);

        return sseEmitter;
    }


    @GetMapping("/manus/chat")
    public SseEmitter doChatWithManus(String message) {
        OpenManus openManus = new OpenManus(allTools, openAiChatModel);
        return openManus.runStream(message);
    }

}
