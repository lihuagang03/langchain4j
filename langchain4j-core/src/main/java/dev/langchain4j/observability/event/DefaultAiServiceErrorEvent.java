package dev.langchain4j.observability.event;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.observability.api.event.AiServiceErrorEvent;

/**
 * AI服务错误事件的默认实现
 * Default implementation of {@link AiServiceErrorEvent}.
 */
public class DefaultAiServiceErrorEvent extends AbstractAiServiceEvent implements AiServiceErrorEvent {

    /**
     * AI 服务调用失败相关的错误的异常
     */
    private final Throwable error;

    public DefaultAiServiceErrorEvent(AiServiceErrorEventBuilder builder) {
        super(builder);
        this.error = ensureNotNull(builder.getError(), "error");
    }

    @Override
    public Throwable error() {
        return error;
    }
}
