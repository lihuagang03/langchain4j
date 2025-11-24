package dev.langchain4j.agent.tool;

import dev.langchain4j.Experimental;

/**
 * 工具的返回行为
 * 定义语言模型调用工具时，工具返回值的行为。
 * Defines the behavior of a tool's return value when called by a language model.
 */
@Experimental
public enum ReturnBehavior {

    /**
     * 该工具返回的值会被发送回大型语言模型以进行进一步处理。
     * The value returned by the tool is sent back to the LLM for further processing.
     * This is the default behavior.
     */
    TO_LLM,

    /**
     * 立即将工具返回的值返回给调用者，而不允许大型语言模型进一步处理它。
     * Returns immediately to the caller the value returned by the tool without allowing the LLM
     * to further process it. Immediate return is only allowed on AI services returning {@code dev.langchain4j.service.Result},
     * while a {@code RuntimeException} will be thrown attempting to use a tool with immediate return with an
     * AI service having a different return type.
     */
    IMMEDIATE;
}
