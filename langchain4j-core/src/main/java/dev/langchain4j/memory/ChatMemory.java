package dev.langchain4j.memory;

import dev.langchain4j.data.message.ChatMessage;
import java.util.Arrays;
import java.util.List;

/**
 * 聊天记忆(对话历史、聊天记录)
 * 表示聊天对话的记忆（历史）。
 * 由于语言模型不会保留对话状态，因此在每次与语言模型交互时，都需要提供之前的所有消息。
 * 聊天记忆帮助跟踪对话，并确保消息适合语言模型的上下文窗口。
 * Represents the memory (history) of a chat conversation.
 * Since language models do not keep the state of the conversation, it is necessary to provide all previous messages
 * on every interaction with the language model.
 * {@link ChatMemory} helps with keeping track of the conversation and ensuring that messages fit within language model's context window.
 */
public interface ChatMemory {

    /**
     * 聊天记忆ID
     * The ID of the {@link ChatMemory}.
     * @return The ID of the {@link ChatMemory}.
     */
    Object id();

    /**
     * 向聊天记忆中添加一条聊天消息。
     * Adds a message to the chat memory.
     *
     * @param message The {@link ChatMessage} to add.
     */
    void add(ChatMessage message);

    /**
     * 将聊天消息列表添加到聊天记忆。
     * Adds messages to the chat memory.
     * @param messages The {@link ChatMessage}s to add
     */
    default void add(ChatMessage... messages) {
        if ((messages != null) && (messages.length > 0)) {
            add(Arrays.asList(messages));
        }
    }

    /**
     * 将聊天消息列表添加到聊天记忆。
     * Adds messages to the chat memory.
     * @param messages The {@link ChatMessage}s to add
     */
    default void add(Iterable<ChatMessage> messages) {
        if (messages != null) {
            messages.forEach(this::add);
        }
    }

    /**
     * 从聊天记忆中检索聊天消息列表。
     * Retrieves messages from the chat memory.
     * Depending on the implementation, it may not return all previously added messages,
     * but rather a subset, a summary, or a combination thereof.
     *
     * @return A list of {@link ChatMessage} objects that represent the current state of the chat memory.
     */
    List<ChatMessage> messages();

    /**
     * 清除聊天记忆。
     * Clears the chat memory.
     */
    void clear();
}
