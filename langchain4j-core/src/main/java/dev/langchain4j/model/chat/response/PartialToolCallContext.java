package dev.langchain4j.model.chat.response;

import dev.langchain4j.Experimental;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * 部分工具调用的上下文
 *
 * @since 1.8.0
 */
@Experimental
public class PartialToolCallContext {

    /**
     * 流处理句柄
     */
    private final StreamingHandle streamingHandle;

    public PartialToolCallContext(StreamingHandle streamingHandle) {
        this.streamingHandle = ensureNotNull(streamingHandle, "streamingHandle");
    }

    public StreamingHandle streamingHandle() {
        return streamingHandle;
    }
}
