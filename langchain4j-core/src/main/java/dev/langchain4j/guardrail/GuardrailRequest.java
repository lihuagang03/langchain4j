package dev.langchain4j.guardrail;

/**
 * 护栏请求
 * 表示传递给 Guardrail.validate(GuardrailRequest) 的参数，用于验证用户与 LLM 之间的交互。
 * Represents the parameter passed to {@link Guardrail#validate(GuardrailRequest)}} in order to validate an interaction
 * between a user and the LLM.
 */
public sealed interface GuardrailRequest<P extends GuardrailRequest<P>>
        permits InputGuardrailRequest, OutputGuardrailRequest {

    /**
     * 检索在护栏检查中共享的常用参数。
     * Retrieves the common parameters that are shared across guardrail checks.
     *
     * @return an instance of {@code GuardrailRequestParams} containing shared parameters such as chat memory,
     *         user message template, and additional variables.
     */
    GuardrailRequestParams requestParams();

    /**
     * 使用给定的输入或输出文本重新创建此护栏参数。
     * Recreate this guardrail param with the given input or output text.
     *
     * @param text
     *            The text of the rewritten param.
     *
     * @return A clone of this guardrail params with the given input or output text.
     */
    P withText(String text);
}
