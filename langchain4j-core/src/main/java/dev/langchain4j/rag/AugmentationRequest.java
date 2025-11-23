package dev.langchain4j.rag;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.rag.query.Metadata;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * 增强请求，对聊天对话消息增强的请求。
 * Represents a request for {@link ChatMessage} augmentation.
 */
public class AugmentationRequest {

    /**
     * 要增强的聊天对话消息。
     * 目前，仅支持用户消息。
     * The chat message to be augmented.
     * Currently, only {@link UserMessage} is supported.
     */
    private final ChatMessage chatMessage;

    /**
     * Additional metadata related to the augmentation request.
     */
    private final Metadata metadata;

    public AugmentationRequest(ChatMessage chatMessage, Metadata metadata) {
        this.chatMessage = ensureNotNull(chatMessage, "chatMessage");
        this.metadata = ensureNotNull(metadata, "metadata");
    }

    public ChatMessage chatMessage() {
        return chatMessage;
    }

    public Metadata metadata() {
        return metadata;
    }
}
