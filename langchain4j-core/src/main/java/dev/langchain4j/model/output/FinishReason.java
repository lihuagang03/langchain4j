package dev.langchain4j.model.output;

/**
 * 完成原因
 * 模型调用完成的原因。
 * The reason why a model call finished.
 */
public enum FinishReason {
    /**
     * 模型调用完成，因为模型认为请求已完成。
     * The model call finished because the model decided the request was done.
     */
    STOP,

    /**
     * 通话结束，因为已达到令牌长度限制。
     * The call finished because the token length was reached.
     */
    LENGTH,

    /**
     * 调用结束，表示需要执行工具。
     * The call finished signalling a need for tool execution.
     */
    TOOL_EXECUTION,

    /**
     * 通话结束，提示需要内容过滤。
     * The call finished signalling a need for content filtering.
     */
    CONTENT_FILTER,

    /**
     * 通话因其他原因结束。
     * The call finished for some other reason.
     */
    OTHER
}
