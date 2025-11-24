package dev.langchain4j.data.message;

/**
 * 聊天对话消息类型
 * The type of chat message, e.g. system, user or AI.
 * Maps to implementations of {@link ChatMessage}.
 */
public enum ChatMessageType {
    /**
     * A message from the system, typically defined by a developer.
     * 系统消息，通常由开发者定义。
     */
    SYSTEM(SystemMessage.class),

    /**
     * A message from the user.
     * 用户消息
     */
    USER(UserMessage.class),

    /**
     * A message from the AI.
     * AI消息
     */
    AI(AiMessage.class),

    /**
     * A message from a tool.
     * 工具执行结果消息
     */
    TOOL_EXECUTION_RESULT(ToolExecutionResultMessage.class),

    /**
     * A custom message.
     * 自定义消息
     */
    CUSTOM(CustomMessage.class);

    private final Class<? extends ChatMessage> messageClass;

    ChatMessageType(Class<? extends ChatMessage> messageClass) {
        this.messageClass = messageClass;
    }

    /**
     * Returns the class of the message type.
     * @return the class of the message type.
     */
    public Class<? extends ChatMessage> messageClass() {
        return messageClass;
    }
}
