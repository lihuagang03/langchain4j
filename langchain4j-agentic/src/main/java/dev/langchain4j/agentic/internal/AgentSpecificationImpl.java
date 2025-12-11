package dev.langchain4j.agentic.internal;

import dev.langchain4j.agentic.agent.AgentRequest;
import dev.langchain4j.agentic.agent.AgentResponse;
import java.util.function.Consumer;

/**
 * 智能体规范实现
 * @param name 智能体名称
 * @param uniqueName 唯一的智能体名称
 * @param description 智能体的描述
 * @param outputKey 输出变量的键
 * @param async 是否异步调用
 * @param invocationListener 调用监视器
 * @param completionListener 完成监视器
 */
public record AgentSpecificationImpl(
        String name,
        String uniqueName,
        String description,
        String outputKey,
        boolean async,
        Consumer<AgentRequest> invocationListener,
        Consumer<AgentResponse> completionListener)
        implements AgentSpecification {

    @Override
    public void beforeInvocation(AgentRequest request) {
        invocationListener.accept(request);
    }

    @Override
    public void afterInvocation(AgentResponse response) {
        completionListener.accept(response);
    }
}
