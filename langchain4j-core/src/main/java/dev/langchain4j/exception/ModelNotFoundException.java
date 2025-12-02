package dev.langchain4j.exception;

/**
 * 未找到模型异常
 */
public class ModelNotFoundException extends NonRetriableException {
    public ModelNotFoundException(String message) {
        super(message);
    }

    public ModelNotFoundException(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public ModelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
