package dev.langchain4j.rag.query.transformer;

import dev.langchain4j.rag.query.Query;

import java.util.Collection;

/**
 * 查询转换器，将给定的查询转换为一个或多个查询。
 * Transforms the given {@link Query} into one or multiple {@link Query}s.
 * <br>
 * 目标是通过修改或扩展原始查询来提高检索质量。
 * The goal is to enhance retrieval quality by modifying or expanding the original {@link Query}.
 * <br>
 * 一些已知的改进检索的方法包括：
 * Some known approaches to improve retrieval include:
 * <pre>
 * - Query compression (see {@link CompressingQueryTransformer}) 查询压缩
 * - Query expansion (see {@link ExpandingQueryTransformer}) 查询扩展
 * - Query re-writing 查询重写
 * - Step-back prompting 后退式提示
 * - Hypothetical document embeddings (HyDE) 假设性文档嵌入
 * </pre>
 * Additional details can be found <a href="https://blog.langchain.dev/query-transformations/">here</a>.
 *
 * @see DefaultQueryTransformer
 * @see CompressingQueryTransformer
 * @see ExpandingQueryTransformer
 */
public interface QueryTransformer {

    /**
     * 将给定的查询转换为一个或多个查询。
     * Transforms the given {@link Query} into one or multiple {@link Query}s.
     *
     * @param query The {@link Query} to be transformed.
     * @return A collection of one or more {@link Query}s derived from the original {@link Query}.
     */
    Collection<Query> transform(Query query);
}

