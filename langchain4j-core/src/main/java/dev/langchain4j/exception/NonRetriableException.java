package dev.langchain4j.exception;

/**
 * 不可重试异常
 */
public class NonRetriableException extends LangChain4jException {
    public NonRetriableException(String message) {
        super(message);
    }

    public NonRetriableException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public NonRetriableException(String message, Throwable cause) {
        super(message, cause);
    }
}
