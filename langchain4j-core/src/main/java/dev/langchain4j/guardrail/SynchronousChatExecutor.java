package dev.langchain4j.guardrail;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.Internal;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * 同步的聊天执行器
 * ChatExecutor 接口的一个具体实现，使用指定的 ChatModel 执行聊天请求。
 * A concrete implementation of the {@link ChatExecutor} interface that executes
 * chat requests using a specified {@link ChatModel}.
 *
 * 该类使用 ChatRequest 来封装输入消息和参数，并将聊天的执行委托给提供的 ChatModel。
 * This class utilizes a {@link ChatRequest} to encapsulate the input messages
 * and parameters and delegates the execution of the chat to the provided
 * {@link ChatModel}.
 *
 * 该类的实例是不可变的，通常使用 ChatExecutor.SynchronousBuilder 来实例化。
 * Instances of this class are immutable and are typically instantiated using
 * the {@link SynchronousBuilder}.
 */
@Internal
final class SynchronousChatExecutor extends AbstractChatExecutor {
    /**
     * 聊天模型
     */
    private final ChatModel chatModel;

    protected SynchronousChatExecutor(SynchronousBuilder builder) {
        super(builder);
        this.chatModel = ensureNotNull(builder.chatModel, "chatModel");
    }

    @Override
    protected ChatResponse execute(ChatRequest chatRequest) {
        // 这是与聊天模型交互的主要 API
        return this.chatModel.chat(chatRequest);
    }
}
