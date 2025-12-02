package dev.langchain4j.exception;

/**
 * 工具参数异常
 * 表示工具参数有问题。
 * 例如，JSON无法解析，或参数类型错误。
 * Indicates that something is wrong with the tool arguments.
 * For example, the JSON cannot be parsed, or an argument is of the wrong type.
 *
 * @since 1.4.0
 */
public class ToolArgumentsException extends LangChain4jException {

    private final Integer errorCode;

    public ToolArgumentsException(String message) {
        this(message, null);
    }

    public ToolArgumentsException(Throwable cause) {
        this(cause, null);
    }

    public ToolArgumentsException(String message, Integer errorCode) {
        this(new RuntimeException(message), errorCode);
    }

    public ToolArgumentsException(Throwable cause, Integer errorCode) {
        super(cause);
        this.errorCode = errorCode;
    }

    public Integer errorCode() {
        return errorCode;
    }
}
