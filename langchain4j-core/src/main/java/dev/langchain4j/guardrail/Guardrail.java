package dev.langchain4j.guardrail;

/**
 * 护栏
 * 护栏是一条规则，在与大型语言模型（LLM）交互时应用于输入（用户消息）或模型的输出，以确保其安全并符合模型的预期。
 * A guardrail is a rule that is applied when interacting with an LLM either to the input (the user message) or to the
 * output of the model to ensure that they are safe and meet the expectations of the model.
 *
 * @param <P>
 *            The type of the {@link GuardrailRequest}
 * @param <R>
 *            The type of the {@link GuardrailResult}
 */
public interface Guardrail<P extends GuardrailRequest, R extends GuardrailResult<R>> {
    /**
     * 验证模型与用户之间任意一个方向的交互。
     * Validate the interaction between the model and the user in one of the two directions.
     *
     * @param request
     *            The parameters of the request or the response to be validated
     *
     * @return The result of the validation
     */
    R validate(P request);
}
