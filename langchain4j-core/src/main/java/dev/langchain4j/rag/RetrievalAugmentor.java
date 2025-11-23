package dev.langchain4j.rag;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.rag.content.Content;

/**
 * 检索增强器，使用检索到的内容增强提供的聊天对话消息。
 * Augments the provided {@link ChatMessage} with retrieved {@link Content}s.
 * <br>
 * 这个作为进入 LangChain4j 中 RAG 流程的入口。
 * This serves as an entry point into the RAG flow in LangChain4j.
 * <br>
 * You are free to use the default implementation ({@link DefaultRetrievalAugmentor}) or to implement a custom one.
 *
 * @see DefaultRetrievalAugmentor
 */
public interface RetrievalAugmentor {

    /**
     * 使用检索到的内容增强增强请求中提供的聊天对话消息。
     * Augments the {@link ChatMessage} provided in the {@link AugmentationRequest} with retrieved {@link Content}s.
     *
     * @param augmentationRequest The {@code AugmentationRequest} containing the {@code ChatMessage} to augment.
     * @return The {@link AugmentationResult} containing the augmented {@code ChatMessage}.
     */
    AugmentationResult augment(AugmentationRequest augmentationRequest);
}
