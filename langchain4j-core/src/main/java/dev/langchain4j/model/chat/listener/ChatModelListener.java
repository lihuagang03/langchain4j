package dev.langchain4j.model.chat.listener;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * 聊天对话模型监听器
 * 一个监听请求、响应和错误的聊天对话模型监听器。
 * A {@link ChatModel} listener that listens for requests, responses and errors.
 */
public interface ChatModelListener {

    /**
     * 在将请求发送到模型之前，会调用此方法。
     * This method is called before the request is sent to the model.
     *
     * @param requestContext The request context. It contains the {@link ChatRequest} and attributes.
     *                       The attributes can be used to pass data between methods of this listener
     *                       or between multiple listeners.
     */
    default void onRequest(ChatModelRequestContext requestContext) {

    }

    /**
     * 在收到模型的响应后，会调用此方法。
     * This method is called after the response is received from the model.
     *
     * @param responseContext The response context.
     *                        It contains {@link ChatResponse}, corresponding {@link ChatRequest} and attributes.
     *                        The attributes can be used to pass data between methods of this listener
     *                        or between multiple listeners.
     */
    default void onResponse(ChatModelResponseContext responseContext) {

    }

    /**
     * 当与模型交互时发生错误时，会调用此方法。
     * This method is called when an error occurs during interaction with the model.
     *
     * @param errorContext The error context.
     *                     It contains the error, corresponding {@link ChatRequest},
     *                     partial {@link ChatResponse} (if available) and attributes.
     *                     The attributes can be used to pass data between methods of this listener
     *                     or between multiple listeners.
     */
    default void onError(ChatModelErrorContext errorContext) {

    }
}
