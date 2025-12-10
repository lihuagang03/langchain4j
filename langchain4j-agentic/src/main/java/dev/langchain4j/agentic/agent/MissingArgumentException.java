package dev.langchain4j.agentic.agent;

/**
 * 缺少参数异常
 */
public class MissingArgumentException extends AgentInvocationException {

    /**
     * 参数名
     */
    private final String argumentName;

    public MissingArgumentException(String argumentName) {
        super("Missing argument: " + argumentName);
        this.argumentName = argumentName;
    }

    public String argumentName() {
        return argumentName;
    }
}
