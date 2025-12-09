package dev.langchain4j.service.tool;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import java.util.Objects;

/**
 * 工具执行结果
 * 表示工具执行的结果。
 * Represents the result of a tool execution.
 *
 * @since 1.6.0
 */
public class ToolExecutionResult {

    /**
     * 错误标志
     */
    private final boolean isError;
    /**
     * 执行结果
     */
    private final Object result;
    /**
     * 执行结果文本
     */
    private final String resultText;

    public ToolExecutionResult(Builder builder) {
        this.isError = builder.isError;
        this.result = builder.result;
        this.resultText = ensureNotNull(builder.resultText, "resultText");
    }

    /**
     * Indicates whether the tool execution result represents an error.
     */
    public boolean isError() {
        return isError;
    }

    /**
     * Returns the tool execution result as object.
     * This object is the actual value returned by the tool.
     *
     * @see #resultText()
     */
    public Object result() {
        return result;
    }

    /**
     * Returns the tool execution result as text.
     * It is a {@link #result()} that is serialized into JSON string.
     *
     * @see #result()
     */
    public String resultText() {
        return resultText;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        ToolExecutionResult that = (ToolExecutionResult) object;
        return isError == that.isError
                && Objects.equals(result, that.result)
                && Objects.equals(resultText, that.resultText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isError, result, resultText);
    }

    @Override
    public String toString() {
        return "ToolExecutionResult{" +
                "isError=" + isError +
                ", result=" + result +
                ", resultText='" + resultText + '\'' +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private boolean isError;
        private Object result;
        private String resultText;

        public Builder isError(boolean isError) {
            this.isError = isError;
            return this;
        }

        public Builder result(Object result) {
            this.result = result;
            return this;
        }

        public Builder resultText(String resultText) {
            this.resultText = resultText;
            return this;
        }

        public ToolExecutionResult build() {
            return new ToolExecutionResult(this);
        }
    }
}
