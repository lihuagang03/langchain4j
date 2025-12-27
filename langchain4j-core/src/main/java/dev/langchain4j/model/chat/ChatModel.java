package dev.langchain4j.model.chat;

import static dev.langchain4j.model.ModelProvider.OTHER;
import static dev.langchain4j.model.chat.ChatModelListenerUtils.onError;
import static dev.langchain4j.model.chat.ChatModelListenerUtils.onRequest;
import static dev.langchain4j.model.chat.ChatModelListenerUtils.onResponse;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.exception.LangChain4jException;
import dev.langchain4j.model.ModelProvider;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ChatRequestParameters;
import dev.langchain4j.model.chat.request.DefaultChatRequestParameters;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天模型
 * 表示具有聊天 API 的语言模型。
 * Represents a language model that has a chat API.
 *
 * @see StreamingChatModel
 */
public interface ChatModel {

    /**
     * 这是与聊天模型交互的主要 API。
     * This is the main API to interact with the chat model.
     *
     * @param chatRequest a {@link ChatRequest}, containing all the inputs to the LLM
     * @return a {@link ChatResponse}, containing all the outputs from the LLM
     */
    default ChatResponse chat(ChatRequest chatRequest) {

        // 聊天请求
        ChatRequest finalChatRequest = ChatRequest.builder()
                .messages(chatRequest.messages())
                .parameters(defaultRequestParameters().overrideWith(chatRequest.parameters()))
                .build();

        // 聊天模型监听器列表
        List<ChatModelListener> listeners = listeners();
        Map<Object, Object> attributes = new ConcurrentHashMap<>();

        // 在将请求发送到模型之前，会调用此方法
        onRequest(finalChatRequest, provider(), attributes, listeners);
        try {
            // 进行聊天
            ChatResponse chatResponse = doChat(finalChatRequest);
            // 在收到模型的响应后，会调用此方法
            onResponse(chatResponse, finalChatRequest, provider(), attributes, listeners);
            return chatResponse;
        } catch (Exception error) {
            // 当与模型交互时发生错误时，会调用此方法
            onError(error, finalChatRequest, provider(), attributes, listeners);
            throw error;
        }
    }

    default ChatResponse doChat(ChatRequest chatRequest) {
        // 进行聊天
        throw new LangChain4jException("Not implemented");
    }

    default ChatRequestParameters defaultRequestParameters() {
        // 聊天请求参数
        return DefaultChatRequestParameters.EMPTY;
    }

    default List<ChatModelListener> listeners() {
        // 聊天模型监听器
        return List.of();
    }

    default ModelProvider provider() {
        // 模型提供者
        return OTHER;
    }

    /**
     * 与 AI 聊天
     * @param userMessage 用户消息
     */
    default String chat(String userMessage) {

        // 用户消息
        // 聊天请求
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(UserMessage.from(userMessage))
                .build();

        // 聊天
        ChatResponse chatResponse = chat(chatRequest);

        // AI 消息的文本内容
        return chatResponse.aiMessage()
                .text();
    }

    /**
     * 与 AI 聊天
     * @param messages 聊天消息列表
     */
    default ChatResponse chat(ChatMessage... messages) {

        // 聊天请求
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(messages)
                .build();

        // 聊天
        return chat(chatRequest);
    }

    /**
     * 与 AI 聊天
     * @param messages 聊天消息列表
     */
    default ChatResponse chat(List<ChatMessage> messages) {

        // 聊天请求
        ChatRequest chatRequest = ChatRequest.builder()
                .messages(messages)
                .build();

        // 聊天
        return chat(chatRequest);
    }

    default Set<Capability> supportedCapabilities() {
        return Set.of();
    }
}
