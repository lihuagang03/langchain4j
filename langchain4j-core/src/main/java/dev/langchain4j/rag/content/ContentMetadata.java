package dev.langchain4j.rag.content;

/**
 * 内容元数据
 */
public enum ContentMetadata {
    /**
     * 分数
     */
    SCORE,
    /**
     * 重排序的分数
     */
    RERANKED_SCORE,
    /**
     * 嵌入向量身份
     */
    EMBEDDING_ID
}
