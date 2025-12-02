package dev.langchain4j.data.message;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;

/**
 * 聊天消息
 * 表示一条聊天消息。
 * 与 ChatModel 和 StreamingChatModel 一起使用。
 * Represents a chat message.
 * Used together with {@link ChatModel} and {@link StreamingChatModel}.
 *
 * @see SystemMessage
 * @see UserMessage
 * @see AiMessage
 * @see ToolExecutionResultMessage
 * @see CustomMessage
 */
public interface ChatMessage {

    /**
     * 消息的类型
     * The type of the message.
     *
     * @return the type of the message
     */
    ChatMessageType type();
}
