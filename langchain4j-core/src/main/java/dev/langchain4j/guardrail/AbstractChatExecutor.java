package dev.langchain4j.guardrail;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.Internal;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.List;

/**
 * 聊天执行器的抽象基类
 * 聊天执行器的抽象基类，为实现 ChatExecutor 接口提供通用结构和共享功能。
 * Abstract base class for chat executors that provides a common structure and shared functionality
 * for implementing the {@link ChatExecutor} interface.
 *
 * 这个类封装了一个 ChatRequest，并允许子类通过实现 execute(ChatRequest) 方法来定义请求的处理方式。
 * This class encapsulates a {@link ChatRequest} and allows subclasses to define how
 * the request should be processed by implementing the {@code execute(ChatRequest)} method.
 *
 * 子类应当是不可变的，并且应该提供执行聊天请求的具体实现，通常使用特定的聊天模型或处理策略。
 * Subclasses are expected to be immutable and should provide specific implementations for
 * executing chat requests, typically using particular chat models or processing strategies.
 *
 * 职责：
 * Responsibilities:
 * - Stores a {@link ChatRequest} object which can be used to build specific chat requests.
 *   存储一个 ChatRequest 对象，可用于构建特定的聊天请求。
 * - Provides standard implementations for executing a chat request with a list of messages
 *   or without any additional input.
 *   提供执行聊天请求的标准实现，可使用消息列表或不附加任何输入。
 * - Defines an abstract method {@code execute(ChatRequest)} for subclasses to implement
 *   specific execution logic.
 *   定义一个抽象方法 execute(ChatRequest)，供子类实现具体的执行逻辑。
 */
@Internal
abstract class AbstractChatExecutor implements ChatExecutor {
    /**
     * 聊天请求
     */
    protected final ChatRequest chatRequest;

    protected AbstractChatExecutor(AbstractBuilder<?> builder) {
        this.chatRequest = ensureNotNull(builder.chatRequest, "chatRequest");
    }

    @Override
    public ChatResponse execute(List<ChatMessage> chatMessages) {
        // 根据聊天消息列表，构建新的聊天请求
        var newChatRequest = this.chatRequest.toBuilder().messages(chatMessages).build();

        // 执行聊天请求
        return execute(newChatRequest);
    }

    @Override
    public ChatResponse execute() {
        // 执行聊天请求
        return execute(this.chatRequest);
    }

    /**
     * 执行给定的聊天请求并返回相应的聊天响应。
     * Executes a given chat request and returns the corresponding chat response.
     *
     * @param chatRequest the chat request to process, containing the input messages and any necessary configurations
     * @return the chat response generated as a result of processing the given chat request
     */
    protected abstract ChatResponse execute(ChatRequest chatRequest);
}
