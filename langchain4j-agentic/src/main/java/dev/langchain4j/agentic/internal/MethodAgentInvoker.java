package dev.langchain4j.agentic.internal;

import dev.langchain4j.agentic.agent.AgentRequest;
import dev.langchain4j.agentic.agent.AgentResponse;
import dev.langchain4j.agentic.agent.MissingArgumentException;
import dev.langchain4j.agentic.scope.AgenticScope;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 方法智能体调用者
 * @param method 智能体的方法
 * @param agentSpecification 智能体规范
 * @param arguments 智能体参数列表
 */
public record MethodAgentInvoker(
        Method method, AgentSpecification agentSpecification, List<AgentUtil.AgentArgument> arguments)
        implements AgentInvoker {

    @Override
    public String name() {
        return agentSpecification.name();
    }

    @Override
    public String uniqueName() {
        return agentSpecification.uniqueName();
    }

    @Override
    public String description() {
        return agentSpecification.description();
    }

    @Override
    public String outputKey() {
        return agentSpecification.outputKey();
    }

    @Override
    public boolean async() {
        return agentSpecification.async();
    }

    @Override
    public void beforeInvocation(final AgentRequest request) {
        agentSpecification.beforeInvocation(request);
    }

    @Override
    public void afterInvocation(final AgentResponse response) {
        agentSpecification.afterInvocation(response);
    }

    @Override
    public String toCard() {
        // 参数名称列表
        List<String> agentArguments = arguments.stream()
                .map(AgentUtil.AgentArgument::name)
                .filter(a -> !a.equals("@MemoryId"))
                .toList();
        // 智能体的唯一名称: 描述, 参数名称列表
        return "{" + uniqueName() + ": " + description() + ", " + agentArguments + "}";
    }

    @Override
    public AgentInvocationArguments toInvocationArguments(AgenticScope agenticScope) throws MissingArgumentException {
        return AgentUtil.agentInvocationArguments(agenticScope, arguments);
    }
}
