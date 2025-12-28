package dev.langchain4j.agentic.a2a;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.declarative.A2AClientAgent;
import dev.langchain4j.agentic.internal.A2AClientBuilder;
import dev.langchain4j.agentic.internal.A2AService;
import dev.langchain4j.agentic.internal.AgentExecutor;
import dev.langchain4j.agentic.internal.AgentInvoker;
import dev.langchain4j.agentic.internal.AgentSpecification;
import java.lang.reflect.Method;
import java.util.Optional;

import static dev.langchain4j.internal.Utils.getAnnotatedMethod;

/**
 * 智能体到智能体的服务
 */
public class DefaultA2AService implements A2AService {

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public <T> A2AClientBuilder<T> a2aBuilder(final String a2aServerUrl, final Class<T> agentServiceClass) {
        // 智能体到智能体的客户端构建者的默认实现
        return new DefaultA2AClientBuilder<>(a2aServerUrl, agentServiceClass);
    }

    @Override
    public Optional<AgentExecutor> methodToAgentExecutor(final AgentSpecification agent, final Method method) {
        // A2A客户端规范
        if (agent instanceof A2AClientSpecification a2aAgent) {
            // @智能体
            // 智能体到智能体的客户端智能体调用者
            // 智能体执行器
            Optional<AgentExecutor> a2aAgentExecutor = getAnnotatedMethod(method, Agent.class)
                    .map(agentMethod -> new AgentExecutor(new A2AClientAgentInvoker(a2aAgent, agentMethod), a2aAgent));
            if (a2aAgentExecutor.isEmpty()) {
                // @智能体到智能体的客户端智能体
                a2aAgentExecutor = getAnnotatedMethod(method, A2AClientAgent.class)
                        .map(agentMethod -> new AgentExecutor(new A2AClientAgentInvoker(a2aAgent, agentMethod), a2aAgent));
            }
            return a2aAgentExecutor;
        }
        // @智能体
        // 智能体调用者
        return getAnnotatedMethod(method, Agent.class)
                .map(agentMethod -> new AgentExecutor(AgentInvoker.fromMethod(agent, agentMethod), agent));    }
}
