package com.book.aiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileBasedChatMemory implements ChatMemory {

    private final String baseDir;

    private static final Kryo KRYO = new Kryo();

    static {
        KRYO.setRegistrationRequired(false);
        KRYO.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    public FileBasedChatMemory(String dir) {
        this.baseDir = dir;
        File directory = new File(dir);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Failed to create chat memory directory: " + dir);
        }
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> conversationMessages = getOrCreateConversation(conversationId);
        conversationMessages.addAll(messages);
        saveConversation(conversationId, conversationMessages);
    }

    @Override
    public List<Message> get(String conversationId) {
        return getOrCreateConversation(conversationId);
    }

    public List<Message> get(String conversationId, int lastN) {
        List<Message> allMessages = getOrCreateConversation(conversationId);
        return allMessages.stream()
                .skip(Math.max(0, allMessages.size() - lastN))
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists() && !file.delete()) {
            throw new IllegalStateException("Failed to delete chat memory file: " + file.getAbsolutePath());
        }
    }

    @SuppressWarnings("unchecked")
    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (Input input = new Input(new FileInputStream(file))) {
            return KRYO.readObject(input, ArrayList.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read chat memory for conversation: " + conversationId, e);
        }
    }

    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            KRYO.writeObject(output, new ArrayList<>(messages));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to save chat memory for conversation: " + conversationId, e);
        }
    }

    private File getConversationFile(String conversationId) {
        return new File(this.baseDir, conversationId + ".kryo");
    }
}
