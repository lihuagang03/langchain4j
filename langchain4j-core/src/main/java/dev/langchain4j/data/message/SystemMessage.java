package dev.langchain4j.data.message;

import static dev.langchain4j.data.message.ChatMessageType.SYSTEM;
import static dev.langchain4j.internal.Utils.quoted;
import static dev.langchain4j.internal.ValidationUtils.ensureNotBlank;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 系统消息
 * 表示一个系统消息，通常由开发者定义。
 * 这种类型的信息通常提供关于人工智能行为的指示，例如其行为方式或应答风格。
 * Represents a system message, typically defined by a developer.
 * This type of message usually provides instructions regarding the AI's actions, such as its behavior or response style.
 */
public class SystemMessage implements ChatMessage {

    /**
     * 文本
     */
    private final String text;

    /**
     * Creates a new system message.
     * @param text the message text.
     */
    public SystemMessage(String text) {
        this.text = ensureNotBlank(text, "text");
    }

    /**
     * Returns the message text.
     * @return the message text.
     */
    public String text() {
        return text;
    }

    @Override
    public ChatMessageType type() {
        return SYSTEM;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SystemMessage that = (SystemMessage) o;
        return Objects.equals(this.text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }

    @Override
    public String toString() {
        return "SystemMessage {" + " text = " + quoted(text) + " }";
    }

    /**
     * Creates a new system message.
     * @param text the message text.
     * @return the system message.
     */
    public static SystemMessage from(String text) {
        return new SystemMessage(text);
    }

    /**
     * Creates a new system message.
     * @param text the message text.
     * @return the system message.
     */
    public static SystemMessage systemMessage(String text) {
        return from(text);
    }

    public static Optional<SystemMessage> findFirst(List<ChatMessage> messages) {
        return messages.stream()
                .filter(message -> message instanceof SystemMessage)
                .map(message -> (SystemMessage) message)
                .findFirst();
    }

    public static Optional<SystemMessage> findLast(List<ChatMessage> messages) {
        return messages.stream()
                .filter(message -> message instanceof SystemMessage)
                .map(message -> (SystemMessage) message)
                .reduce((first, second) -> second);
    }

    public static List<SystemMessage> findAll(List<ChatMessage> messages) {
        return messages.stream()
                .filter(message -> message instanceof SystemMessage)
                .map(message -> (SystemMessage) message)
                .toList();
    }
}
