package dev.langchain4j.observability.event;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.observability.api.event.ToolExecutedEvent;

/**
 * 工具执行事件的默认实现
 * Default implementation of {@link ToolExecutedEvent}.
 */
public class DefaultToolExecutedEvent extends AbstractAiServiceEvent implements ToolExecutedEvent {

    /**
     * 工具执行请求
     */
    private final ToolExecutionRequest request;
    /**
     * 工具执行的结果文本
     */
    private final String resultText;

    public DefaultToolExecutedEvent(ToolExecutedEventBuilder builder) {
        super(builder);
        this.request = ensureNotNull(builder.request(), "request");
        this.resultText = ensureNotNull(builder.resultText(), "resultText");
    }

    @Override
    public ToolExecutionRequest request() {
        return request;
    }

    @Override
    public String resultText() {
        return resultText;
    }
}
