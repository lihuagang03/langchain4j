package dev.langchain4j.exception;

/**
 * 身份验证异常
 */
public class AuthenticationException extends NonRetriableException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
