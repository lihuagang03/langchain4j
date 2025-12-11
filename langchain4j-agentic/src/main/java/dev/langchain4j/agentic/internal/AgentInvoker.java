package dev.langchain4j.agentic.internal;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agentic.agent.AgentInvocationException;
import dev.langchain4j.agentic.agent.AgentRequest;
import dev.langchain4j.agentic.agent.AgentResponse;
import dev.langchain4j.agentic.agent.MissingArgumentException;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.invocation.LangChain4jManaged;
import dev.langchain4j.service.V;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Optional;

import static dev.langchain4j.agentic.internal.AgentUtil.argumentsFromMethod;

/**
 * 智能体调用者
 */
public interface AgentInvoker extends AgentSpecification {

    Logger LOG = LoggerFactory.getLogger(AgentInvoker.class);

    /**
     * 智能体的方法
     */
    Method method();

    /**
     * 智能体的卡片
     */
    String toCard();

    /**
     * 智能体调用参数
     * @param agenticScope 智能体自主范围
     */
    AgentInvocationArguments toInvocationArguments(AgenticScope agenticScope) throws MissingArgumentException;

    default Object invoke(AgenticScope agenticScope, Object agent, AgentInvocationArguments args) throws AgentInvocationException {
        try {
            // 在调用之前
            beforeInvocation(new AgentRequest(agenticScope, name(), args.namedArgs()));
        } catch (Exception e) {
            LOG.error("Before agent invocation listener for agent " + name() + " failed: " + e.getMessage(), e);
        }
        // 设置当前线程的智能体自主范围
        LangChain4jManaged.setCurrent(Map.of(AgenticScope.class, agenticScope));
        // 内部调用智能体的方法
        Object result = internalInvoke(agent, args);
        try {
            LangChain4jManaged.removeCurrent();
            // 在调用完成之后
            afterInvocation(new AgentResponse(agenticScope, name(), args.namedArgs(), result));
        } catch (Exception e) {
            LOG.error("After agent invocation listener for agent " + name() + " failed: " + e.getMessage(), e);
        }
        return result;
    }

    private Object internalInvoke(Object agent, AgentInvocationArguments args) {
        try {
            // 调用智能体的方法
            return method().invoke(agent, args.positionalArgs());
        } catch (Exception e) {
            throw new AgentInvocationException("Failed to invoke agent method: " + method(), e);
        }
    }

    /**
     * 来自方法的智能体调用者
     * @param spec 智能体规范
     * @param method 智能体的方法
     */
    static AgentInvoker fromMethod(AgentSpecification spec, Method method) {
        if (method.getDeclaringClass() == UntypedAgent.class) {
            return new UntypedAgentInvoker(method, spec);
        }

        return new MethodAgentInvoker(method, spec, argumentsFromMethod(method));
    }

    /**
     * 参数名称
     * @param parameter 参数对象
     */
    static String parameterName(Parameter parameter) {
        return optionalParameterName(parameter)
                .orElseThrow(() -> new IllegalArgumentException("Parameter name not specified and no @P or @V annotation present: " + parameter));
    }

    static Optional<String> optionalParameterName(Parameter parameter) {
        P p = parameter.getAnnotation(P.class);
        if (p != null) {
            return Optional.of(p.value());
        }
        V v = parameter.getAnnotation(V.class);
        if (v != null) {
            return Optional.of(v.value());
        }
        return parameter.isNamePresent() ? Optional.of(parameter.getName()) : java.util.Optional.empty();
    }
}
