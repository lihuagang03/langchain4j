package dev.langchain4j.agentic.agent;

import dev.langchain4j.agentic.internal.AgentSpecification;
import dev.langchain4j.agentic.internal.AgenticScopeOwner;
import dev.langchain4j.agentic.internal.UserMessageRecorder;
import dev.langchain4j.agentic.scope.DefaultAgenticScope;
import dev.langchain4j.service.AiServiceContext;
import dev.langchain4j.service.memory.ChatMemoryAccess;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * 智能体调用处理程序
 */
public class AgentInvocationHandler implements InvocationHandler {

    /**
     * AI服务上下文
     */
    private final AiServiceContext context;
    /**
     * 智能体构建者
     */
    private final AgentBuilder<?> builder;
    /**
     * 智能体对象
     */
    private final Object agent;
    /**
     * 用户消息记录者
     */
    private final UserMessageRecorder messageRecorder;
    /**
     * 是否依赖于智能体自主范围
     */
    private final boolean agenticScopeDependent;

    AgentInvocationHandler(
            AiServiceContext context,
            Object agent,
            AgentBuilder<?> builder,
            UserMessageRecorder messageRecorder,
            boolean agenticScopeDependent) {
        this.context = context;
        this.agent = agent;
        this.builder = builder;
        this.messageRecorder = messageRecorder;
        this.agenticScopeDependent = agenticScopeDependent;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
        // 聊天消息访问
        if (method.getDeclaringClass() == ChatMessagesAccess.class) {
            return switch (method.getName()) {
                case "lastUserMessage" -> messageRecorder.lastUserMessage();
                default ->
                    throw new UnsupportedOperationException(
                            "Unknown method on AgenticScopeOwner class : " + method.getName());
            };
        }

        // 智能体自主范围的持有者
        if (method.getDeclaringClass() == AgenticScopeOwner.class) {
            return switch (method.getName()) {
                case "withAgenticScope" ->
                    agenticScopeDependent
                            ? ((DefaultAgenticScope) args[0]).getOrCreateAgent(builder.agentId(), builder::build)
                            : proxy;
                case "registry" ->
                    throw new UnsupportedOperationException(
                            "AgenticScopeOwner's registry method can be used only on the root agent of an agentic system.");
                default ->
                    throw new UnsupportedOperationException(
                            "Unknown method on AgenticScopeOwner class : " + method.getName());
            };
        }

        // 聊天记忆访问
        if (method.getDeclaringClass() == ChatMemoryAccess.class) {
            return switch (method.getName()) {
                case "getChatMemory" ->
                    context.hasChatMemory() ? context.chatMemoryService.getChatMemory(args[0]) : null;
                case "evictChatMemory" ->
                    context.hasChatMemory() && context.chatMemoryService.evictChatMemory(args[0]) != null;
                default ->
                    throw new UnsupportedOperationException(
                            "Unknown method on ChatMemoryAccess class : " + method.getName());
            };
        }

        // 智能体规范
        if (method.getDeclaringClass() == AgentSpecification.class) {
            return switch (method.getName()) {
                case "name" -> builder.name;
                case "uniqueName" -> builder.uniqueName;
                case "description" -> builder.description;
                case "outputKey" -> builder.outputKey;
                case "async" -> builder.async;
                case "beforeInvocation" -> {
                    builder.beforeListener.accept((AgentRequest) args[0]);
                    yield null;
                }
                case "afterInvocation" -> {
                    builder.afterListener.accept((AgentResponse) args[0]);
                    yield null;
                }
                default ->
                    throw new UnsupportedOperationException(
                            "Unknown method on ChatMemoryAccess class : " + method.getName());
            };
        }

        // 调用智能体的方法
        return method.invoke(agent, args);
    }
}
