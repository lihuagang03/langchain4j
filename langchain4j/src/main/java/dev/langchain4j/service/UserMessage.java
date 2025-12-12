package dev.langchain4j.service;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 用户消息
 * 指定每次调用 AI 服务时要使用的完整用户消息或用户消息模板。
 * 用户消息可以包含模板变量，这些变量将通过使用 @V 注解的方法参数的值来解析。
 * Specifies either a complete user message or a user message template to be used each time an AI service is invoked.
 * The user message can contain template variables,
 * which will be resolved with values from method parameters annotated with @{@link V}.
 * <br>
 * An example:
 * <pre>
 * interface Assistant {
 *
 *     {@code @UserMessage}("Say hello to {{name}}")
 *     String greet(@V("name") String name);
 * }
 * </pre>
 * {@code @UserMessage} can also be used with method parameters:
 * <pre>
 * interface Assistant {
 *
 *     {@code @SystemMessage}("You are a {{characteristic}} assistant")
 *     String chat(@UserMessage String userMessage, @V("characteristic") String characteristic);
 * }
 * </pre>
 * In this case {@code String userMessage} can contain unresolved template variables (e.g. "{{characteristic}}"),
 * which will be resolved using the values of method parameters annotated with @{@link V}.
 *
 * @see SystemMessage
 */
@Retention(RUNTIME)
@Target({METHOD, PARAMETER})
public @interface UserMessage {

    /**
     * 提示模板可以用一行或多行来定义。
     * Prompt template can be defined in one line or multiple lines.
     * If the template is defined in multiple lines, the lines will be joined with a delimiter defined below.
     */
    String[] value() default "";

    String delimiter() default "\n";

    /**
     * The resource from which to read the prompt template.
     * If no resource is specified, the prompt template is taken from {@link #value()}.
     * If the resource is not found, an {@link IllegalConfigurationException} is thrown.
     * <p>
     * The resource will be read by calling {@link Class#getResourceAsStream(String)}
     * on the AI Service class (interface).
     */
    String fromResource() default "";
}
