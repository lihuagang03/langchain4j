package dev.langchain4j.exception;

/**
 * 工具执行异常
 * 表示在执行该工具时出现了问题。
 * Indicates that something went wrong while executing the tool.
 *
 * @since 1.4.0
 */
public class ToolExecutionException extends LangChain4jException {

    private final Integer errorCode;

    public ToolExecutionException(String message) {
        this(message, null);
    }

    public ToolExecutionException(Throwable cause) {
        this(cause, null);
    }

    public ToolExecutionException(String message, Integer errorCode) {
        this(new RuntimeException(message), errorCode);
    }

    public ToolExecutionException(Throwable cause, Integer errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public Integer errorCode() {
        return errorCode;
    }
}
