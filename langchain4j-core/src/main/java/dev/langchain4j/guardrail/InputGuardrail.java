package dev.langchain4j.guardrail;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrailResult.Failure;

/**
 * 输入护栏
 * 输入护栏是一条应用于模型输入的规则，用于确保输入（即用户消息和参数）是安全的并符合模型的预期。
 * An input guardrail is a rule that is applied to the input of the model to ensure that the input (i.e. the user
 * message and parameters) is safe and meets the expectations of the model.
 * <p>
 *     输入保护措施要么成功，要么失败。成功的保护措施意味着输入是有效的，可以发送到模型。
 *     失败的保护措施意味着输入是无效的，无法发送到模型。
 *     Input guardrails are either successful or failed. A successful guardrail means that the input is valid and can be sent to
 *     the model. A failed guardrail means that the input is invalid and cannot be sent to the model.
 * </p>
 * <p>
 *     一个失效的护栏将阻止对任何其他输入护栏的进一步处理。
 *     A failed guardrail will stop further processing of any other input guardrails.
 * </p>
 */
public interface InputGuardrail extends Guardrail<InputGuardrailRequest, InputGuardrailResult> {
    /**
     * 验证将发送给大型语言模型的用户消息。
     * Validates the {@code user message} that will be sent to the LLM.
     * <p>
     *
     * @param userMessage
     *            the response from the LLM
     */
    default InputGuardrailResult validate(UserMessage userMessage) {
        return failure("Validation not implemented");
    }

    /**
     * 验证将发送给大型语言模型的输入。
     * Validates the input that will be sent to the LLM.
     * <p>
     * 与 validate(UserMessage) 不同，这种方法允许访问内存和增强结果（在 RAG 的情况下）。
     * Unlike {@link #validate(UserMessage)}, this method allows to access the memory and the augmentation result (in
     * the case of a RAG).
     * <p>
     * 实现不得尝试写入内存或增强结果。
     * Implementation must not attempt to write to the memory or the augmentation result.
     *
     * @param request
     *            the parameters, including the user message, the memory, and the augmentation result.
     *            参数，包括用户消息、聊天记忆和增强结果。
     */
    @Override
    default InputGuardrailResult validate(InputGuardrailRequest request) {
        ensureNotNull(request, "params");
        return validate(request.userMessage());
    }

    /**
     * 产生一个没有任何成功文本的成功结果
     * Produces a successful result without any successful text
     *
     * @return The result of a successful input guardrail validation.
     */
    default InputGuardrailResult success() {
        return InputGuardrailResult.success();
    }

    /**
     * 产生具有特定成功文本的成功结果
     * Produces a successful result with specific success text
     *
     * @return The result of a successful input guardrail validation with a specific text.
     *
     * @param successfulText
     *            The text of the successful result.
     *            成功结果的文本
     */
    default InputGuardrailResult successWith(String successfulText) {
        return InputGuardrailResult.successWith(successfulText);
    }

    /**
     * 产生非致命失败
     * Produces a non-fatal failure
     *
     * @param message
     *            A message describing the failure.
     *            描述失败的消息
     *
     * @return The result of a failed input guardrail validation.
     */
    default InputGuardrailResult failure(String message) {
        return new InputGuardrailResult(new Failure(message), false);
    }

    /**
     * 产生非致命失败
     * Produces a non-fatal failure
     *
     * @param message
     *            A message describing the failure.
     *            描述失败的消息
     * @param cause
     *            The exception that caused this failure.
     *            导致此失败的异常
     *
     * @return The result of a failed input guardrail validation.
     */
    default InputGuardrailResult failure(String message, Throwable cause) {
        return new InputGuardrailResult(new Failure(message, cause), false);
    }

    /**
     * 产生非致命失败
     * Produces a fatal failure
     *
     * @param message
     *            A message describing the failure.
     *            描述失败的消息
     *
     * @return The result of a failed input guardrail validation.
     */
    default InputGuardrailResult fatal(String message) {
        return new InputGuardrailResult(new Failure(message), true);
    }

    /**
     * 产生非致命失败
     * Produces a non-fatal failure
     *
     * @param message
     *            A message describing the failure.
     *            描述失败的消息
     * @param cause
     *            The exception that caused this failure.
     *            导致此失败的异常
     *
     * @return The result of a failed input guardrail validation.
     */
    default InputGuardrailResult fatal(String message, Throwable cause) {
        return new InputGuardrailResult(new Failure(message, cause), true);
    }
}
