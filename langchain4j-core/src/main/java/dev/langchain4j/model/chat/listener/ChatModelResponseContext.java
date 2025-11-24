package dev.langchain4j.model.chat.listener;

import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

import java.util.Map;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * 聊天对话模型的响应上下文。
 * The chat response context.
 * It contains {@link ChatResponse}, corresponding {@link ChatRequest}, {@link ModelProvider} and attributes.
 * The attributes can be used to pass data between methods of a {@link ChatModelListener}
 * or between multiple {@link ChatModelListener}s.
 */
public class ChatModelResponseContext {

    /**
     * 聊天对话响应
     */
    private final ChatResponse chatResponse;
    /**
     * 聊天对话请求
     */
    private final ChatRequest chatRequest;
    /**
     * 模型提供者
     */
    private final ModelProvider modelProvider;
    /**
     * 属性映射
     */
    private final Map<Object, Object> attributes;

    public ChatModelResponseContext(ChatResponse chatResponse,
                                    ChatRequest chatRequest,
                                    ModelProvider modelProvider,
                                    Map<Object, Object> attributes) {
        this.chatResponse = ensureNotNull(chatResponse, "chatResponse");
        this.chatRequest = ensureNotNull(chatRequest, "chatRequest");
        this.modelProvider = modelProvider;
        this.attributes = ensureNotNull(attributes, "attributes");
    }

    public ChatResponse chatResponse() {
        return chatResponse;
    }

    public ChatRequest chatRequest() {
        return chatRequest;
    }

    public ModelProvider modelProvider() {
        return modelProvider;
    }

    /**
     * @return The attributes map. It can be used to pass data between methods of a {@link ChatModelListener}
     * or between multiple {@link ChatModelListener}s.
     */
    public Map<Object, Object> attributes() {
        return attributes;
    }
}
