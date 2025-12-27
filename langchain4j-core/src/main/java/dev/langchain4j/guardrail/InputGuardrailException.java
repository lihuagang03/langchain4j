package dev.langchain4j.guardrail;

/**
 * 输入防护异常
 * 当输入防护验证失败时抛出异常。
 * Exception thrown when an input guardrail validation fails.
 * <p>
 *     这个类不打算在护栏实现中抛出。它仅用于框架。捕获它是可以的。
 *     This class is not intended to be thrown within guardrail implementations. It is for the framework only. It is ok to catch it.
 * </p>
 */
public final class InputGuardrailException extends GuardrailException {
    public InputGuardrailException(String message) {
        super(message);
    }

    public InputGuardrailException(String message, Throwable cause) {
        super(message, cause);
    }
}
