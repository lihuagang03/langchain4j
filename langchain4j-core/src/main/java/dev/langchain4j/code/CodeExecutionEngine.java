package dev.langchain4j.code;

/**
 * 代码执行引擎
 * 用于执行代码的接口。
 * Interface for executing code.
 */
public interface CodeExecutionEngine {

    /**
     * 执行给定的代码。
     * Execute the given code.
     *
     * @param code The code to execute.
     * @return The result of the execution.
     */
    String execute(String code);
}
