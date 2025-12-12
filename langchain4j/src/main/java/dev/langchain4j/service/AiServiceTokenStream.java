package dev.langchain4j.service;

import static dev.langchain4j.internal.Utils.copy;
import static dev.langchain4j.internal.ValidationUtils.ensureNotEmpty;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import dev.langchain4j.Internal;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.guardrail.ChatExecutor;
import dev.langchain4j.guardrail.GuardrailRequestParams;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialThinkingContext;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.tool.BeforeToolExecution;
import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
import dev.langchain4j.service.tool.ToolExecutor;

/**
 * AI服务的词元流
 */
@Internal
public class AiServiceTokenStream implements TokenStream {

    /**
     * 聊天消息列表
     */
    private final List<ChatMessage> messages;

    /**
     * 工具规格列表
     */
    private final List<ToolSpecification> toolSpecifications;
    /**
     * 工具名称到工具执行器的映射表
     */
    private final Map<String, ToolExecutor> toolExecutors;
    /**
     * 工具参数错误处理程序
     */
    private final ToolArgumentsErrorHandler toolArgumentsErrorHandler;
    /**
     * 工具执行错误处理程序
     */
    private final ToolExecutionErrorHandler toolExecutionErrorHandler;
    /**
     * 工具执行器
     */
    private final Executor toolExecutor;

    /**
     * 接收到的内容列表
     */
    private final List<Content> retrievedContents;
    /**
     * AI服务的上下文
     */
    private final AiServiceContext context;
    /**
     * AI服务调用的上下文
     */
    private final InvocationContext invocationContext;
    private final GuardrailRequestParams commonGuardrailParams;
    /**
     * 方法的键
     */
    private final Object methodKey;

    /**
     * 部分响应的处理器
     */
    private Consumer<String> partialResponseHandler;
    /**
     * 部分响应及其上下文的处理器
     */
    private BiConsumer<PartialResponse, PartialResponseContext> partialResponseWithContextHandler;
    /**
     * 部分思考的处理器
     */
    private Consumer<PartialThinking> partialThinkingHandler;
    /**
     * 部分思考及其上下文的处理器
     */
    private BiConsumer<PartialThinking, PartialThinkingContext> partialThinkingWithContextHandler;
    /**
     * 内容列表的处理器
     */
    private Consumer<List<Content>> contentsHandler;
    /**
     * 中间聊天响应的处理器
     */
    private Consumer<ChatResponse> intermediateResponseHandler;
    /**
     * 工具执行前的处理器
     */
    private Consumer<BeforeToolExecution> beforeToolExecutionHandler;
    /**
     * 工具执行的处理器
     */
    private Consumer<ToolExecution> toolExecutionHandler;
    /**
     * 完成聊天响应的处理器
     */
    private Consumer<ChatResponse> completeResponseHandler;
    /**
     * 错误异常的处理器
     */
    private Consumer<Throwable> errorHandler;

    private int onPartialResponseInvoked;
    private int onPartialResponseWithContextInvoked;
    private int onPartialThinkingInvoked;
    private int onPartialThinkingWithContextInvoked;
    private int onIntermediateResponseInvoked;
    private int onCompleteResponseInvoked;
    private int onRetrievedInvoked;
    private int beforeToolExecutionInvoked;
    private int onToolExecutedInvoked;
    private int onErrorInvoked;
    private int ignoreErrorsInvoked;

    /**
     * Creates a new instance of {@link AiServiceTokenStream} with the given parameters.
     *
     * @param parameters the parameters for creating the token stream
     */
    public AiServiceTokenStream(AiServiceTokenStreamParameters parameters) {
        ensureNotNull(parameters, "parameters");
        this.messages = copy(ensureNotEmpty(parameters.messages(), "messages"));
        this.toolSpecifications = copy(parameters.toolSpecifications());
        this.toolExecutors = copy(parameters.toolExecutors());
        this.toolArgumentsErrorHandler = parameters.toolArgumentsErrorHandler();
        this.toolExecutionErrorHandler = parameters.toolExecutionErrorHandler();
        this.toolExecutor = parameters.toolExecutor();
        this.retrievedContents = copy(parameters.gretrievedContents());
        this.context = ensureNotNull(parameters.context(), "context");
        ensureNotNull(this.context.streamingChatModel, "streamingChatModel");
        this.invocationContext = parameters.invocationContext();
        this.commonGuardrailParams = parameters.commonGuardrailParams();
        this.methodKey = parameters.methodKey();
    }

    @Override
    public TokenStream onPartialResponse(Consumer<String> partialResponseHandler) {
        this.partialResponseHandler = partialResponseHandler;
        this.onPartialResponseInvoked++;
        return this;
    }

    @Override
    public TokenStream onPartialResponseWithContext(BiConsumer<PartialResponse, PartialResponseContext> handler) {
        this.partialResponseWithContextHandler = handler;
        this.onPartialResponseWithContextInvoked++;
        return this;
    }

    @Override
    public TokenStream onPartialThinking(Consumer<PartialThinking> partialThinkingHandler) {
        this.partialThinkingHandler = partialThinkingHandler;
        this.onPartialThinkingInvoked++;
        return this;
    }

    @Override
    public TokenStream onPartialThinkingWithContext(BiConsumer<PartialThinking, PartialThinkingContext> handler) {
        this.partialThinkingWithContextHandler = handler;
        this.onPartialThinkingWithContextInvoked++;
        return this;
    }

    @Override
    public TokenStream onRetrieved(Consumer<List<Content>> contentsHandler) {
        this.contentsHandler = contentsHandler;
        this.onRetrievedInvoked++;
        return this;
    }

    @Override
    public TokenStream onIntermediateResponse(Consumer<ChatResponse> intermediateResponseHandler) {
        this.intermediateResponseHandler = intermediateResponseHandler;
        this.onIntermediateResponseInvoked++;
        return this;
    }

    @Override
    public TokenStream beforeToolExecution(Consumer<BeforeToolExecution> beforeToolExecutionHandler) {
        this.beforeToolExecutionHandler = beforeToolExecutionHandler;
        this.beforeToolExecutionInvoked++;
        return this;
    }

    @Override
    public TokenStream onToolExecuted(Consumer<ToolExecution> toolExecutionHandler) {
        this.toolExecutionHandler = toolExecutionHandler;
        this.onToolExecutedInvoked++;
        return this;
    }

    @Override
    public TokenStream onCompleteResponse(Consumer<ChatResponse> completionHandler) {
        this.completeResponseHandler = completionHandler;
        this.onCompleteResponseInvoked++;
        return this;
    }

    @Override
    public TokenStream onError(Consumer<Throwable> errorHandler) {
        this.errorHandler = errorHandler;
        this.onErrorInvoked++;
        return this;
    }

    @Override
    public TokenStream ignoreErrors() {
        this.errorHandler = null;
        this.ignoreErrorsInvoked++;
        return this;
    }

    @Override
    public void start() {
        // 验证配置
        validateConfiguration();

        // 聊天请求
        ChatRequest chatRequest = context.chatRequestTransformer.apply(
                ChatRequest.builder()
                        .messages(messages)
                        .toolSpecifications(toolSpecifications)
                        .build(),
                invocationContext.chatMemoryId());

        // 聊天执行器
        ChatExecutor chatExecutor = ChatExecutor.builder(context.streamingChatModel)
                .errorHandler(errorHandler)
                .chatRequest(chatRequest)
                .build();

        // AI服务的流式聊天模型的响应处理器
        var handler = new AiServiceStreamingResponseHandler(
                chatExecutor,
                context,
                invocationContext,
                partialResponseHandler,
                partialResponseWithContextHandler,
                partialThinkingHandler,
                partialThinkingWithContextHandler,
                beforeToolExecutionHandler,
                toolExecutionHandler,
                intermediateResponseHandler,
                completeResponseHandler,
                errorHandler,
                initTemporaryMemory(context, messages),
                new TokenUsage(),
                toolSpecifications,
                toolExecutors,
                toolArgumentsErrorHandler,
                toolExecutionErrorHandler,
                toolExecutor,
                commonGuardrailParams,
                methodKey);

        if (contentsHandler != null && retrievedContents != null) {
            contentsHandler.accept(retrievedContents);
        }

        // 这是与聊天模型交互的主要 API
        context.streamingChatModel.chat(chatRequest, handler);
    }

    private void validateConfiguration() {
        if (onPartialResponseInvoked + onPartialResponseWithContextInvoked > 1) {
            throw new IllegalConfigurationException("One of [onPartialResponse, onPartialResponseWithContext] " +
                    "can be invoked on TokenStream at most 1 time");
        }
        if (onPartialThinkingInvoked + onPartialThinkingWithContextInvoked > 1) {
            throw new IllegalConfigurationException("One of [onPartialThinking, onPartialThinkingWithContext] " +
                    "can be invoked on TokenStream at most 1 time");
        }
        if (onIntermediateResponseInvoked > 1) {
            throw new IllegalConfigurationException(
                    "onIntermediateResponse can be invoked on TokenStream at most 1 time");
        }
        if (onCompleteResponseInvoked > 1) {
            throw new IllegalConfigurationException("onCompleteResponse can be invoked on TokenStream at most 1 time");
        }
        if (onRetrievedInvoked > 1) {
            throw new IllegalConfigurationException("onRetrieved can be invoked on TokenStream at most 1 time");
        }
        if (beforeToolExecutionInvoked > 1) {
            throw new IllegalConfigurationException("beforeToolExecution can be invoked on TokenStream at most 1 time");
        }
        if (onToolExecutedInvoked > 1) {
            throw new IllegalConfigurationException("onToolExecuted can be invoked on TokenStream at most 1 time");
        }
        if (onErrorInvoked + ignoreErrorsInvoked != 1) {
            throw new IllegalConfigurationException(
                    "One of [onError, ignoreErrors] " + "must be invoked on TokenStream exactly 1 time");
        }
    }

    private ChatMemory initTemporaryMemory(AiServiceContext context, List<ChatMessage> messagesToSend) {
        // 聊天记忆
        var chatMemory = MessageWindowChatMemory.withMaxMessages(Integer.MAX_VALUE);

        if (!context.hasChatMemory()) {
            chatMemory.add(messagesToSend);
        }

        return chatMemory;
    }
}
