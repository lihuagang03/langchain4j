package dev.langchain4j.agentic.scope;

import dev.langchain4j.agentic.internal.AgentInvocation;
import dev.langchain4j.invocation.LangChain4jManaged;
import java.util.List;
import java.util.Map;

/**
 * 智能体自主范围
 * AgenticScope 类表示一个常见的环境，其中属于同一智能系统的代理可以共享它们的状态。
 * 它维护计算的状态，跟踪代理的调用，并提供方法以允许代理与共享状态进行交互。
 * The AgenticScope class represents a common environment where agents belonging to the same
 * agentic system can share their state.
 * It maintains the state of the computation, tracks agent invocations, and provides
 * methods to allow agents to interact with the shared state.
 * <p>
 * 代理可以注册他们的调用，并且交互的上下文会被存储以便稍后检索。
 * 该类还提供了读取和写入状态、管理代理调用以及将上下文作为对话检索的方法。
 * Agents can register their calls, and the context of interactions is stored for later retrieval.
 * The class also provides methods to read and write state, manage agent invocations, and retrieve
 * the context as a conversation.
 */
public interface AgenticScope extends LangChain4jManaged {

    /**
     * 聊天记忆ID
     */
    Object memoryId();

    // 状态管理

    void writeState(String key, Object value);

    void writeStates(Map<String, Object> newState);

    boolean hasState(String key);

    Object readState(String key);

    <T> T readState(String key, T defaultValue);

    Map<String, Object> state();

    /**
     * 上下文作为对话
     * @param agentNames 智能体名称列表
     */
    String contextAsConversation(String... agentNames);

    /**
     * 上下文作为对话
     * @param agents 智能体对象列表
     */
    String contextAsConversation(Object... agents);

    /**
     * 智能体调用列表
     * @param agentName 智能体名称
     */
    List<AgentInvocation> agentInvocations(String agentName);
}
