package dev.langchain4j.service.tool;

import static dev.langchain4j.agent.tool.ToolSpecifications.toolSpecificationFrom;
import static dev.langchain4j.internal.Exceptions.runtime;
import static dev.langchain4j.internal.Utils.getAnnotatedMethod;
import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.Utils.isNullOrBlank;
import static dev.langchain4j.service.IllegalConfigurationException.illegalConfiguration;

import dev.langchain4j.Internal;
import dev.langchain4j.agent.tool.ReturnBehavior;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.exception.ToolArgumentsException;
import dev.langchain4j.internal.DefaultExecutorProvider;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.observability.api.AiServiceListenerRegistrar;
import dev.langchain4j.observability.api.event.AiServiceResponseReceivedEvent;
import dev.langchain4j.observability.api.event.ToolExecutedEvent;
import dev.langchain4j.service.IllegalConfigurationException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.function.Function;

/**
 * 工具服务
 */
@Internal
public class ToolService {

    /**
     * 默认的工具参数错误处理程序
     */
    private static final ToolArgumentsErrorHandler DEFAULT_TOOL_ARGUMENTS_ERROR_HANDLER = (error, context) -> {
        if (error instanceof RuntimeException re) {
            throw re;
        } else {
            throw new RuntimeException(error);
        }
    };
    /**
     * 默认的工具执行错误处理程序
     */
    private static final ToolExecutionErrorHandler DEFAULT_TOOL_EXECUTION_ERROR_HANDLER = (error, context) -> {
        String errorMessage = isNullOrBlank(error.getMessage()) ? error.getClass().getName() : error.getMessage();
        return ToolErrorHandlerResult.text(errorMessage);
    };

    /**
     * 工具规格列表
     */
    private final List<ToolSpecification> toolSpecifications = new ArrayList<>();
    /**
     * 工具名称到工具执行器的映射表
     */
    private final Map<String, ToolExecutor> toolExecutors = new HashMap<>();
    /**
     * 立即返回的工具列表
     */
    private final Set<String> immediateReturnTools = new HashSet<>();
    /**
     * 工具提供者
     */
    private ToolProvider toolProvider;
    /**
     * 工具执行器
     */
    private Executor executor;
    /**
     * 最大连续的工具调用次数
     */
    private int maxSequentialToolsInvocations = 100;
    /**
     * 工具参数错误处理程序
     */
    private ToolArgumentsErrorHandler argumentsErrorHandler;
    /**
     * 工具执行错误处理程序
     */
    private ToolExecutionErrorHandler executionErrorHandler;
    /**
     * 工具的幻觉策略
     */
    private Function<ToolExecutionRequest, ToolExecutionResultMessage> toolHallucinationStrategy =
            HallucinatedToolNameStrategy.THROW_EXCEPTION;

    public void hallucinatedToolNameStrategy(
            Function<ToolExecutionRequest, ToolExecutionResultMessage> toolHallucinationStrategy) {
        this.toolHallucinationStrategy = toolHallucinationStrategy;
    }

    public void toolProvider(ToolProvider toolProvider) {
        this.toolProvider = toolProvider;
    }

    public void tools(Map<ToolSpecification, ToolExecutor> tools) {
        tools.forEach((toolSpecification, toolExecutor) -> {
            toolSpecifications.add(toolSpecification);
            toolExecutors.put(toolSpecification.name(), toolExecutor);
        });
    }

    public void tools(Collection<Object> objectsWithTools) {
        // @Tool 的对象列表
        for (Object objectWithTool : objectsWithTools) {
            if (objectWithTool instanceof Class) {
                throw illegalConfiguration("Tool '%s' must be an object, not a class", objectWithTool);
            }

            for (Method method : objectWithTool.getClass().getDeclaredMethods()) {
                // @Tool
                getAnnotatedMethod(method, Tool.class)
                        .ifPresent(toolMethod -> processToolMethod(objectWithTool, toolMethod));
            }
        }
    }

    private void processToolMethod(Object object, Method method) {
        // 处理工具方法
        // 工具规格
        ToolSpecification toolSpecification = toolSpecificationFrom(method);
        if (toolExecutors.containsKey(toolSpecification.name())) {
            throw new IllegalConfigurationException("Duplicated definition for tool: " + toolSpecification.name());
        }
        toolSpecifications.add(toolSpecification);

        // 工具执行器
        ToolExecutor toolExecutor = createToolExecutor(object, method);
        toolExecutors.put(toolSpecification.name(), toolExecutor);

        if (method.getAnnotation(Tool.class).returnBehavior() == ReturnBehavior.IMMEDIATE) {
            // 立即返回的工具
            immediateReturnTools.add(toolSpecification.name());
        }
    }

    private static ToolExecutor createToolExecutor(Object object, Method method) {
        // 创建工具执行器
        return DefaultToolExecutor.builder()
                .object(object)
                .originalMethod(method)
                .methodToInvoke(method)
                .wrapToolArgumentsExceptions(true)
                .propagateToolExecutionExceptions(true)
                .build();
    }

    /**
     * @since 1.4.0
     */
    public void executeToolsConcurrently() {
        this.executor = defaultExecutor();
    }

    /**
     * @since 1.4.0
     */
    public void executeToolsConcurrently(Executor executor) {
        this.executor = getOrDefault(executor, ToolService::defaultExecutor);
    }

    private static Executor defaultExecutor() {
        // 默认的执行器服务
        return DefaultExecutorProvider.getDefaultExecutorService();
    }

    public void maxSequentialToolsInvocations(int maxSequentialToolsInvocations) {
        this.maxSequentialToolsInvocations = maxSequentialToolsInvocations;
    }

    /**
     * @since 1.4.0
     */
    public void argumentsErrorHandler(ToolArgumentsErrorHandler handler) {
        this.argumentsErrorHandler = handler;
    }

    /**
     * @since 1.4.0
     */
    public ToolArgumentsErrorHandler argumentsErrorHandler() {
        return getOrDefault(argumentsErrorHandler, DEFAULT_TOOL_ARGUMENTS_ERROR_HANDLER);
    }

    /**
     * @since 1.4.0
     */
    public void executionErrorHandler(ToolExecutionErrorHandler handler) {
        this.executionErrorHandler = handler;
    }

    /**
     * @since 1.4.0
     */
    public ToolExecutionErrorHandler executionErrorHandler() {
        return getOrDefault(executionErrorHandler, DEFAULT_TOOL_EXECUTION_ERROR_HANDLER);
    }

    /**
     * 创建工具服务的上下文
     * @param invocationContext AI服务调用的上下文
     * @param userMessage 用户消息
     * @return 工具服务的上下文
     */
    public ToolServiceContext createContext(InvocationContext invocationContext, UserMessage userMessage) {
        // 工具提供者
        if (this.toolProvider == null) {
            // 工具服务的上下文
            return this.toolSpecifications.isEmpty()
                    ? ToolServiceContext.Empty.INSTANCE
                    : new ToolServiceContext(this.toolSpecifications, this.toolExecutors);
        }

        // 工具规格列表
        List<ToolSpecification> toolsSpecs = new ArrayList<>(this.toolSpecifications);
        // 工具名称到工具执行器的映射表
        Map<String, ToolExecutor> toolExecs = new HashMap<>(this.toolExecutors);
        // 工具提供者的请求
        ToolProviderRequest toolProviderRequest = ToolProviderRequest.builder()
                .invocationContext(invocationContext)
                .userMessage(userMessage)
                .build();
        // 提供用于向大语言模型请求的工具
        ToolProviderResult toolProviderResult = toolProvider.provideTools(toolProviderRequest);
        if (toolProviderResult != null) {
            for (Map.Entry<ToolSpecification, ToolExecutor> entry :
                    toolProviderResult.tools().entrySet()) {
                if (toolExecs.putIfAbsent(entry.getKey().name(), entry.getValue()) == null) {
                    toolsSpecs.add(entry.getKey());
                } else {
                    throw new IllegalConfigurationException(
                            "Duplicated definition for tool: " + entry.getKey().name());
                }
            }
        }
        // 工具服务的上下文
        return new ToolServiceContext(toolsSpecs, toolExecs);
    }

    /**
     * 执行推理和工具循环
     */
    public ToolServiceResult executeInferenceAndToolsLoop(
            ChatResponse chatResponse,
            ChatRequestParameters parameters,
            List<ChatMessage> messages,
            ChatModel chatModel,
            ChatMemory chatMemory,
            InvocationContext invocationContext,
            Map<String, ToolExecutor> toolExecutors,
            boolean isReturnTypeResult,
            AiServiceListenerRegistrar aiServiceListenerRegistrar) {
        // 汇总的词元使用情况
        TokenUsage aggregateTokenUsage = chatResponse.metadata().tokenUsage();
        // 工具执行列表
        List<ToolExecution> toolExecutions = new ArrayList<>();
        // 中间的聊天响应列表
        List<ChatResponse> intermediateResponses = new ArrayList<>();

        int executionsLeft = maxSequentialToolsInvocations;
        while (true) {

            if (executionsLeft-- == 0) {
                throw runtime(
                        "Something is wrong, exceeded %s sequential tool executions", maxSequentialToolsInvocations);
            }

            // AI消息
            AiMessage aiMessage = chatResponse.aiMessage();

            if (chatMemory != null) {
                chatMemory.add(aiMessage);
            } else {
                messages = new ArrayList<>(messages);
                messages.add(aiMessage);
            }

            if (!aiMessage.hasToolExecutionRequests()) {
                break;
            }

            // 中间的聊天响应列表
            intermediateResponses.add(chatResponse);

            // 执行工具
            Map<ToolExecutionRequest, ToolExecutionResult> toolResults =
                    execute(aiMessage.toolExecutionRequests(), toolExecutors, invocationContext);

            // 立即的工具返回标志
            boolean immediateToolReturn = true;
            for (Map.Entry<ToolExecutionRequest, ToolExecutionResult> entry : toolResults.entrySet()) {
                ToolExecutionRequest request = entry.getKey();
                ToolExecutionResult result = entry.getValue();
                // 工具执行结果消息
                ToolExecutionResultMessage resultMessage =
                        ToolExecutionResultMessage.from(request, result.resultText());

                // 工具执行
                ToolExecution toolExecution =
                        ToolExecution.builder().request(request).result(result).build();
                toolExecutions.add(toolExecution);

                // 触发 工具执行事件
                aiServiceListenerRegistrar.fireEvent(ToolExecutedEvent.builder()
                        .invocationContext(invocationContext)
                        .request(request)
                        .resultText(toolExecution.result())
                        .build());

                // 结果消息
                if (chatMemory != null) {
                    chatMemory.add(resultMessage);
                } else {
                    messages.add(resultMessage);
                }

                if (immediateToolReturn) {
                    if (isImmediateTool(request.name())) {
                        if (!isReturnTypeResult) {
                            throw illegalConfiguration(
                                    "Tool '%s' with immediate return is not allowed on a AI service not returning Result.",
                                    request.name());
                        }
                    } else {
                        immediateToolReturn = false;
                    }
                }
            }

            if (immediateToolReturn) {
                // 最终聊天响应
                ChatResponse finalResponse = intermediateResponses.remove(intermediateResponses.size() - 1);
                // 工具服务结果
                return ToolServiceResult.builder()
                        .intermediateResponses(intermediateResponses)
                        .finalResponse(finalResponse)
                        .toolExecutions(toolExecutions)
                        .aggregateTokenUsage(aggregateTokenUsage)
                        .immediateToolReturn(true)
                        .build();
            }

            if (chatMemory != null) {
                messages = chatMemory.messages();
            }

            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(messages)
                    .parameters(parameters)
                    .build();

            // 这是与聊天模型交互的主要 API
            chatResponse = chatModel.chat(chatRequest);
            // 触发 AI服务响应已接收到事件
            fireResponseReceivedEvent(chatResponse, invocationContext, aiServiceListenerRegistrar);
            // 汇总的词元使用情况
            aggregateTokenUsage =
                    TokenUsage.sum(aggregateTokenUsage, chatResponse.metadata().tokenUsage());
        }

        // 工具服务结果
        return ToolServiceResult.builder()
                .intermediateResponses(intermediateResponses)
                .finalResponse(chatResponse)
                .toolExecutions(toolExecutions)
                .aggregateTokenUsage(aggregateTokenUsage)
                .build();
    }

    private void fireResponseReceivedEvent(
            ChatResponse chatResponse,
            InvocationContext invocationContext,
            AiServiceListenerRegistrar listenerRegistrar) {
        // 触发 AI服务响应已接收到事件
        listenerRegistrar.fireEvent(AiServiceResponseReceivedEvent.builder()
                .invocationContext(invocationContext)
                .response(chatResponse)
                .build());
    }

    private Map<ToolExecutionRequest, ToolExecutionResult> execute(
            List<ToolExecutionRequest> toolRequests,
            Map<String, ToolExecutor> toolExecutors,
            InvocationContext invocationContext) {
        if (executor != null && toolRequests.size() > 1) {
            // 并发地执行工具
            return executeConcurrently(toolRequests, toolExecutors, invocationContext);
        } else {
            // 顺序地执行工具
            // when there is only one tool to execute, it doesn't make sense to do it in a separate thread
            return executeSequentially(toolRequests, toolExecutors, invocationContext);
        }
    }

    private Map<ToolExecutionRequest, ToolExecutionResult> executeConcurrently(
            List<ToolExecutionRequest> toolRequests,
            Map<String, ToolExecutor> toolExecutors,
            InvocationContext invocationContext) {
        // 并发地执行工具
        Map<ToolExecutionRequest, CompletableFuture<ToolExecutionResult>> futures = new LinkedHashMap<>();

        for (ToolExecutionRequest toolRequest : toolRequests) {
            // 提供异步调用
            CompletableFuture<ToolExecutionResult> future = CompletableFuture.supplyAsync(
                    () -> {
                        // 工具执行器
                        ToolExecutor toolExecutor = toolExecutors.get(toolRequest.name());
                        if (toolExecutor == null) {
                            // 应用工具的幻觉策略
                            return applyToolHallucinationStrategy(toolRequest);
                        } else {
                            // 使用错误处理执行工具请求
                            return executeWithErrorHandling(
                                    toolRequest,
                                    toolExecutor,
                                    invocationContext,
                                    argumentsErrorHandler(),
                                    executionErrorHandler());
                        }
                    },
                    executor);
            futures.put(toolRequest, future);
        }

        Map<ToolExecutionRequest, ToolExecutionResult> results = new LinkedHashMap<>();
        for (Map.Entry<ToolExecutionRequest, CompletableFuture<ToolExecutionResult>> entry : futures.entrySet()) {
            try {
                results.put(entry.getKey(), entry.getValue().get());
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

        return results;
    }

    private Map<ToolExecutionRequest, ToolExecutionResult> executeSequentially(
            List<ToolExecutionRequest> toolRequests,
            Map<String, ToolExecutor> toolExecutors,
            InvocationContext invocationContext) {
        // 顺序地执行工具
        Map<ToolExecutionRequest, ToolExecutionResult> toolResults = new LinkedHashMap<>();
        for (ToolExecutionRequest toolRequest : toolRequests) {
            // 执行器
            ToolExecutor executor = toolExecutors.get(toolRequest.name());
            ToolExecutionResult toolResult;
            if (executor == null) {
                // 应用工具的幻觉策略
                toolResult = applyToolHallucinationStrategy(toolRequest);
            } else {
                // 使用错误处理执行工具请求
                toolResult = executeWithErrorHandling(
                        toolRequest, executor, invocationContext, argumentsErrorHandler(), executionErrorHandler());
            }
            toolResults.put(toolRequest, toolResult);
        }
        return toolResults;
    }

    public static ToolExecutionResult executeWithErrorHandling(
            ToolExecutionRequest toolRequest,
            ToolExecutor toolExecutor,
            InvocationContext invocationContext,
            ToolArgumentsErrorHandler argumentsErrorHandler,
            ToolExecutionErrorHandler executionErrorHandler) {
        // 使用错误处理执行工具请求
        try {
            // 使用上下文执行工具请求
            return toolExecutor.executeWithContext(toolRequest, invocationContext);
        } catch (Exception e) {
            // 工具错误的上下文
            ToolErrorContext errorContext = ToolErrorContext.builder()
                    .toolExecutionRequest(toolRequest)
                    .invocationContext(invocationContext)
                    .build();

            // 工具错误处理器的结果
            ToolErrorHandlerResult errorHandlerResult;
            if (e instanceof ToolArgumentsException) {
                errorHandlerResult = argumentsErrorHandler.handle(e.getCause(), errorContext);
            } else {
                errorHandlerResult = executionErrorHandler.handle(e.getCause(), errorContext);
            }

            // 工具执行结果
            return ToolExecutionResult.builder()
                    .isError(true)
                    .resultText(errorHandlerResult.text())
                    .build();
        }
    }

    public ToolExecutionResult applyToolHallucinationStrategy(ToolExecutionRequest toolRequest) {
        // 应用工具的幻觉策略
        ToolExecutionResultMessage toolResultMessage = toolHallucinationStrategy.apply(toolRequest);
        return ToolExecutionResult.builder()
                .resultText(toolResultMessage.text())
                .build();
    }

    public List<ToolSpecification> toolSpecifications() {
        return toolSpecifications;
    }

    public Map<String, ToolExecutor> toolExecutors() {
        return toolExecutors;
    }

    /**
     * @since 1.4.0
     */
    public Executor executor() {
        return executor;
    }

    public ToolProvider toolProvider() {
        return toolProvider;
    }

    public boolean isImmediateTool(String toolName) {
        return immediateReturnTools.contains(toolName);
    }
}
