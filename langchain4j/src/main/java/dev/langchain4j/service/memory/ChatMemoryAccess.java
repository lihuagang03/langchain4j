package dev.langchain4j.service.memory;

import dev.langchain4j.memory.ChatMemory;

/**
 * 聊天记忆访问
 * 允许访问任何扩展其功能的 AI 服务的聊天记忆。
 * Allow to access the {@link ChatMemory} of any AI service extending it.
 */
public interface ChatMemoryAccess {

    /**
     * 返回此 AI 服务中具有给定 ID 的聊天记忆，如果该记忆不存在，则返回 null。
     * Returns the {@link ChatMemory} with the given id for this AI service or null if such memory doesn't exist.
     *
     * @param memoryId The id of the chat memory.
     * @return The {@link ChatMemory} with the given memoryId or null if such memory doesn't exist.
     */
    ChatMemory getChatMemory(Object memoryId);

    /**
     * 驱逐具有指定 ID 的聊天记录。
     * Evicts the {@link ChatMemory} with the given id.
     *
     * @param memoryId The id of the chat memory to be evicted.
     * @return true if {@link ChatMemory} with the given id existed, and it was successfully evicted, false otherwise.
     */
    boolean evictChatMemory(Object memoryId);
}
