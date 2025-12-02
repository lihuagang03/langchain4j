package dev.langchain4j.exception;

/**
 * 请求频率限制异常
 */
public class RateLimitException extends RetriableException {
    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public RateLimitException(String message, Throwable cause) {
        super(message, cause);
    }
}
