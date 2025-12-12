package dev.langchain4j.service;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 用户名称
 * 带有 @UserName 注解的方法参数的值将被注入到 UserMessage 的 'name' 字段中。
 * The value of a method parameter annotated with @UserName will be injected into the field 'name' of a UserMessage.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface UserName {
}
