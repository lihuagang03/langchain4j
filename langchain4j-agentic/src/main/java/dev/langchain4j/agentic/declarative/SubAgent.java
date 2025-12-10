package dev.langchain4j.agentic.declarative;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 子代理
 * 定义基于工作流的代理或主管代理的子代理。
 * Defines a sub-agent of a workflow-based or supervisor agent.
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface SubAgent {

    /**
     * 子代理的类
     * The class of the sub-agent.
     *
     * @return the class of the sub-agent
     */
    Class<?> type() default Object.class;

    /**
     * 用于存储代理调用结果的输出变量的键。
     * Key of the output variable that will be used to store the result of the agent's invocation.
     *
     * @return name of the output variable.
     */
    String outputKey() default "";

    /**
     * 参与定义该代理上下文的其他代理的名称。
     * Names of other agents participating in the definition of the context of this agent.
     *
     * @return array of names of other agents participating in the definition of the context of this agent.
     */
    String[] summarizedContext() default {};
}
