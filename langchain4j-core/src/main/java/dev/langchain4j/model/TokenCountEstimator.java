package dev.langchain4j.model;

import dev.langchain4j.data.message.ChatMessage;

/**
 * 词元计数估算器
 * 表示一个用于估算各种文本类型（例如文本、提示、文本片段等）中的词元数量的接口。
 * 当需要事先了解处理指定文本所需的成本时，这会非常有用。
 * Represents an interface for estimating the count of tokens in various text types such as a text, prompt, text segment, etc.
 * This can be useful when it's necessary to know in advance the cost of processing a specified text by the LLM.
 */
public interface TokenCountEstimator {

    /**
     * 估算给定文本中的词元数量。
     * Estimates the count of tokens in the given text.
     *
     * @param text the text.
     * @return the estimated count of tokens.
     */
    int estimateTokenCountInText(String text);

    /**
     * 估算给定聊天消息中的词元数量。
     * Estimates the count of tokens in the given message.
     *
     * @param message the message.
     * @return the estimated count of tokens.
     */
    int estimateTokenCountInMessage(ChatMessage message);

    /**
     * 估算给定聊天消息列表中的词元数量。
     * Estimates the count of tokens in the given messages.
     *
     * @param messages the messages.
     * @return the estimated count of tokens.
     */
    int estimateTokenCountInMessages(Iterable<ChatMessage> messages);
}
