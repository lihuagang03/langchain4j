package dev.langchain4j.service;

import static dev.langchain4j.spi.ServiceHelper.loadFactory;

import dev.langchain4j.Internal;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.moderation.ModerationModel;
import dev.langchain4j.observability.api.AiServiceListenerRegistrar;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.service.guardrail.GuardrailService;
import dev.langchain4j.service.memory.ChatMemoryService;
import dev.langchain4j.service.tool.ToolService;
import dev.langchain4j.spi.services.AiServiceContextFactory;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * AI服务的上下文
 */
@Internal
public class AiServiceContext {

    /**
     * 默认的消息提供者
     */
    private static final Function<Object, Optional<String>> DEFAULT_MESSAGE_PROVIDER = x -> Optional.empty();

    /**
     * AI服务类
     */
    public final Class<?> aiServiceClass;
    /**
     * AI服务监视器的注册器
     */
    public final AiServiceListenerRegistrar eventListenerRegistrar = AiServiceListenerRegistrar.newInstance();

    /**
     * 聊天模型
     */
    public ChatModel chatModel;
    /**
     * 流式聊天模型
     */
    public StreamingChatModel streamingChatModel;

    /**
     * 聊天记忆服务
     */
    public ChatMemoryService chatMemoryService;

    /**
     * 工具服务
     */
    public ToolService toolService = new ToolService();

    /**
     * 护栏服务构建者
     */
    public final GuardrailService.Builder guardrailServiceBuilder;
    /**
     * 护栏服务的引用
     */
    private final AtomicReference<GuardrailService> guardrailService = new AtomicReference<>();

    /**
     * 审查模型
     */
    public ModerationModel moderationModel;

    /**
     * 检索增强器
     */
    public RetrievalAugmentor retrievalAugmentor;

    /**
     * 系统提示提供者
     */
    public Function<Object, Optional<String>> systemMessageProvider = DEFAULT_MESSAGE_PROVIDER;

    /**
     * 聊天请求转换器
     */
    public BiFunction<ChatRequest, Object, ChatRequest> chatRequestTransformer = (req, memId) -> req;

    protected AiServiceContext(Class<?> aiServiceClass) {
        this.aiServiceClass = aiServiceClass;
        this.guardrailServiceBuilder = GuardrailService.builder(aiServiceClass);
    }

    private static class FactoryHolder {
        private static final AiServiceContextFactory contextFactory = loadFactory(AiServiceContextFactory.class);
    }

    public static AiServiceContext create(Class<?> aiServiceClass) {
        return FactoryHolder.contextFactory != null
                ? FactoryHolder.contextFactory.create(aiServiceClass)
                : new AiServiceContext(aiServiceClass);
    }

    public boolean hasChatMemory() {
        return chatMemoryService != null;
    }

    public void initChatMemories(ChatMemory chatMemory) {
        chatMemoryService = new ChatMemoryService(chatMemory);
    }

    public void initChatMemories(ChatMemoryProvider chatMemoryProvider) {
        chatMemoryService = new ChatMemoryService(chatMemoryProvider);
    }

    public GuardrailService guardrailService() {
        return this.guardrailService.updateAndGet(
                service -> (service != null) ? service : guardrailServiceBuilder.build());
    }
}
