package dev.langchain4j.guardrail;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.Internal;
import dev.langchain4j.exception.LangChain4jException;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 流式到同步的聊天执行器
 * ChatExecutor 接口的一个具体实现，它使用指定的 StreamingChatModel 执行聊天请求。
 * 然后，它会像同步执行一样执行这些请求，本质上将流式请求转换为同步请求。
 * A concrete implementation of the {@link ChatExecutor} interface that executes
 * chat requests using a specified {@link StreamingChatModel}. It then executes the requests as if it were
 * synchronous, essentially transforming a streaming request to a synchronous request
 *
 * 该类使用 ChatRequest 来封装输入消息和参数，并将聊天的执行委托给提供的 StreamingChatModel。
 * This class utilizes a {@link ChatRequest} to encapsulate the input messages
 *  and parameters and delegates the execution of the chat to the provided {@link StreamingChatModel}.
 *
 * 该类的实例是不可变的，通常使用 ChatExecutor.StreamingToSynchronousBuilder 进行实例化。
 *  Instances of this class are immutable and are typically instantiated using
 *  the {@link StreamingToSynchronousBuilder}.
 */
@Internal
final class StreamingToSynchronousChatExecutor extends AbstractChatExecutor {
    /**
     * 流式聊天模型
     */
    private final StreamingChatModel streamingChatModel;
    /**
     * 错误异常的处理器
     */
    private final Consumer<Throwable> errorHandler;

    StreamingToSynchronousChatExecutor(StreamingToSynchronousBuilder builder) {
        super(builder);

        this.streamingChatModel = ensureNotNull(builder.streamingChatModel, "streamingChatModel");
        this.errorHandler = builder.errorHandler;
    }

    @Override
    protected ChatResponse execute(ChatRequest chatRequest) {
        // 聊天模型的响应处理器
        var responseHandler = new StreamingToSyncResponseHandler(this.errorHandler);
        // 这是与聊天模型交互的主要 API
        this.streamingChatModel.chat(chatRequest, responseHandler);

        return Optional.ofNullable(responseHandler.getResponse()).orElseGet(ChatResponse.builder()::build);
    }

    /**
     * 流式到同步的聊天模型的响应处理器
     */
    private static class StreamingToSyncResponseHandler implements StreamingChatResponseHandler {
        private static final Logger LOG = LoggerFactory.getLogger(StreamingToSyncResponseHandler.class);

        /**
         * 错误异常的处理器
         */
        private final Consumer<Throwable> errorHandler;
        /**
         * 倒计时锁
         */
        private final CountDownLatch latch = new CountDownLatch(1);
        /**
         * 聊天响应的引用
         */
        private final AtomicReference<ChatResponse> response = new AtomicReference<>();

        StreamingToSyncResponseHandler(Consumer<Throwable> errorHandler) {
            this.errorHandler = errorHandler;
        }

        @Override
        public void onPartialResponse(String partialResponse) {}

        @Override
        public void onCompleteResponse(ChatResponse completeResponse) {
            // 聊天响应完成
            response.set(completeResponse);
            this.latch.countDown();
        }

        private void waitForCompletion() {
            try {
                // 等待完成
                this.latch.await();
            } catch (InterruptedException e) {
                throw new LangChain4jException(e);
            }
        }

        ChatResponse getResponse() {
            // 等待完成
            waitForCompletion();
            return this.response.get();
        }

        @Override
        public void onError(Throwable error) {
            if (errorHandler != null) {
                try {
                    // 接受错误
                    errorHandler.accept(error);
                } catch (Exception e) {
                    LOG.error("While handling the following error...", error);
                    LOG.error("...the following error happened", e);
                }
            } else {
                LOG.warn("Ignored error", error);
            }
        }
    }
}
