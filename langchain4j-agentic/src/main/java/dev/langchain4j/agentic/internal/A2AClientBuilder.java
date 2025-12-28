package dev.langchain4j.agentic.internal;

import dev.langchain4j.agentic.agent.AgentRequest;
import dev.langchain4j.agentic.agent.AgentResponse;
import java.util.function.Consumer;

/**
 * 智能体到智能体的客户端构建者
 */
public interface A2AClientBuilder<T> {

    /**
     * 输入变量的键列表
     * @param inputKeys 输入变量的键列表
     */
    A2AClientBuilder<T> inputKeys(String... inputKeys);

    /**
     * 输出变量的键
     * @param outputKey 输出变量的键
     */
    A2AClientBuilder<T> outputKey(String outputKey);

    A2AClientBuilder<T> async(boolean async);

    /**
     * 在智能体调用之前
     * @param invocationListener 调用监听器
     */
    A2AClientBuilder<T> beforeAgentInvocation(Consumer<AgentRequest> invocationListener);

    /**
     * 在智能体调用之后
     * @param completionListener 调用监听器
     */
    A2AClientBuilder<T> afterAgentInvocation(Consumer<AgentResponse> completionListener);

    T build();
}
