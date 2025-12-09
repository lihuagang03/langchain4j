package dev.langchain4j.agentic.internal;

import dev.langchain4j.agentic.agent.AgentRequest;
import dev.langchain4j.agentic.agent.AgentResponse;

/**
 * 智能体代理规格
 */
public interface AgentSpecification {

    /**
     * 代理名称
     */
    String name();

    String uniqueName();

    /**
     * 代理的描述
     */
    String description();

    /**
     * 输出的键
     */
    String outputKey();

    /**
     * 是否异步执行
     */
    boolean async();

    void beforeInvocation(AgentRequest request);

    void afterInvocation(AgentResponse response);
}
