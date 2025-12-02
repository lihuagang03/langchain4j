package dev.langchain4j.exception;

/**
 * 未解析的模型服务器异常
 */
public class UnresolvedModelServerException extends NonRetriableException {
    public UnresolvedModelServerException(String message) {
        super(message);
    }

    public UnresolvedModelServerException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public UnresolvedModelServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
