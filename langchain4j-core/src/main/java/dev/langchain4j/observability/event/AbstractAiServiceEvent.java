package dev.langchain4j.observability.event;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.observability.api.event.AiServiceEvent;

/**
 * AI服务事件的抽象基类
 */
public abstract class AbstractAiServiceEvent implements AiServiceEvent {
    /**
     * AI服务调用的上下文
     */
    private final InvocationContext invocationContext;

    protected AbstractAiServiceEvent(Builder<?> builder) {
        ensureNotNull(builder, "builder");
        this.invocationContext = ensureNotNull(builder.invocationContext(), "invocationContext");
    }

    @Override
    public InvocationContext invocationContext() {
        return this.invocationContext;
    }
}
