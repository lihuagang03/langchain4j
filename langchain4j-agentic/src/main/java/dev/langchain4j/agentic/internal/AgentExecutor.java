package dev.langchain4j.agentic.internal;

import dev.langchain4j.agentic.agent.AgentInvocationException;
import dev.langchain4j.agentic.agent.ErrorRecoveryResult;
import dev.langchain4j.agentic.scope.DefaultAgenticScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 智能体执行器
 * @param agentInvoker 智能体调用者
 * @param agent 智能体对象
 */
public record AgentExecutor(AgentInvoker agentInvoker, Object agent) {

    private static final Logger LOG = LoggerFactory.getLogger(AgentExecutor.class);

    public Object execute(DefaultAgenticScope agenticScope) {
        return execute(agenticScope, agentInvoker.async());
    }

    public Object syncExecute(DefaultAgenticScope agenticScope) {
        if (agentInvoker.async()) {
            LOG.info("Executing '{}' agent in a sync way even if declared as async", agentInvoker.name());
        }
        return execute(agenticScope, false);
    }

    private Object execute(DefaultAgenticScope agenticScope, boolean async) {
        // 调用的智能体对象
        Object invokedAgent = (agent instanceof AgenticScopeOwner co ? co.withAgenticScope(agenticScope) : agent);
        return internalExecute(agenticScope, invokedAgent, async);
    }

    private Object handleAgentFailure(
            AgentInvocationException e, DefaultAgenticScope agenticScope, Object invokedAgent) {
        ErrorRecoveryResult recoveryResult = agenticScope.handleError(agentInvoker.name(), e);
        return switch (recoveryResult.type()) {
            case THROW_EXCEPTION -> throw e;
            case RETRY -> internalExecute(agenticScope, invokedAgent, false);
            case RETURN_RESULT -> recoveryResult.result();
        };
    }

    private Object internalExecute(DefaultAgenticScope agenticScope, Object invokedAgent, boolean async) {
        try {
            // 智能体调用参数列表
            AgentInvocationArguments args = agentInvoker.toInvocationArguments(agenticScope);
            // 调用智能体
            // 智能体调用者
            Object response = async
                    ? new AsyncResponse<>(() -> {
                        try {
                            // 异步地调用
                            return agentInvoker.invoke(agenticScope, invokedAgent, args);
                        } catch (AgentInvocationException e) {
                            return handleAgentFailure(e, agenticScope, invokedAgent);
                        }
                    })
                    : agentInvoker.invoke(agenticScope, invokedAgent, args);
            // 输出变量的键
            String outputKey = agentInvoker.outputKey();
            if (outputKey != null && !outputKey.isBlank()) {
                // 响应结果写入状态
                agenticScope.writeState(outputKey, response);
            }
            // 注册智能体调用
            agenticScope.registerAgentCall(agentInvoker, invokedAgent, args, response);
            return response;
        } catch (AgentInvocationException e) {
            return handleAgentFailure(e, agenticScope, invokedAgent);
        }
    }
}
