package dev.langchain4j.observability.event;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.observability.api.event.AiServiceStartedEvent;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/**
 * AI服务已启动事件的默认实现
 * Default implementation of {@link AiServiceStartedEvent}.
 */
public class DefaultAiServiceStartedEvent extends AbstractAiServiceEvent implements AiServiceStartedEvent {

    /**
     * 与调用相关的可选的系统消息
     */
    private final @Nullable SystemMessage systemMessage;
    /**
     * 与调用相关的用户消息
     */
    private final UserMessage userMessage;

    public DefaultAiServiceStartedEvent(AiServiceStartedEventBuilder builder) {
        super(builder);
        this.systemMessage = builder.systemMessage();
        this.userMessage = ensureNotNull(builder.userMessage(), "userMessage");
    }

    @Override
    public Optional<SystemMessage> systemMessage() {
        return Optional.ofNullable(this.systemMessage);
    }

    @Override
    public UserMessage userMessage() {
        return userMessage;
    }
}
