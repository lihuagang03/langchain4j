package dev.langchain4j.rag.content.retriever;

import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.query.Query;

import java.util.List;

/**
 * 内容检索器
 * 使用给定的查询从底层数据源检索内容。
 * Retrieves {@link Content}s from an underlying data source using a given {@link Query}.
 * <br>
 * 目标是仅检索与给定查询相关的内容。
 * The goal is to retrieve only relevant {@link Content}s in relation to a given {@link Query}.
 * <br>
 * 底层数据源几乎可以是任何东西：
 * The underlying data source can be virtually anything:
 * <pre>
 * - Embedding (vector) store (see {@link EmbeddingStoreContentRetriever}) 嵌入（向量）存储
 * - Full-text search engine (see {@code AzureAiSearchContentRetriever} in the {@code langchain4j-azure-ai-search} module)
 *   全文搜索引擎
 * - Hybrid of vector and full-text search (see {@code AzureAiSearchContentRetriever} in the {@code langchain4j-azure-ai-search} module)
 *   向量搜索和全文搜索的混合
 * - Web Search Engine (see {@link WebSearchContentRetriever}) 网络搜索引擎
 * - Knowledge graph (see {@code Neo4jContentRetriever} in the {@code langchain4j-community-neo4j-retriever} module)
 *   知识图谱
 * - SQL database (see {@code SqlDatabaseContentRetriever} in the {@code langchain4j-experimental-sql} module)
 *   SQL数据库
 * - etc.
 * </pre>
 *
 * @see EmbeddingStoreContentRetriever
 * @see WebSearchContentRetriever
 */
public interface ContentRetriever {

    /**
     * 使用给定的查询检索相关内容。
     * 内容按相关性排序，最相关的内容会出现在返回的内容列表的开头。
     * Retrieves relevant {@link Content}s using a given {@link Query}.
     * The {@link Content}s are sorted by relevance, with the most relevant {@link Content}s appearing
     * at the beginning of the returned {@code List<Content>}.
     *
     * @param query The {@link Query} to use for retrieval.
     * @return A list of retrieved {@link Content}s.
     */
    List<Content> retrieve(Query query);
}
