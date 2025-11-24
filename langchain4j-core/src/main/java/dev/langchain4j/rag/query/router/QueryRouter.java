package dev.langchain4j.rag.query.router;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.Collection;

/**
 * 查询路由器，将给定的查询路由到一个或多个内容检索器。
 * Routes the given {@link Query} to one or multiple {@link ContentRetriever}s.
 * <br>
 * 目标是确保内容仅从相关的数据源中获取。
 * The goal is to ensure that {@link Content} is retrieved only from relevant data sources.
 * <br>
 * 一些潜在的方法包括：
 * Some potential approaches include:
 * <pre>
 * - Using an LLM (see {@link LanguageModelQueryRouter}) 使用大型语言模型
 * - Using an {@link EmbeddingModel} (aka "semantic routing", see {@code EmbeddingModelTextClassifier} in the {@code langchain4j} module)
 *   使用嵌入模型（也称为“语义路由”）
 * - Using keyword-based routing 使用基于关键字的路由
 * - Route depending on the user ({@code query.metadata().chatMemoryId()}) and/or permissions
 *   路由取决于用户（query.metadata().chatMemoryId()）和/或权限
 * </pre>
 *
 * @see DefaultQueryRouter
 * @see LanguageModelQueryRouter
 */
public interface QueryRouter {

    /**
     * 将给定的查询路由到一个或多个内容检索器。
     * Routes the given {@link Query} to one or multiple {@link ContentRetriever}s.
     *
     * @param query The {@link Query} to be routed.
     * @return A collection of one or more {@link ContentRetriever}s to which the {@link Query} should be routed.
     */
    Collection<ContentRetriever> route(Query query);
}
