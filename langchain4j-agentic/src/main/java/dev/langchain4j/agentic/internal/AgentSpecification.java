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

    /**
     * 唯一的代理名称
     */
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

    /**
     * 调用前
     * @param request 代理请求
     */
    void beforeInvocation(AgentRequest request);

    /**
     * 调用后
     * @param response 代理响应
     */
    void afterInvocation(AgentResponse response);
}
