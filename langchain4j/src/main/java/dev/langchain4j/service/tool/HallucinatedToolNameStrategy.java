package dev.langchain4j.service.tool;

import static dev.langchain4j.internal.Exceptions.runtime;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import java.util.function.Function;

/**
 * 幻觉的工具名称策略
 */
public enum HallucinatedToolNameStrategy implements Function<ToolExecutionRequest, ToolExecutionResultMessage> {
    /**
     * 抛出异常
     */
    THROW_EXCEPTION;

    public ToolExecutionResultMessage apply(ToolExecutionRequest toolExecutionRequest) {
        // LLM 正尝试执行 '%s' 工具，但不存在这样的工具。很可能这是幻觉。
        // 你可以通过在 AiService 上设置 hallucinatedToolNameStrategy 来覆盖此默认策略
        switch (this) {
            case THROW_EXCEPTION -> {
                throw runtime(
                        "The LLM is trying to execute the '%s' tool, but no such tool exists. Most likely, it is a "
                                + "hallucination. You can override this default strategy by setting the hallucinatedToolNameStrategy on the AiService",
                        toolExecutionRequest.name());
            }
        }
        throw new UnsupportedOperationException();
    }
}
