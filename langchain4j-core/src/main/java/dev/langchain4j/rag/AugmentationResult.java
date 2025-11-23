package dev.langchain4j.rag;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.rag.content.Content;

import java.util.List;

import static dev.langchain4j.internal.Utils.copy;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * 增强结果，聊天对话消息增强的结果。
 * Represents the result of a {@link ChatMessage} augmentation.
 */
public class AugmentationResult {

    /**
     * 增强的聊天对话消息。
     * The augmented chat message.
     */
    private final ChatMessage chatMessage;

    /**
     * 用于增强原始聊天对话消息的内容列表。
     * A list of content used to augment the original chat message.
     */
    private final List<Content> contents;

    public AugmentationResult(ChatMessage chatMessage, List<Content> contents) {
        this.chatMessage = ensureNotNull(chatMessage, "chatMessage");
        this.contents = copy(contents);
    }

    public static AugmentationResultBuilder builder() {
        return new AugmentationResultBuilder();
    }

    public ChatMessage chatMessage() {
        return chatMessage;
    }

    public List<Content> contents() {
        return contents;
    }

    public static class AugmentationResultBuilder {

        private ChatMessage chatMessage;
        private List<Content> contents;

        AugmentationResultBuilder() {
        }

        public AugmentationResultBuilder chatMessage(ChatMessage chatMessage) {
            this.chatMessage = chatMessage;
            return this;
        }

        public AugmentationResultBuilder contents(List<Content> contents) {
            this.contents = contents;
            return this;
        }

        public AugmentationResult build() {
            return new AugmentationResult(this.chatMessage, this.contents);
        }
    }
}
