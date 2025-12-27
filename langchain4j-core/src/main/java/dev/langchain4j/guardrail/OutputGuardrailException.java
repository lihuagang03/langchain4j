package dev.langchain4j.guardrail;

/**
 * 输出护栏异常
 * 当输出护栏验证失败时抛出异常。
 * Exception thrown when an output guardrail validation fails.
 * <p>
 *     这个类不打算在护栏实现中抛出。它仅用于框架。捕获它是可以的。
 *     This class is not intended to be thrown within guardrail implementations. It is for the framework only. It is ok to catch it.
 * </p>
 */
public final class OutputGuardrailException extends GuardrailException {
    public OutputGuardrailException(String message) {
        super(message);
    }

    public OutputGuardrailException(String message, Throwable cause) {
        super(message, cause);
    }
}
