package dev.langchain4j.service;

import dev.langchain4j.model.input.PromptTemplate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Function;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 提示模板变量
 * 当 AI 服务中方法的一个参数被注解为 @V 时，它就会成为一个提示模板变量。
 * 它的值将被注入通过 @UserMessage、@SystemMessage 和 AiServices.systemMessageProvider(Function) 定义的提示模板中。
 * When a parameter of a method in an AI Service is annotated with {@code @V},
 * it becomes a prompt template variable. Its value will be injected into prompt templates defined
 * via @{@link UserMessage}, @{@link SystemMessage} and {@link AiServices#systemMessageProvider(Function)}.
 * <p>
 * Example:
 * <pre>
 * {@code @UserMessage("Hello, my name is {{name}}. I am {{age}} years old.")}
 * String chat(@V("name") String name, @V("age") int age);
 * </pre>
 * <p>
 * This annotation is necessary only when the "-parameters" option is *not* enabled during Java compilation.
 * If the "-parameters" option is enabled, parameter names can directly serve as identifiers, eliminating
 * the need to define a value of @V annotation.
 * Example:
 * <pre>
 * {@code @UserMessage("Hello, my name is {{name}}. I am {{age}} years old.")}
 * String chat(@V String name, @V int age);
 * </pre>
 * <p>
 * When using LangChain4j with Quarkus or Spring Boot, using this annotation is not necessary.
 *
 * @see UserMessage
 * @see SystemMessage
 * @see PromptTemplate
 */
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface V {

    /**
     * Name of a variable (placeholder) in a prompt template.
     */
    String value();
}
