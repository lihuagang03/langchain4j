package dev.langchain4j.store.memory.chat;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.memory.ChatMemory;

import java.util.List;

/**
 * 聊天记忆存储
 * 表示用于聊天记忆状态的存储。
 * 允许在存储聊天记忆的位置和方式上具有灵活性。
 * Represents a store for the {@link ChatMemory} state.
 * Allows for flexibility in terms of where and how chat memory is stored.
 * <br>
 * <br>
 * 目前，唯一可用的实现是 InMemoryChatMemoryStore。
 * 随着时间的推移，将会为流行的存储类型（如 SQL 数据库、文档存储等）添加开箱即用的实现。
 * 与此同时，您可以实现该接口以连接到任何您选择的存储。
 * Currently, the only implementation available is {@link InMemoryChatMemoryStore}.
 * Over time, out-of-the-box implementations will be added for popular stores like SQL databases, document stores, etc.
 * In the meantime, you can implement this interface to connect to any storage of your choice.
 * <br>
 * <br>
 * More documentation can be found <a href="https://docs.langchain4j.dev/tutorials/chat-memory">here</a>.
 */
public interface ChatMemoryStore {

    /**
     * 检索指定聊天记录的消息。
     * Retrieves messages for a specified chat memory.
     *
     * @param memoryId The ID of the chat memory. 聊天记忆ID
     * @return List of messages for the specified chat memory. Must not be null. Can be deserialized from JSON using {@link ChatMessageDeserializer}.
     */
    List<ChatMessage> getMessages(Object memoryId);

    /**
     * 更新指定聊天记录的消息。
     * Updates messages for a specified chat memory.
     *
     * @param memoryId The ID of the chat memory.
     * @param messages List of messages for the specified chat memory, that represent the current state of the {@link ChatMemory}.
     *                 Can be serialized to JSON using {@link ChatMessageSerializer}.
     */
    void updateMessages(Object memoryId, List<ChatMessage> messages);

    /**
     * Deletes all messages for a specified chat memory.
     *
     * @param memoryId The ID of the chat memory.
     */
    void deleteMessages(Object memoryId);
}
