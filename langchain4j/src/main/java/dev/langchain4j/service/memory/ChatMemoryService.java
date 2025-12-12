package dev.langchain4j.service.memory;

import dev.langchain4j.Internal;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * 聊天记忆服务
 */
@Internal
public class ChatMemoryService {

    /**
     * 默认的聊天记忆ID
     */
    public static final String DEFAULT = "default";

    /**
     * 默认的聊天记忆
     */
    private ChatMemory defaultChatMemory;
    /**
     * 聊天记忆ID到聊天记忆的映射表
     */
    private Map<Object, ChatMemory> chatMemories;
    /**
     * 聊天记忆提供者
     */
    private ChatMemoryProvider chatMemoryProvider;

    public ChatMemoryService(ChatMemoryProvider chatMemoryProvider) {
        this.chatMemories = new ConcurrentHashMap<>();
        this.chatMemoryProvider = ensureNotNull(chatMemoryProvider, "chatMemoryProvider");
    }

    public ChatMemoryService(ChatMemory chatMemory) {
        defaultChatMemory = ensureNotNull(chatMemory, "chatMemory");
    }

    public ChatMemory getOrCreateChatMemory(Object memoryId) {
        // 默认的聊天记忆ID
        if (memoryId == DEFAULT) {
            // 默认的聊天记忆
            if (defaultChatMemory == null) {
                defaultChatMemory = chatMemoryProvider.get(DEFAULT);
            }
            return defaultChatMemory;
        }
        return chatMemories.computeIfAbsent(memoryId, chatMemoryProvider::get);
    }

    public ChatMemory getChatMemory(Object memoryId) {
        return memoryId == DEFAULT ? defaultChatMemory : chatMemories.get(memoryId);
    }

    public ChatMemory evictChatMemory(Object memoryId) {
        return chatMemories.remove(memoryId);
    }

    public void clearAll() {
        chatMemories.values().forEach(ChatMemory::clear);
        chatMemories.clear();
    }

    public Collection<Object> getChatMemoryIDs() {
        return chatMemories.keySet();
    }

    public Collection<ChatMemory> getChatMemories() {
        return chatMemories.values();
    }
}
