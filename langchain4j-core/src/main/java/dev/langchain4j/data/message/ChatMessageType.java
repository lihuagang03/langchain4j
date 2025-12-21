package dev.langchain4j.data.message;

/**
 * 聊天消息的类型
 * The type of chat message, e.g. system, user or AI.
 * Maps to implementations of {@link ChatMessage}.
 */
public enum ChatMessageType {
    /**
     * 系统消息，通常由开发者定义。
     * A message from the system, typically defined by a developer.
     */
    SYSTEM(SystemMessage.class),

    /**
     * 用户消息
     * A message from the user.
     */
    USER(UserMessage.class),

    /**
     * AI消息
     * A message from the AI.
     */
    AI(AiMessage.class),

    /**
     * 工具执行结果消息
     * A message from a tool.
     */
    TOOL_EXECUTION_RESULT(ToolExecutionResultMessage.class),

    /**
     * 自定义消息
     * A custom message.
     */
    CUSTOM(CustomMessage.class);

    /**
     * 消息的实现类
     */
    private final Class<? extends ChatMessage> messageClass;

    ChatMessageType(Class<? extends ChatMessage> messageClass) {
        this.messageClass = messageClass;
    }

    /**
     * 返回消息类型的类。
     * Returns the class of the message type.
     * @return the class of the message type.
     */
    public Class<? extends ChatMessage> messageClass() {
        return messageClass;
    }
}
