package dev.langchain4j.model.chat.response;

import dev.langchain4j.Experimental;

/**
 * 流处理句柄
 * 可以通过 StreamingChatResponseHandler 用来取消进行的流式处理的句柄。
 * Handle that can be used to cancel the streaming done via {@link StreamingChatResponseHandler}.
 *
 * @since 1.8.0
 */
@Experimental
public interface StreamingHandle {

    /**
     * Cancels the streaming.
     */
    void cancel();

    /**
     * Returns {@code true} if streaming was cancelled by calling {@link #cancel()}.
     */
    boolean isCancelled();
}
