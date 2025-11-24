package dev.langchain4j.agent.tool;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 工具记忆ID
 * 如果工具方法的参数被此注解标注，记忆ID（在 AI 服务中用 @MemoryId 注解的参数）将会被自动注入。
 * If a {@link Tool} method parameter is annotated with this annotation,
 * memory id (parameter annotated with @MemoryId in AI Service) will be injected automatically.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface ToolMemoryId {
}
