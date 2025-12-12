package dev.langchain4j.model.moderation;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.output.Response;

import java.util.List;

/**
 * 审核模型
 * 表示一个可以审查文本的模型。
 * Represents a model that can moderate text.
 */
public interface ModerationModel {

    /**
     * 审核给定的文本。
     * Moderates the given text.
     * @param text the text to moderate.
     * @return the moderation {@code Response}.
     */
    Response<Moderation> moderate(String text);

    /**
     * 对给定的提示进行审核。
     * Moderates the given prompt.
     * @param prompt the prompt to moderate.
     * @return the moderation {@code Response}.
     */
    default Response<Moderation> moderate(Prompt prompt) {
        return moderate(prompt.text());
    }
  
    /**
     * Moderates the given chat message.
     * @param message the chat message to moderate.
     * @return the moderation {@code Response}.
     */
    default Response<Moderation> moderate(ChatMessage message) {
        return moderate(List.of(message));
    }

    /**
     * 审核给定的聊天消息列表。
     * Moderates the given list of chat messages.
     * @param messages the list of chat messages to moderate.
     * @return the moderation {@code Response}.
     */
    Response<Moderation> moderate(List<ChatMessage> messages);

    /**
     * 审核给定的文本段落。
     * Moderates the given text segment.
     * @param textSegment the text segment to moderate.
     * @return the moderation {@code Response}.
     */
    default Response<Moderation> moderate(TextSegment textSegment) {
        return moderate(textSegment.text());
    }
}
