package dev.langchain4j.agentic.internal;

import dev.langchain4j.agentic.agent.AgentRequest;
import dev.langchain4j.agentic.agent.AgentResponse;

/**
 * 智能体规范
 */
public interface AgentSpecification {

    /**
     * 智能体名称
     */
    String name();

    /**
     * 唯一的智能体名称
     */
    String uniqueName();

    /**
     * 智能体的描述
     */
    String description();

    /**
     * 输出变量的键
     */
    String outputKey();

    /**
     * 是否异步调用
     */
    boolean async();

    /**
     * 在调用之前
     * @param request 智能体请求
     */
    void beforeInvocation(AgentRequest request);

    /**
     * 在调用完成之后
     * @param response 智能体响应
     */
    void afterInvocation(AgentResponse response);
}
