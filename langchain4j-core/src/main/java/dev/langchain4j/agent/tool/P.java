package dev.langchain4j.agent.tool;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 工具的参数
 * Parameter of a Tool
 */
@Retention(RUNTIME)
@Target({PARAMETER})
public @interface P {

    /**
     * 参数的描述
     * Description of a parameter
     * @return the description of a parameter
     */
    String value();

    /**
     * 该参数是否必填
     * Whether the parameter is required
     * @return true if the parameter is required, false otherwise
     * Default is true.
     */
    boolean required() default true;
}
