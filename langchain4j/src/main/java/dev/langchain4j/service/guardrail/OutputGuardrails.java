package dev.langchain4j.service.guardrail;

import dev.langchain4j.guardrail.OutputGuardrail;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 输出护栏类
 * 使用声明式 AiServices 方法对模型输出应用安全控制的注释。
 * An annotation to apply guardrails to the output of the model using the declarative {@link dev.langchain4j.service.AiServices AiServices}
 * approach.
 * <p>
 *     输出护栏是一条应用于模型输出的规则，旨在确保输出安全并符合特定期望。
 *     Am output guardrail is a rule that is applied to the output of the model to ensure that the output is safe and meets
 *     certain expectations.
 * </p>
 * <p>
 *     当验证失败时，结果可以指示请求是否应按原样重试，或提供一个重新提示消息以附加到提示中。
 *     When a validation fails, the result can indicate whether the request should be retried as-is, or to provide a
 *     {@code reprompt} message to append to the prompt.
 * </p>
 * <p>
 *     在重新提示的情况下，重新提示消息会被添加到大语言模型的上下文中，然后请求会被重试。
 *     In the case of re-prompting, the reprompt message is added to the LLM context and the request is then retried.
 * </p>
 * <p>
 *     如果注解出现在一个类上，保护措施将应用于该类的所有方法。
 *     If the annotation is present on a class, the guardrails will be applied to all the methods of the class.
 * </p>
 * <p>
 *     当应用多个防护措施时，防护措施的顺序很重要，因为防护措施会按照列出的顺序应用。
 *     When several guardrails are applied, the order of the guardrails is important, as the guardrails are applied in
 *     the order they are listed.
 * </p>
 * <p>
 *     当应用多个输出防护措施时，如果任何一个防护措施要求重试或重新提示，那么所有防护措施都会重新应用于新的响应。
 *     When several {@link OutputGuardrail}s are applied, if any guardrail forces a retry or reprompt, then all of the
 *     guardrails will be re-applied to the new response.
 * </p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface OutputGuardrails {
    /**
     * 应用于模型输出的有序防护措施列表。
     * The ordered list of guardrails to apply to the output of the model.
     * <p>
     *     类的顺序很重要，因为防护栏是按照它们列出的顺序应用的。
     *     防护栏在列表中不能重复出现。
     *     The order of the classes is important as the guardrails are applied in the order they are listed.
     *     Guardrails can not be present twice in the list.
     * </p>
     */
    Class<? extends OutputGuardrail>[] value();

    /**
     * 当输出防护措施强制重试或重新提示时，执行的最大重试次数。
     * The maximum number of retries to perform when an output guardrail forces a retry or reprompt.
     * <p>
     *     设置为0以禁用重试
     *     Set to {@code 0} to disable retries
     * </p>
     * @see dev.langchain4j.guardrail.config.OutputGuardrailsConfig#maxRetries()
     */
    int maxRetries() default dev.langchain4j.guardrail.config.OutputGuardrailsConfig.MAX_RETRIES_DEFAULT;
}
