package dev.langchain4j.agentic;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 智能体
 * 用 @Agent 注解的 Java 方法被视为其他智能体可以调用的智能体。
 * Java methods annotated with {@code @Agent} are considered agents that other agents can invoke.
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface Agent {

    /**
     * 智能体名称
     * 如果未提供，将使用方法名称。
     * Name of the agent. If not provided, method name will be used.
     *
     * @return name of the agent.
     */
    String name() default "";

    /**
     * Description of the agent. This is an alias of the {@code description} attribute, and it is possible to use either.
     * It should be clear and descriptive to allow language model to understand the agent's purpose and its intended use.
     *
     * @return description of the agent.
     */
    String value() default "";

    /**
     * 智能体的描述
     * 描述应清晰且具有说明性，以便语言模型理解智能体的用途及其预期使用方式。
     * Description of the agent. This is an alias of the {@code value} attribute, and it is possible to use either.
     * It should be clear and descriptive to allow language model to understand the agent's purpose and its intended use.
     *
     * @return description of the agent.
     */
    String description() default "";

    /**
     * 输出变量的键，将用于存储智能体调用的结果。
     * Key of the output variable that will be used to store the result of the agent's invocation.
     *
     * @return name of the output variable.
     */
    String outputKey() default "";

    /**
     * If true, the agent will be invoked in an asynchronous manner, allowing the workflow to continue without waiting for the agent's result.
     *
     * @return true if the agent should be invoked in an asynchronous manner, false otherwise.
     */
    boolean async() default false;
}
