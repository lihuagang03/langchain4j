package dev.langchain4j.store.memory.chat;

import dev.langchain4j.data.message.ChatMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天记忆内存存储
 * ChatMemoryStore 的实现，用于将 ChatMemory（聊天消息）的状态存储在内存中。
 * Implementation of {@link ChatMemoryStore} that stores state of {@link dev.langchain4j.memory.ChatMemory} (chat messages) in-memory.
 * <p>
 * 这种存储机制是临时的，无法在应用重启后保留数据。
 * This storage mechanism is transient and does not persist data across application restarts.
 */
public class InMemoryChatMemoryStore implements ChatMemoryStore {

    private final Map<Object, List<ChatMessage>> messagesByMemoryId = new ConcurrentHashMap<>();

    /**
     * Constructs a new {@link InMemoryChatMemoryStore}.
     */
    public InMemoryChatMemoryStore() {}

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        return messagesByMemoryId.computeIfAbsent(memoryId, ignored -> new ArrayList<>());
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        messagesByMemoryId.put(memoryId, messages);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        messagesByMemoryId.remove(memoryId);
    }
}
