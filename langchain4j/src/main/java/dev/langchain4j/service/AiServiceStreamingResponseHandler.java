package dev.langchain4j.service;

import static dev.langchain4j.internal.Utils.copy;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static dev.langchain4j.service.tool.ToolService.executeWithErrorHandling;

import dev.langchain4j.Internal;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.guardrail.ChatExecutor;
import dev.langchain4j.guardrail.GuardrailRequestParams;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.CompleteToolCall;
import dev.langchain4j.model.chat.response.PartialResponse;
import dev.langchain4j.model.chat.response.PartialResponseContext;
import dev.langchain4j.model.chat.response.PartialThinking;
import dev.langchain4j.model.chat.response.PartialThinkingContext;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.observability.api.event.AiServiceCompletedEvent;
import dev.langchain4j.observability.api.event.AiServiceErrorEvent;
import dev.langchain4j.observability.api.event.AiServiceResponseReceivedEvent;
import dev.langchain4j.observability.api.event.ToolExecutedEvent;
import dev.langchain4j.service.tool.BeforeToolExecution;
import dev.langchain4j.service.tool.ToolArgumentsErrorHandler;
import dev.langchain4j.service.tool.ToolExecution;
import dev.langchain4j.service.tool.ToolExecutionErrorHandler;
import dev.langchain4j.service.tool.ToolExecutionResult;
import dev.langchain4j.service.tool.ToolExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI服务的流式聊天模型的响应处理器
 * 处理来自 AI 服务的语言模型的响应，该响应以逐词元方式流式传输。
 * 处理常规（文本）响应以及带有执行一个或多个工具请求的响应。
 * Handles response from a language model for AI Service that is streamed token-by-token. Handles both regular (text)
 * responses and responses with the request to execute one or multiple tools.
 */
@Internal
class AiServiceStreamingResponseHandler implements StreamingChatResponseHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AiServiceStreamingResponseHandler.class);

    /**
     * 聊天执行器
     */
    private final ChatExecutor chatExecutor;
    /**
     * AI服务的上下文
     */
    private final AiServiceContext context;
    /**
     * AI服务调用的上下文
     */
    private final InvocationContext invocationContext;
    /**
     * 护栏请求参数
     */
    private final GuardrailRequestParams commonGuardrailParams;
    /**
     * 方法的键
     */
    private final Object methodKey;

    /**
     * 部分响应的处理器
     */
    private final Consumer<String> partialResponseHandler;
    /**
     * 部分响应及其上下文的处理器
     */
    private final BiConsumer<PartialResponse, PartialResponseContext> partialResponseWithContextHandler;
    /**
     * 部分思考的处理器
     */
    private final Consumer<PartialThinking> partialThinkingHandler;
    /**
     * 部分思考及其上下文的处理器
     */
    private final BiConsumer<PartialThinking, PartialThinkingContext> partialThinkingWithContextHandler;
    /**
     * 工具执行前的处理器
     */
    private final Consumer<BeforeToolExecution> beforeToolExecutionHandler;
    /**
     * 工具执行的处理器
     */
    private final Consumer<ToolExecution> toolExecutionHandler;
    /**
     * 中间聊天响应的处理器
     */
    private final Consumer<ChatResponse> intermediateResponseHandler;
    /**
     * 完成聊天响应的处理器
     */
    private final Consumer<ChatResponse> completeResponseHandler;

    /**
     * 错误异常的处理器
     */
    private final Consumer<Throwable> errorHandler;

    /**
     * 临时的聊天记忆
     */
    private final ChatMemory temporaryMemory;
    /**
     * 词元使用情况
     */
    private final TokenUsage tokenUsage;

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
     * 工具执行的请求和结果的队列
     */
    private final Queue<Future<ToolRequestResult>> toolExecutionFutures = new ConcurrentLinkedQueue<>();

    /**
     * 响应缓冲区
     */
    private final List<String> responseBuffer = new ArrayList<>();
    private final boolean hasOutputGuardrails;

    /**
     * 工具执行的请求和结果
     * @param request 工具执行请求
     * @param result 工具执行结果
     */
    private record ToolRequestResult(ToolExecutionRequest request, ToolExecutionResult result) {}

    /**
     * AI服务的流式聊天模型的响应处理器
     */
    AiServiceStreamingResponseHandler(
            ChatExecutor chatExecutor,
            AiServiceContext context,
            InvocationContext invocationContext,
            Consumer<String> partialResponseHandler,
            BiConsumer<PartialResponse, PartialResponseContext> partialResponseWithContextHandler,
            Consumer<PartialThinking> partialThinkingHandler,
            BiConsumer<PartialThinking, PartialThinkingContext> partialThinkingWithContextHandler,
            Consumer<BeforeToolExecution> beforeToolExecutionHandler,
            Consumer<ToolExecution> toolExecutionHandler,
            Consumer<ChatResponse> intermediateResponseHandler,
            Consumer<ChatResponse> completeResponseHandler,
            Consumer<Throwable> errorHandler,
            ChatMemory temporaryMemory,
            TokenUsage tokenUsage,
            List<ToolSpecification> toolSpecifications,
            Map<String, ToolExecutor> toolExecutors,
            ToolArgumentsErrorHandler toolArgumentsErrorHandler,
            ToolExecutionErrorHandler toolExecutionErrorHandler,
            Executor toolExecutor,
            GuardrailRequestParams commonGuardrailParams,
            Object methodKey) {
        this.chatExecutor = ensureNotNull(chatExecutor, "chatExecutor");
        this.context = ensureNotNull(context, "context");
        this.invocationContext = ensureNotNull(invocationContext, "invocationContext");
        this.methodKey = methodKey;

        this.partialResponseHandler = partialResponseHandler;
        this.partialResponseWithContextHandler = partialResponseWithContextHandler;
        this.partialThinkingHandler = partialThinkingHandler;
        this.partialThinkingWithContextHandler = partialThinkingWithContextHandler;
        this.intermediateResponseHandler = intermediateResponseHandler;
        this.completeResponseHandler = completeResponseHandler;
        this.beforeToolExecutionHandler = beforeToolExecutionHandler;
        this.toolExecutionHandler = toolExecutionHandler;
        this.errorHandler = errorHandler;

        this.temporaryMemory = temporaryMemory;
        this.tokenUsage = ensureNotNull(tokenUsage, "tokenUsage");
        this.commonGuardrailParams = commonGuardrailParams;

        this.toolSpecifications = copy(toolSpecifications);
        this.toolExecutors = copy(toolExecutors);
        this.toolArgumentsErrorHandler = ensureNotNull(toolArgumentsErrorHandler, "toolArgumentsErrorHandler");
        this.toolExecutionErrorHandler = ensureNotNull(toolExecutionErrorHandler, "toolExecutionErrorHandler");
        this.toolExecutor = toolExecutor;

        this.hasOutputGuardrails = context.guardrailService().hasOutputGuardrails(methodKey);
    }

    @Override
    public void onPartialResponse(String partialResponse) {
        // If we're using output guardrails, then buffer the partial response until the guardrails have completed
        if (hasOutputGuardrails) {
            responseBuffer.add(partialResponse);
        } else if (partialResponseHandler != null) {
            partialResponseHandler.accept(partialResponse);
        } else if (partialResponseWithContextHandler != null) {
            // 部分响应的上下文
            PartialResponseContext context = new PartialResponseContext(new CancellationUnsupportedStreamingHandle());
            partialResponseWithContextHandler.accept(new PartialResponse(partialResponse), context);
        }
    }

    @Override
    public void onPartialResponse(PartialResponse partialResponse, PartialResponseContext context) {
        // If we're using output guardrails, then buffer the partial response until the guardrails have completed
        if (hasOutputGuardrails) {
            responseBuffer.add(partialResponse.text());
        } else if (partialResponseHandler != null) {
            partialResponseHandler.accept(partialResponse.text());
        } else if (partialResponseWithContextHandler != null) {
            // 部分响应及其上下文
            partialResponseWithContextHandler.accept(partialResponse, context);
        }
    }

    @Override
    public void onPartialThinking(PartialThinking partialThinking) {
        if (partialThinkingHandler != null) {
            partialThinkingHandler.accept(partialThinking);
        } else if (partialThinkingWithContextHandler != null) {
            // 部分思考的上下文
            PartialThinkingContext context = new PartialThinkingContext(new CancellationUnsupportedStreamingHandle());
            partialThinkingWithContextHandler.accept(partialThinking, context);
        }
    }

    @Override
    public void onPartialThinking(PartialThinking partialThinking, PartialThinkingContext context) {
        if (partialThinkingHandler != null) {
            partialThinkingHandler.accept(partialThinking);
        } else if (partialThinkingWithContextHandler != null) {
            // 部分思考及其上下文
            partialThinkingWithContextHandler.accept(partialThinking, context);
        }
    }

    @Override
    public void onCompleteToolCall(CompleteToolCall completeToolCall) {
        if (toolExecutor != null) {
            // 完成工具调用的工具执行请求
            ToolExecutionRequest toolRequest = completeToolCall.toolExecutionRequest();
            var future = CompletableFuture.supplyAsync(
                    () -> {
                        // 异步地执行工具调用
                        ToolExecutionResult toolResult = execute(toolRequest);
                        return new ToolRequestResult(toolRequest, toolResult);
                    },
                    toolExecutor);
            toolExecutionFutures.add(future);
        }
    }

    // 触发事件

    private <T> void fireInvocationComplete(T result) {
        context.eventListenerRegistrar.fireEvent(AiServiceCompletedEvent.builder()
                .invocationContext(invocationContext)
                .result(result)
                .build());
    }

    private void fireToolExecutedEvent(ToolRequestResult toolRequestResult) {
        context.eventListenerRegistrar.fireEvent(ToolExecutedEvent.builder()
                .invocationContext(invocationContext)
                .request(toolRequestResult.request())
                .resultText(toolRequestResult.result().resultText())
                .build());
    }

    private void fireResponseReceivedEvent(ChatResponse chatResponse) {
        context.eventListenerRegistrar.fireEvent(AiServiceResponseReceivedEvent.builder()
                .invocationContext(invocationContext)
                .response(chatResponse)
                .build());
    }

    private void fireErrorReceived(Throwable error) {
        context.eventListenerRegistrar.fireEvent(AiServiceErrorEvent.builder()
                .invocationContext(invocationContext)
                .error(error)
                .build());
    }

    @Override
    public void onCompleteResponse(ChatResponse chatResponse) {
        // 触发 AI服务响应已接收到事件
        fireResponseReceivedEvent(chatResponse);
        // 聊天响应的AI消息
        AiMessage aiMessage = chatResponse.aiMessage();
        // 添加到聊天记忆中
        addToMemory(aiMessage);

        if (aiMessage.hasToolExecutionRequests()) {

            // 中间聊天响应的处理器
            if (intermediateResponseHandler != null) {
                intermediateResponseHandler.accept(chatResponse);
            }

            boolean immediateToolReturn = true;

            if (toolExecutor != null) {
                for (Future<ToolRequestResult> toolExecutionFuture : toolExecutionFutures) {
                    try {
                        // 工具执行的请求和结果
                        ToolRequestResult toolRequestResult = toolExecutionFuture.get();
                        // 触发 工具执行事件
                        fireToolExecutedEvent(toolRequestResult);
                        // 工具执行结果消息
                        ToolExecutionResultMessage toolExecutionResultMessage = ToolExecutionResultMessage.from(
                                toolRequestResult.request(),
                                toolRequestResult.result().resultText());
                        // 添加到聊天记忆中
                        addToMemory(toolExecutionResultMessage);
                        // 中间工具的返回
                        immediateToolReturn = immediateToolReturn
                                && context.toolService.isImmediateTool(toolExecutionResultMessage.toolName());
                    } catch (ExecutionException e) {
                        if (e.getCause() instanceof RuntimeException re) {
                            throw re;
                        } else {
                            throw new RuntimeException(e.getCause());
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }
                }
            } else {
                for (ToolExecutionRequest toolRequest : aiMessage.toolExecutionRequests()) {
                    // 执行工具调用
                    ToolExecutionResult toolResult = execute(toolRequest);
                    ToolRequestResult toolRequestResult = new ToolRequestResult(toolRequest, toolResult);
                    // 触发 工具执行事件
                    fireToolExecutedEvent(toolRequestResult);
                    // 添加到聊天记忆中
                    addToMemory(ToolExecutionResultMessage.from(toolRequest, toolResult.resultText()));
                    // 中间工具的返回
                    immediateToolReturn =
                            immediateToolReturn && context.toolService.isImmediateTool(toolRequest.name());
                }
            }

            if (immediateToolReturn) {
                // 最终聊天响应
                ChatResponse finalChatResponse = finalResponse(chatResponse, aiMessage);
                // 触发 AI服务完成事件
                fireInvocationComplete(finalChatResponse);

                if (completeResponseHandler != null) {
                    completeResponseHandler.accept(finalChatResponse);
                }
                return;
            }

            // 聊天请求
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(messagesToSend(invocationContext.chatMemoryId()))
                    .toolSpecifications(toolSpecifications)
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
                    temporaryMemory,
                    TokenUsage.sum(tokenUsage, chatResponse.metadata().tokenUsage()),
                    toolSpecifications,
                    toolExecutors,
                    toolArgumentsErrorHandler,
                    toolExecutionErrorHandler,
                    toolExecutor,
                    commonGuardrailParams,
                    methodKey);

            // 这是与聊天模型交互的主要 API
            context.streamingChatModel.chat(chatRequest, handler);
        } else {
            // 最终聊天响应
            ChatResponse finalChatResponse = finalResponse(chatResponse, aiMessage);

            if (completeResponseHandler != null) {
                // Invoke output guardrails
                if (hasOutputGuardrails) {
                    if (commonGuardrailParams != null) {
                        var newCommonParams = commonGuardrailParams.toBuilder()
                                .chatMemory(getMemory())
                                .build();

                        var outputGuardrailParams = OutputGuardrailRequest.builder()
                                .responseFromLLM(finalChatResponse)
                                .chatExecutor(chatExecutor)
                                .requestParams(newCommonParams)
                                .build();

                        finalChatResponse =
                                context.guardrailService().executeGuardrails(methodKey, outputGuardrailParams);
                    }

                    // If we have output guardrails, we should process all of the partial responses first before
                    // completing
                    if (partialResponseHandler != null) {
                        responseBuffer.forEach(partialResponseHandler::accept);
                    }
                    responseBuffer.clear();
                }

                // 触发 AI服务完成事件
                fireInvocationComplete(finalChatResponse);
                completeResponseHandler.accept(finalChatResponse);
            } else {
                // 触发 AI服务完成事件
                fireInvocationComplete(finalChatResponse);
            }
        }
    }

    /**
     * 构建最终聊天响应
     * @param completeResponse 完成聊天响应
     * @param aiMessage AI消息
     * @return 最终聊天响应
     */
    private ChatResponse finalResponse(ChatResponse completeResponse, AiMessage aiMessage) {
        return ChatResponse.builder()
                .aiMessage(aiMessage)
                .metadata(completeResponse.metadata().toBuilder()
                        .tokenUsage(tokenUsage.add(completeResponse.metadata().tokenUsage()))
                        .build())
                .build();
    }

    /**
     * 执行工具调用
     * @param toolRequest 工具执行请求
     * @return 工具执行结果
     */
    private ToolExecutionResult execute(ToolExecutionRequest toolRequest) {
        // 工具执行器
        ToolExecutor toolExecutor = toolExecutors.get(toolRequest.name());
        // TODO applyToolHallucinationStrategy
        // 工具执行前处理
        handleBeforeTool(toolRequest);
        // 使用错误处理执行工具请求
        ToolExecutionResult toolResult = executeWithErrorHandling(
                toolRequest, toolExecutor, invocationContext, toolArgumentsErrorHandler, toolExecutionErrorHandler);
        // 工具执行后处理
        handleAfterTool(toolRequest, toolResult);
        return toolResult;
    }

    private void handleBeforeTool(ToolExecutionRequest request) {
        if (beforeToolExecutionHandler != null) {
            // 工具执行前
            BeforeToolExecution beforeToolExecution =
                    BeforeToolExecution.builder().request(request).build();
            beforeToolExecutionHandler.accept(beforeToolExecution);
        }
    }

    private void handleAfterTool(ToolExecutionRequest request, ToolExecutionResult result) {
        if (toolExecutionHandler != null) {
            ToolExecution toolExecution =
                    ToolExecution.builder().request(request).result(result).build();
            toolExecutionHandler.accept(toolExecution);
        }
    }

    private ChatMemory getMemory() {
        return getMemory(invocationContext.chatMemoryId());
    }

    private ChatMemory getMemory(Object memoryId) {
        return context.hasChatMemory()
                ? context.chatMemoryService.getOrCreateChatMemory(memoryId)
                : temporaryMemory;
    }

    private void addToMemory(ChatMessage chatMessage) {
        getMemory().add(chatMessage);
    }

    private List<ChatMessage> messagesToSend(Object memoryId) {
        return getMemory(memoryId).messages();
    }

    @Override
    public void onError(Throwable error) {
        if (errorHandler != null) {
            try {
                fireErrorReceived(error);
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
