package dev.langchain4j.model.chat.request;

import dev.langchain4j.model.chat.ChatModel;

/**
 * 工具选择机制
 * 指定聊天模型应该如何使用工具。
 * Specifies how {@link ChatModel} should use tools.
 */
public enum ToolChoice {

    /**
     * 聊天模型可以选择是否使用工具、使用哪些工具以及使用多少工具。
     * The chat model can choose whether to use tools, which ones to use, and how many.
     */
    AUTO,

    /**
     * The chat model is required to use one or more tools.
     */
    REQUIRED,

    /**
     * The chat model cannot use tools
     */
    NONE,
}
