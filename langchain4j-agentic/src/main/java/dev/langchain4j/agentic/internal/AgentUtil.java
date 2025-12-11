package dev.langchain4j.agentic.internal;

import static dev.langchain4j.internal.Utils.getAnnotatedMethod;
import static dev.langchain4j.internal.Utils.isNullOrBlank;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.agent.MissingArgumentException;
import dev.langchain4j.agentic.declarative.LoopCounter;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.service.MemoryId;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * 智能体辅助操作
 */
public class AgentUtil {

    /**
     * 聊天记忆ID
     */
    public static final String MEMORY_ID_ARG_NAME = "@MemoryId";
    /**
     * 智能体自主范围
     */
    public static final String AGENTIC_SCOPE_ARG_NAME = "@AgenticScope";
    /**
     * 循环计数器
     */
    public static final String LOOP_COUNTER_ARG_NAME = "@LoopCounter";

    /**
     * 智能体的计数器
     */
    private static final AtomicInteger AGENT_COUNTER = new AtomicInteger(0);

    private AgentUtil() {}

    /**
     * 智能体参数
     * @param type 参数类型
     * @param name 参数名称
     */
    public record AgentArgument(Class<?> type, String name) {}

    /**
     * 唯一的智能体名称
     * @param agentName 智能体名称
     */
    public static String uniqueAgentName(String agentName) {
        return agentName + "$" + AGENT_COUNTER.incrementAndGet();
    }

    public static List<AgentExecutor> agentsToExecutors(Object... agents) {
        return Stream.of(agents).map(AgentUtil::agentToExecutor).toList();
    }

    /**
     * 智能体执行器
     * @param agent 智能体对象
     */
    public static AgentExecutor agentToExecutor(Object agent) {
        if (agent instanceof Class c) {
            agent = AgenticServices.agentBuilder(c).build();
        }
        return agent instanceof AgentSpecification agentSpecification
                ? agentToExecutor(agentSpecification)
                : nonAiAgentToExecutor(agent);
    }

    private static AgentExecutor nonAiAgentToExecutor(Object agent) {
        // 自主智能体的方法
        Method agenticMethod = validateAgentClass(agent.getClass());
        // @智能体
        Agent annotation = agenticMethod.getAnnotation(Agent.class);
        // 智能体名称
        String name = isNullOrBlank(annotation.name()) ? agenticMethod.getName() : annotation.name();
        // 唯一的智能体名称
        String uniqueName = uniqueAgentName(name);
        // 智能体的描述
        String description = isNullOrBlank(annotation.description()) ? annotation.value() : annotation.description();
        // 智能体调用者
        AgentInvoker agentInvoker = agent instanceof AgentSpecsProvider spec
                ? new MethodAgentInvoker(
                        agenticMethod,
                        new AgentSpecificationImpl(
                                name, uniqueName, spec.description(), spec.outputKey(), spec.async(), x -> {}, x -> {}),
                        List.of(new AgentArgument(agenticMethod.getParameterTypes()[0], spec.inputKey())))
                : AgentInvoker.fromMethod(
                        new AgentSpecificationImpl(
                                name,
                                uniqueName,
                                description,
                                annotation.outputKey(),
                                annotation.async(),
                                x -> {},
                                x -> {}),
                        agenticMethod);
        // 智能体执行器
        return new AgentExecutor(agentInvoker, agent);
    }

    public static AgentExecutor agentToExecutor(AgentSpecification agent) {
        for (Method method : agent.getClass().getMethods()) {
            // A2A服务
            Optional<AgentExecutor> executor = A2AService.get().isPresent()
                    ? A2AService.get().methodToAgentExecutor(agent, method)
                    : methodToAgentExecutor(agent, method);
            if (executor.isPresent()) {
                return executor.get();
            }
        }
        throw new IllegalArgumentException("Agent executor not found");
    }

    public static Optional<Method> getAnnotatedMethodOnClass(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Arrays.stream(clazz.getMethods())
                .filter(m -> m.isAnnotationPresent(annotation))
                .findFirst();
    }

    private static Optional<AgentExecutor> methodToAgentExecutor(AgentSpecification agent, Method method) {
        // @智能体 注解的方法
        return getAnnotatedMethod(method, Agent.class)
                .map(agentMethod -> new AgentExecutor(AgentInvoker.fromMethod(agent, agentMethod), agent));
    }

    public static List<AgentArgument> argumentsFromMethod(Method method) {
        return Stream.of(method.getParameters())
                .map(p -> new AgentArgument(p.getType(), parameterName(p)))
                .toList();
    }

    /**
     * 参数名称
     * @param p 参数对象
     */
    private static String parameterName(Parameter p) {
        if (p.getAnnotation(MemoryId.class) != null) {
            return MEMORY_ID_ARG_NAME;
        }
        if (p.getAnnotation(LoopCounter.class) != null) {
            return LOOP_COUNTER_ARG_NAME;
        }
        if (AgenticScope.class.isAssignableFrom(p.getType())) {
            return AGENTIC_SCOPE_ARG_NAME;
        }
        return AgentInvoker.parameterName(p);
    }

    public static AgentInvocationArguments agentInvocationArguments(
            AgenticScope agenticScope, List<AgentArgument> agentArguments) throws MissingArgumentException {
        // 智能体调用参数
        return agentInvocationArguments(agenticScope, agentArguments, Map.of());
    }

    public static AgentInvocationArguments agentInvocationArguments(
            AgenticScope agenticScope, List<AgentArgument> agentArguments, Map<String, Object> additionalArgs)
            throws MissingArgumentException {
        Map<String, Object> namedArgs = new HashMap<>();
        Object[] positionalArgs = new Object[agentArguments.size()];

        int i = 0;
        for (AgentArgument arg : agentArguments) {
            // 参数名称
            String argName = arg.name();
            // 聊天记忆ID
            if (argName.equals(MEMORY_ID_ARG_NAME)) {
                positionalArgs[i++] = agenticScope.memoryId();
                continue;
            }
            // 智能体自主范围
            if (argName.equals(AGENTIC_SCOPE_ARG_NAME)) {
                positionalArgs[i++] = agenticScope;
                continue;
            }
            if (additionalArgs.containsKey(argName)) {
                positionalArgs[i++] = additionalArgs.get(argName);
                continue;
            }
            Object argValue = argumentFromAgenticScope(agenticScope, arg.type(), argName);
            positionalArgs[i++] = argValue;
            namedArgs.put(argName, argValue);
        }
        return new AgentInvocationArguments(namedArgs, positionalArgs);
    }

    public static Object argumentFromAgenticScope(AgenticScope agenticScope, Class<?> argType, String argName) {
        Object argValue = agenticScope.readState(argName);
        if (argValue == null) {
            throw new MissingArgumentException(argName);
        }
        Object parsedArgument = parseArgument(argValue, argType);
        if (argValue != parsedArgument) {
            agenticScope.writeState(argName, parsedArgument);
        }
        return parsedArgument;
    }

    static Object parseArgument(Object argValue, Class<?> type) {
        if (argValue instanceof String s) {
            return switch (type.getName()) {
                case "java.lang.String", "java.lang.Object" -> s;
                case "int", "java.lang.Integer" -> Integer.parseInt(s);
                case "long", "java.lang.Long" -> Long.parseLong(s);
                case "double", "java.lang.Double" -> Double.parseDouble(s);
                case "float", "java.lang.Float" -> Float.parseFloat(s);
                case "boolean", "java.lang.Boolean" -> Boolean.parseBoolean(s);
                default -> throw new IllegalArgumentException("Unsupported type: " + type);
            };
        }
        return argValue;
    }

    public static Method validateAgentClass(Class<?> agentServiceClass) {
        return validateAgentClass(agentServiceClass, true);
    }

    public static Method validateAgentClass(Class<?> agentServiceClass, boolean failOnMissingAnnotation) {
        Method agentMethod = null;
        for (Method method : agentServiceClass.getMethods()) {
            if (method.isAnnotationPresent(Agent.class)) {
                if (agentMethod != null) {
                    throw new IllegalArgumentException(
                            "Multiple agent methods found in class: " + agentServiceClass.getName());
                }
                agentMethod = method;
            }
        }
        if (agentMethod == null && failOnMissingAnnotation) {
            throw new IllegalArgumentException("No agent method found in class: " + agentServiceClass.getName());
        }
        return agentMethod;
    }
}
