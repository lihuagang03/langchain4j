package dev.langchain4j.rag.query.router;

import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.Collection;

import static dev.langchain4j.internal.ValidationUtils.ensureNotEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableCollection;

/**
 * 查询路由器的默认实现，旨在适用于大多数使用场景。
 * Default implementation of {@link QueryRouter} intended to be suitable for the majority of use cases.
 * <br>
 * <br>
 * 重要的是要注意，尽管会尽量避免引入破坏性更改，
 * 但如果发现当前行为无法充分满足大多数使用场景，这个类的默认行为未来可能会被更新。
 * 这些更改将旨在惠及当前和未来的用户。
 * It's important to note that while efforts will be made to avoid breaking changes,
 * the default behavior of this class may be updated in the future if it's found
 * that the current behavior does not adequately serve the majority of use cases.
 * Such changes would be made to benefit both current and future users.
 * <br>
 * <br>
 * 本实现始终将所有查询路由到构造函数中提供的一个或多个内容检索器。
 * This implementation always routes all {@link Query}s
 * to one or multiple {@link ContentRetriever}s provided in the constructor.
 *
 * @see LanguageModelQueryRouter
 */
public class DefaultQueryRouter implements QueryRouter {

    /**
     * 内容检索器列表
     */
    private final Collection<ContentRetriever> contentRetrievers;

    public DefaultQueryRouter(ContentRetriever... contentRetrievers) {
        this(asList(contentRetrievers));
    }

    public DefaultQueryRouter(Collection<ContentRetriever> contentRetrievers) {
        this.contentRetrievers = unmodifiableCollection(ensureNotEmpty(contentRetrievers, "contentRetrievers"));
    }

    @Override
    public Collection<ContentRetriever> route(Query query) {
        return contentRetrievers;
    }
}
