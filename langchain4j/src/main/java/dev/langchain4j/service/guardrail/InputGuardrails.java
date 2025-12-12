package dev.langchain4j.service.guardrail;

import dev.langchain4j.guardrail.InputGuardrail;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 输入护栏类
 * 使用声明式 AiServices 方法对模型输入应用输入防护的注解。
 * An annotation to apply input guardrails to the input of the model using the declarative {@link dev.langchain4j.service.AiServices AiServices} approach.
 * <p>
 *     输入护栏是一种应用于模型输入（本质上是用户消息）的规则，用于确保输入是安全的并符合模型的期望。
 *     它不能替代内容审核模型，但可以用于增加额外的检查（例如提示注入等）。
 *     An input guardrail is a rule that is applied to the input of the model (essentially the user message) to ensure
 *     that the input is safe and meets the expectations of the model. It does not replace a moderation model, but it can
 *     be used to add additional checks (i.e. prompt injection, etc).
 * </p>
 * <p>
 *     与输出防护措施不同，输入防护措施不支持重试或重新提示。
 *     失败会直接传递给调用者，并封装为 GuardrailException。
 *     Unlike for output guardrails, the input guardrails do not support retry or reprompt. The failure is passed directly
 *     to the caller, wrapped into a {@link dev.langchain4j.guardrail.GuardrailException GuardrailException}.
 * </p>
 * <p>
 *     如果注解出现在一个类上，保护措施将应用于该类的所有方法。
 *     If the annotation is present on a class, the guardrails will be applied to all the methods of the class.
 * </p>
 * <p>
 *     当应用多个防护措施时，防护措施的顺序很重要，因为防护措施会按照列出的顺序应用。
 *     When several guardrails are applied, the order of the guardrails is important, as the guardrails are applied in the order
 *     they are listed.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface InputGuardrails {
    /**
     * 应用于模型输入的 InputGuardrails 的有序列表。
     * The ordered list of {@link InputGuardrail}s to apply to the input of the model.
     * <p>
     *     类的顺序很重要，因为护栏会按列出的顺序应用。
     *     列表中不能出现重复的护栏。
     *     The order of the classes is important as the guardrails are applied in the order they are listed.
     *     Guardrails can not be present twice in the list.
     * </p>
     */
    Class<? extends InputGuardrail>[] value();
}
