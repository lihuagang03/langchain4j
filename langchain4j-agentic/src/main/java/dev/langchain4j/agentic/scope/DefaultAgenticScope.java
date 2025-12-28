package dev.langchain4j.agentic.scope;

import dev.langchain4j.Internal;
import dev.langchain4j.agentic.agent.AgentInvocationException;
import dev.langchain4j.agentic.agent.ChatMessagesAccess;
import dev.langchain4j.agentic.agent.ErrorContext;
import dev.langchain4j.agentic.agent.ErrorRecoveryResult;
import dev.langchain4j.agentic.internal.AgentInvocation;
import dev.langchain4j.agentic.internal.AgentInvocationArguments;
import dev.langchain4j.agentic.internal.AgentSpecification;
import dev.langchain4j.agentic.internal.AsyncResponse;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.service.memory.ChatMemoryAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 智能体自主范围的默认实现
 */
@Internal
public class DefaultAgenticScope implements AgenticScope {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultAgenticScope.class);

    /**
     * 智能体消息
     */
    public record AgentMessage(String agentName, String agentUniqueName, ChatMessage message) {}

    /**
     * 聊天记忆ID
     */
    private final Object memoryId;
    /**
     * 状态映射表
     */
    private final Map<String, Object> state = new ConcurrentHashMap<>();
    /**
     * 智能体名称到智能体调用列表的映射表
     */
    private final Map<String, List<AgentInvocation>> agentInvocations = new ConcurrentHashMap<>();
    /**
     * 智能体消息列表的上下文
     */
    private final List<AgentMessage> context = Collections.synchronizedList(new ArrayList<>());

    /**
     * 智能体ID到智能体对象的映射表
     */
    private final transient Map<String, Object> agents = new ConcurrentHashMap<>();

    /**
     * 默认的错误恢复策略（抛出异常）
     */
    private static final Function<ErrorContext, ErrorRecoveryResult> DEFAULT_ERROR_RECOVERY =
            errorContext -> ErrorRecoveryResult.throwException();

    /**
     * 错误处理程序
     */
    private transient Function<ErrorContext, ErrorRecoveryResult> errorHandler = DEFAULT_ERROR_RECOVERY;

    public enum Kind {
        /**
         * 临时的
         */
        EPHEMERAL,
        /**
         * 已注册
         */
        REGISTERED,
        /**
         * 持久化
         */
        PERSISTENT
    }

    /**
     * 类型
     */
    private final Kind kind;

    /**
     * 读写锁
     * This lock is used to ensure that the AgenticScope doesn't get concurrently modified when it is going to be persisted.
     * The internal data structures of the AgenticScope are all thread-safe, so they don't need to be guarded by a read lock
     * when accessed. In essence multiple changes are allowed at the same time, but it is not allowed to persist a
     * AgenticScope that is not in a frozen state. That's why the read lock is acquired for the first and a write lock
     * when the second happens.
     */
    private final transient ReadWriteLock lock;

    DefaultAgenticScope(Kind kind) {
        this(Utils.randomUUID(), kind);
    }

    DefaultAgenticScope(Object memoryId, Kind kind) {
        // UUID
        this.memoryId = memoryId;
        this.kind = kind;
        this.lock = (kind == Kind.PERSISTENT) ? new ReentrantReadWriteLock() : null;
    }

    @Override
    public Object memoryId() {
        return memoryId;
    }

    @Override
    public void writeState(String key, Object value) {
        withReadLock(() -> {
            if (value == null) {
                state.remove(key);
            } else {
                state.put(key, value);
            }
        });
    }

    @Override
    public void writeStates(Map<String, Object> newState) {
        withReadLock(() -> state.putAll(newState));
    }

    @Override
    public boolean hasState(String key) {
        Object value = state.get(key);
        if (value == null) {
            return false;
        }
        return value instanceof String s ? !s.isBlank() : true;
    }

    @Override
    public Object readState(String key) {
        return readStateBlocking(key, state.get(key));
    }

    @Override
    public <T> T readState(String key, T defaultValue) {
        return (T) readStateBlocking(key, state.getOrDefault(key, defaultValue));
    }

    /**
     * 读取状态，阻塞
     */
    private Object readStateBlocking(String key, Object state) {
        if (state instanceof AsyncResponse asyncResponse) {
            // 异步响应
            state = asyncResponse.blockingGet();
            writeState(key, state);
        }
        return state;
    }

    @Override
    public Map<String, Object> state() {
        return state;
    }

    public <T> T getOrCreateAgent(String agentId, Function<DefaultAgenticScope, T> agentFactory) {
        return (T) agents.computeIfAbsent(agentId, id -> agentFactory.apply(this));
    }

    /**
     * 注册智能体调用
     * @param agentSpec 智能体规范
     * @param agent 智能体对象
     * @param input 智能体调用参数
     * @param output 输出结果
     */
    public void registerAgentCall(AgentSpecification agentSpec, Object agent, AgentInvocationArguments input, Object output) {
        withReadLock(() -> {
            // 智能体调用
            agentInvocations.computeIfAbsent(agentSpec.name(), name -> new ArrayList<>())
                            .add(new AgentInvocation(agentSpec.name(), input.namedArgs(), output));
            // 注册上下文
            registerContext(agentSpec, agent, output);
        });
    }

    /**
     * 根调用已启动
     * @param registry 智能体自主范围的注册表
     */
    public void rootCallStarted(AgenticScopeRegistry registry) {
    }

    /**
     * 根调用已结束
     * @param registry 智能体自主范围的注册表
     */
    public void rootCallEnded(AgenticScopeRegistry registry) {
        // ensure that all pending async operations are completed before ending the root call
        state.replaceAll(this::readStateBlocking);

        if (kind == Kind.EPHEMERAL) {
            // Ephemeral agenticScope are for single-use and can be evicted immediately
            registry.evict(memoryId);
        } else if (kind == Kind.PERSISTENT) {
            flush(registry);
        }
    }

    private void flush(AgenticScopeRegistry registry) {
        lock.writeLock().lock();
        try {
            registry.update(this);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void registerContext(AgentSpecification agentSpec, Object agent, Object output) {
        // 聊天记忆
    	ChatMemory chatMemory = agent instanceof ChatMemoryAccess agentWithMemory ? agentWithMemory.getChatMemory(memoryId) : null;
    	if (chatMemory != null) {
            registerContextFromChatMemory(agentSpec, chatMemory);
    	} else if (output != null && agent instanceof ChatMessagesAccess chatMessagesAccess) {
            // 最新的用户消息
            context.add(new AgentMessage(agentSpec.name(), agentSpec.uniqueName(), chatMessagesAccess.lastUserMessage()));
            // 最新的AI消息
            context.add(new AgentMessage(agentSpec.name(), agentSpec.uniqueName(), AiMessage.aiMessage(output.toString())));
        }
    }

    private void registerContextFromChatMemory(AgentSpecification agentSpec, ChatMemory chatMemory) {
        // 智能体的聊天消息列表
        List<ChatMessage> agentMessages = chatMemory.messages();
        if (Utils.isNullOrEmpty(agentMessages)) {
            return;
        }

        // 最新的AI消息
        ChatMessage lastMessage = agentMessages.get(agentMessages.size() - 1);
        if (!(lastMessage instanceof AiMessage aiMessage)) {
            return;
        }

        for (int i = agentMessages.size() - 1; i >= 0; i--) {
        	if (agentMessages.get(i) instanceof UserMessage userMessage) {
                // 最新的用户消息
        		// Only add to the agenticScope's context the last UserMessage ...
        		context.add(new AgentMessage(agentSpec.name(), agentSpec.uniqueName(), userMessage));
                // 最新的AI消息
        		// ... and last AiMessage response, all other messages are local to the invoked agent internals
        		context.add(new AgentMessage(agentSpec.name(), agentSpec.uniqueName(), aiMessage));
                return;
        	}
        }
    }

    public List<AgentMessage> context() {
        return context;
    }

    @Override
    public String contextAsConversation(Object... agents) {
        // 智能体名称的过滤器
        Predicate<String> agentFilter = agents != null && agents.length > 0 ?
                Arrays.stream(agents).filter(AgentSpecification.class::isInstance).map(AgentSpecification.class::cast)
                        .map(AgentSpecification::name).toList()::contains :
                agent -> true;
        return contextAsConversation(agentFilter);
    }

    @Override
    public String contextAsConversation(String... agentNames) {
        // 智能体名称的过滤器
        Predicate<String> agentFilter = agentNames != null && agentNames.length > 0 ?
                List.of(agentNames)::contains :
                agent -> true;
        return contextAsConversation(agentFilter);
    }

    private String contextAsConversation(Predicate<String> agentFilter) {
        StringBuilder sb = new StringBuilder();
        for (AgentMessage agentMessage : context) {
            // 智能体名称的过滤器
            if (!agentFilter.test(agentMessage.agentName())) {
                continue;
            }
            // 聊天消息
            ChatMessage message = agentMessage.message();
            if (message instanceof UserMessage userMessage) {
                // 用户消息
                sb.append("User: \"").append(userMessage.singleText()).append("\"\n");
            } else if (message instanceof AiMessage aiMessage) {
                // AI消息
                sb.append(agentMessage.agentName()).append(" agent: \"").append(aiMessage.text()).append("\"\n");
            }
        }

        String contextAsConversation = sb.toString();
        // 智能体自主范围的上下文作为对话
        LOG.trace("AgenticScope context as conversation: '{}'", contextAsConversation);
        return contextAsConversation;
    }

    public List<AgentInvocation> agentInvocations(String agentName) {
        return agentInvocations.getOrDefault(agentName, List.of());
    }

    @Override
    public String toString() {
        return "AgenticScope{" +
                "memoryId='" + memoryId + '\'' +
                ", state=" + state +
                '}';
    }

    /**
     * 读取状态，读锁
     * @param action 行动线程
     */
    private void withReadLock(Runnable action) {
        if (kind == Kind.PERSISTENT) {
            lock.readLock().lock();
            try {
                action.run();
            } finally {
                lock.readLock().unlock();
            }
        } else {
            action.run();
        }
    }

    public DefaultAgenticScope withErrorHandler(Function<ErrorContext, ErrorRecoveryResult> errorHandler) {
        if (errorHandler != null) {
            this.errorHandler = errorHandler;
        }
        return this;
    }

    public ErrorRecoveryResult handleError(String agentName, AgentInvocationException exception) {
        return errorHandler.apply(new ErrorContext(agentName, this, exception));
    }
}
