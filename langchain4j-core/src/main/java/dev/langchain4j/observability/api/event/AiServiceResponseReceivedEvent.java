package dev.langchain4j.observability.api.event;

import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.observability.event.DefaultAiServiceResponseReceivedEvent;

/**
 * AI服务响应已接收到事件
 * 当收到来自 ChatModel 的响应时触发。
 * 需要注意的是，当存在工具或保护措施时，在一次 AI 服务调用期间可能会多次触发此事件。
 * Invoked when response from a {@link dev.langchain4j.model.chat.ChatModel} is received.
 * It is important to note that this can be invoked multiple times during a single AI Service invocation
 * when tools or guardrails exist.
 */
public interface AiServiceResponseReceivedEvent extends AiServiceEvent {
    /**
     * 从 AI 服务调用事件中检索聊天响应。
     * Retrieves the chat response from the AI Service invocation event.
     *
     * @return the {@link ChatResponse} object containing the AI-generated message and related metadata.
     */
    ChatResponse response();

    @Override
    default Class<AiServiceResponseReceivedEvent> eventClass() {
        return AiServiceResponseReceivedEvent.class;
    }

    @Override
    default AiServiceResponseReceivedEventBuilder toBuilder() {
        return new AiServiceResponseReceivedEventBuilder(this);
    }

    static AiServiceResponseReceivedEventBuilder builder() {
        return new AiServiceResponseReceivedEventBuilder();
    }

    /**
     * Builder for {@link DefaultAiServiceResponseReceivedEvent} instances.
     */
    class AiServiceResponseReceivedEventBuilder extends Builder<AiServiceResponseReceivedEvent> {
        /**
         * 聊天响应
         */
        private ChatResponse response;

        protected AiServiceResponseReceivedEventBuilder() {}

        /**
         * Creates a builder initialized from an existing {@link AiServiceResponseReceivedEvent}.
         */
        protected AiServiceResponseReceivedEventBuilder(AiServiceResponseReceivedEvent src) {
            super(src);
            response(src.response());
        }

        public ChatResponse response() {
            return response;
        }

        /**
         * Sets the invocation context.
         */
        public AiServiceResponseReceivedEventBuilder invocationContext(InvocationContext invocationContext) {
            return (AiServiceResponseReceivedEventBuilder) super.invocationContext(invocationContext);
        }

        /**
         * Sets the chat response.
         */
        public AiServiceResponseReceivedEventBuilder response(ChatResponse response) {
            this.response = response;
            return this;
        }

        /**
         * Builds a {@link AiServiceResponseReceivedEvent}.
         */
        public AiServiceResponseReceivedEvent build() {
            return new DefaultAiServiceResponseReceivedEvent(this);
        }
    }
}
