package dev.langchain4j.spi.store.embedding.inmemory;

import dev.langchain4j.Internal;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStoreJsonCodec;

/**
 * 内存嵌入存储的 JSON 编解码器的工厂
 */
@Internal
public interface InMemoryEmbeddingStoreJsonCodecFactory {

    /**
     * @return 内存嵌入存储的 JSON 编解码器
     */
    InMemoryEmbeddingStoreJsonCodec create();
}
