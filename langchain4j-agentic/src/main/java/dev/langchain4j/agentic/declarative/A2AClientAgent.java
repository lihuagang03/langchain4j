package dev.langchain4j.agentic.declarative;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * A2A客户端代理
 * 将方法标记为 A2A 客户端代理。
 * Marks a method as an A2A client agent.
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface A2AClientAgent {

    /**
     * 将发送请求的 A2A 服务器的 URL。
     * URL of the A2A server to which the requests will be sent.
     *
     * @return URL of the A2A server.
     */
    String a2aServerUrl();

    /**
     * 代理名称。
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
     * 代理的描述
     * Description of the agent. This is an alias of the {@code value} attribute, and it is possible to use either.
     * It should be clear and descriptive to allow language model to understand the agent's purpose and its intended use.
     *
     * @return description of the agent.
     */
    String description() default "";

    /**
     * 用于存储代理调用结果的输出变量的键。
     * Key of the output variable that will be used to store the result of the agent's invocation.
     *
     * @return name of the output variable.
     */
    String outputKey() default "";

    /**
     * 如果为真，该代理将以异步方式被调用，从而允许工作流在不等待代理结果的情况下继续进行。
     * If true, the agent will be invoked in an asynchronous manner, allowing the workflow to continue without waiting for the agent's result.
     *
     * @return true if the agent should be invoked in an asynchronous manner, false otherwise.
     */
    boolean async() default false;
}
