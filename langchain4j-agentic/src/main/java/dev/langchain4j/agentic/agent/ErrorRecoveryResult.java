package dev.langchain4j.agentic.agent;

/**
 * 错误恢复结果
 * @param type 类型
 * @param result 结果
 */
public record ErrorRecoveryResult(Type type, Object result) {

    public enum Type {
        /**
         * 抛出异常
         */
        THROW_EXCEPTION,
        /**
         * 返回结果
         */
        RETURN_RESULT,
        /**
         * 重试
         */
        RETRY
    }

    public static ErrorRecoveryResult throwException() {
        return new ErrorRecoveryResult(Type.THROW_EXCEPTION, null);
    }

    public static ErrorRecoveryResult retry() {
        return new ErrorRecoveryResult(Type.RETRY, null);
    }

    public static ErrorRecoveryResult result(Object result) {
        return new ErrorRecoveryResult(Type.RETURN_RESULT, result);
    }
}
