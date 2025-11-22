package dev.langchain4j.model.input;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.Objects;

import static dev.langchain4j.data.message.AiMessage.aiMessage;
import static dev.langchain4j.data.message.SystemMessage.systemMessage;
import static dev.langchain4j.data.message.UserMessage.userMessage;
import static dev.langchain4j.internal.Utils.quoted;
import static dev.langchain4j.internal.ValidationUtils.ensureNotBlank;

/**
 * 提示，一个发送给LLM的输入文本。
 * 一个提示通常包含指令、上下文信息、最终用户输入等。
 * 一个提示通常是通过将一个或多个值应用于提示模板来创建的。
 * Represents a prompt (an input text sent to the LLM).
 * A prompt usually contains instructions, contextual information, end-user input, etc.
 * A Prompt is typically created by applying one or multiple values to a PromptTemplate.
 */
public class Prompt {

    /**
     * 输入文本
     */
    private final String text;

    /**
     * Create a new Prompt.
     * @param text the text of the prompt.
     */
    public Prompt(String text) {
        this.text = ensureNotBlank(text, "text");
    }

    /**
     * The text of the prompt.
     * @return the text of the prompt.
     */
    public String text() {
        return text;
    }

    /**
     * 将这个提示转换为系统消息。
     * Convert this prompt to a SystemMessage.
     * @return the SystemMessage.
     */
    public SystemMessage toSystemMessage() {
        return systemMessage(text);
    }

    /**
     * 将这个提示转换为用户消息，使用指定的用户名称。
     * Convert this prompt to a UserMessage with specified userName.
     * @return the UserMessage.
     */
    public UserMessage toUserMessage(String userName) {
        return userMessage(userName, text);
    }

    /**
     * 将这个提示转换为用户消息。
     * Convert this prompt to a UserMessage.
     * @return the UserMessage.
     */
    public UserMessage toUserMessage() {
        return userMessage(text);
    }

    /**
     * Convert this prompt to an AiMessage.
     * 将这个提示转换为AI消息。
     * @return the AiMessage.
     */
    public AiMessage toAiMessage() {
        return aiMessage(text);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prompt that = (Prompt) o;
        return Objects.equals(this.text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return "Prompt {" +
                " text = " + quoted(text) +
                " }";
    }

    /**
     * Create a new Prompt.
     * @param text the text of the prompt.
     * @return the new Prompt.
     */
    public static Prompt from(String text) {
        return new Prompt(text);
    }
}
