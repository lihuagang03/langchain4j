package dev.langchain4j.memory.chat;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.service.MemoryId;

/**
 * 聊天记忆提供者
 * 提供 ChatMemory 的实例。
 * 用于与 AiServices 一起使用。
 * Provides instances of {@link ChatMemory}.
 * Intended to be used with {@link dev.langchain4j.service.AiServices}.
 */
@FunctionalInterface
public interface ChatMemoryProvider {

    /**
     * 提供一个 ChatMemory 实例。
     * 每当调用 AI 服务方法（其参数用 @MemoryId 注解）并使用之前未见过的聊天记忆 ID 时，都会调用此方法。
     * 一旦返回 ChatMemory 实例，它将被保留在内存中，并由 AiServices 管理。
     * Provides an instance of {@link ChatMemory}.
     * This method is called each time an AI Service method (having a parameter annotated with {@link MemoryId})
     * is called with a previously unseen memory ID.
     * Once the {@link ChatMemory} instance is returned, it's retained in memory and managed by {@link dev.langchain4j.service.AiServices}.
     *
     * @param memoryId The ID of the chat memory.
     *                 聊天记忆ID
     * @return A {@link ChatMemory} instance.
     * @see MemoryId
     */
    ChatMemory get(Object memoryId);
}
