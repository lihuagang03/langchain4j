package dev.langchain4j.rag.content;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
import dev.langchain4j.rag.content.injector.ContentInjector;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.Map;

/**
 * 内容
 * Represents content relevant to a user {@link Query} with the potential to enhance and ground the LLM's response.
 * <br>
 * Currently, it is limited to text content (i.e., {@link TextSegment}),
 * but future expansions may include support for other modalities (e.g., images, audio, video, etc.).
 *
 * @see ContentRetriever
 * @see ContentAggregator
 * @see ContentInjector
 */
public interface Content {

    /**
     * 文本片段
     */
    TextSegment textSegment();

    /**
     * 内容元数据
     */
    Map<ContentMetadata, Object> metadata();

    static Content from(String text) {
        return new DefaultContent(text);
    }

    static Content from(TextSegment textSegment) {
        return new DefaultContent(textSegment);
    }

    static Content from(TextSegment textSegment, Map<ContentMetadata, Object> metadata) {
        return new DefaultContent(textSegment, metadata);
    }
}
