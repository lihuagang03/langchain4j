package dev.langchain4j.agentic.declarative;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 条件代理
 * 将某个方法标记为条件代理的定义，通常用于根据其激活条件的验证将代理工作流程引导至一个或多个子代理。
 * 每个子代理都有其自己的激活条件，一个使用 @ActivationCondition 注解的静态方法，用于确定何时应调用它。
 * Marks a method as a definition of a conditional agent, generally used to route the agentic workflow toward
 * one or more sub-agents according to the verification of their activation conditions.
 * Each sub-agent has its own activation condition, a static method annotated with {@link ActivationCondition} that
 * determines when it should be invoked.
 * <p>
 * Example:
 * <pre>
 * {@code
 *     public interface ExpertsAgent {
 *
 *         @ConditionalAgent(outputKey = "response", subAgents = {
 *                 @SubAgent(type = MedicalExpert.class, outputKey = "response"),
 *                 @SubAgent(type = TechnicalExpert.class, outputKey = "response"),
 *                 @SubAgent(type = LegalExpert.class, outputKey = "response")
 *         })
 *         String askExpert(@V("request") String request);
 *
 *         @ActivationCondition(MedicalExpert.class)
 *         static boolean activateMedical(@V("category") RequestCategory category) {
 *             return category == RequestCategory.MEDICAL;
 *         }
 *
 *         @ActivationCondition(TechnicalExpert.class)
 *         static boolean activateTechnical(@V("category") RequestCategory category) {
 *             return category == RequestCategory.TECHNICAL;
 *         }
 *
 *         @ActivationCondition(LegalExpert.class)
 *         static boolean activateLegal(AgenticScope agenticScope) {
 *             return agenticScope.readState("category", RequestCategory.UNKNOWN) == RequestCategory.LEGAL;
 *         }
 *     }
 * }
 * </pre>
 */
@Retention(RUNTIME)
@Target({METHOD})
public @interface ConditionalAgent {

    /**
     * 代理名称
     * Name of the agent. If not provided, method name will be used.
     *
     * @return name of the agent.
     */
    String name() default "";

    /**
     * 代理的描述
     * Description of the agent.
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
     * 可以由此代理条件激活的子代理。
     * Sub-agents that can be conditionally activated by this agent.
     *
     * @return array of sub-agents.
     */
    SubAgent[] subAgents();
}
