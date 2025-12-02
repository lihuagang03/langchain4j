package dev.langchain4j.exception;

/**
 * 超时异常
 */
public class TimeoutException extends RetriableException {
    public TimeoutException(String message) {
        super(message);
    }

    public TimeoutException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
