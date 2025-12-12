package dev.langchain4j.store.embedding.inmemory;

import dev.langchain4j.Internal;
import dev.langchain4j.data.segment.TextSegment;

/**
 * 内存嵌入存储的JSON编解码器
 */
@Internal
public interface InMemoryEmbeddingStoreJsonCodec {

    InMemoryEmbeddingStore<TextSegment> fromJson(String json);

    String toJson(InMemoryEmbeddingStore<?> store);
}
