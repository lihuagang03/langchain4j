package dev.langchain4j.rag.query.router;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotBlank;
import static dev.langchain4j.internal.ValidationUtils.ensureNotEmpty;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static dev.langchain4j.rag.query.router.LanguageModelQueryRouter.FallbackStrategy.DO_NOT_ROUTE;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * 查询路由器，一个使用聊天对话模型来做出路由决策。
 * A {@link QueryRouter} that utilizes a {@link ChatModel} to make a routing decision.
 * <br>
 * 构造函数中提供的每个内容检索器都应附有描述，以帮助大型语言模型决定将查询路由到何处。
 * Each {@link ContentRetriever} provided in the constructor should be accompanied by a description which
 * should help the LLM to decide where to route a {@link Query}.
 * <br>
 * Refer to {@link #DEFAULT_PROMPT_TEMPLATE} and implementation for more details.
 * <br>
 * <br>
 * Configurable parameters (optional):
 * <br>
 * - {@link #promptTemplate}: The prompt template used to ask the LLM for routing decisions.
 * <br>
 * - {@link #fallbackStrategy}: The strategy applied if the call to the LLM fails of if LLM does not return a valid response.
 * Please check {@link FallbackStrategy} for more details. Default value: {@link FallbackStrategy#DO_NOT_ROUTE}
 *
 * @see DefaultQueryRouter
 */
public class LanguageModelQueryRouter implements QueryRouter {

    /**
     * 默认的提示模版
     */
    /*
     * 根据用户查询，从以下选项中确定最合适的数据源以检索相关信息：
     * {{options}}
     * 你的答案必须仅包含一个数字或多个用逗号分隔的数字，不能包含其他任何内容，这一点非常重要！
     * 用户查询：{{query}}
     */
    public static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = PromptTemplate.from(
            """
                    Based on the user query, determine the most suitable data source(s) \
                    to retrieve relevant information from the following options:
                    {{options}}
                    It is very important that your answer consists of either a single number \
                    or multiple numbers separated by commas and nothing else!
                    User query: {{query}}"""
    );

    /**
     * 聊天对话模型
     */
    protected final ChatModel chatModel;
    /**
     * 提示模版
     */
    protected final PromptTemplate promptTemplate;
    /**
     * ID到内容检索器描述的选项列表
     */
    protected final String options;
    /**
     * ID到内容检索器的映射
     */
    protected final Map<Integer, ContentRetriever> idToRetriever;
    /**
     * 回退策略
     */
    protected final FallbackStrategy fallbackStrategy;

    public LanguageModelQueryRouter(ChatModel chatModel,
                                    Map<ContentRetriever, String> retrieverToDescription) {
        this(chatModel, retrieverToDescription, DEFAULT_PROMPT_TEMPLATE, DO_NOT_ROUTE);
    }

    public LanguageModelQueryRouter(ChatModel chatModel,
                                    Map<ContentRetriever, String> retrieverToDescription,
                                    PromptTemplate promptTemplate,
                                    FallbackStrategy fallbackStrategy) {
        this.chatModel = ensureNotNull(chatModel, "chatModel");
        ensureNotEmpty(retrieverToDescription, "retrieverToDescription");
        this.promptTemplate = getOrDefault(promptTemplate, DEFAULT_PROMPT_TEMPLATE);

        Map<Integer, ContentRetriever> idToRetriever = new HashMap<>();
        StringBuilder optionsBuilder = new StringBuilder();
        int id = 1;
        for (Map.Entry<ContentRetriever, String> entry : retrieverToDescription.entrySet()) {
            idToRetriever.put(id, ensureNotNull(entry.getKey(), "ContentRetriever"));

            // id: ContentRetriever description
            if (id > 1) {
                optionsBuilder.append("\n");
            }
            optionsBuilder.append(id);
            optionsBuilder.append(": ");
            optionsBuilder.append(ensureNotBlank(entry.getValue(), "ContentRetriever description"));

            id++;
        }
        this.idToRetriever = idToRetriever;
        this.options = optionsBuilder.toString();
        this.fallbackStrategy = getOrDefault(fallbackStrategy, DO_NOT_ROUTE);
    }

    public static LanguageModelQueryRouterBuilder builder() {
        return new LanguageModelQueryRouterBuilder();
    }

    @Override
    public Collection<ContentRetriever> route(Query query) {
        // 提示
        Prompt prompt = createPrompt(query);
        try {
            // 聊天对话模型
            String response = chatModel.chat(prompt.text());
            // 解析响应内容
            return parse(response);
        } catch (Exception e) {
            // 回退
            return fallback(query, e);
        }
    }

    protected Collection<ContentRetriever> fallback(Query query, Exception e) {
        return switch (fallbackStrategy) {
            case DO_NOT_ROUTE -> {
                yield emptyList();
            }
            case ROUTE_TO_ALL -> {
                yield new ArrayList<>(idToRetriever.values());
            }
            default -> throw new RuntimeException(e);
        };
    }

    protected Prompt createPrompt(Query query) {
        Map<String, Object> variables = new HashMap<>();
        // 查询文本
        variables.put("query", query.text());
        // ID到内容检索器描述的选项列表
        variables.put("options", options);
        return promptTemplate.apply(variables);
    }

    protected Collection<ContentRetriever> parse(String choices) {
        // ID选择到内容检索器的映射
        return stream(choices.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .map(idToRetriever::get)
                .collect(toList());
    }

    /**
     * 回退策略
     * 如果调用大型语言模型失败或大型语言模型未返回有效响应时采用的策略。
     * 这可能是因为格式不正确，或者不清楚应该发送到哪里。
     * Strategy applied if the call to the LLM fails of if LLM does not return a valid response.
     * It could be because it was formatted improperly, or it is unclear where to route.
     */
    public enum FallbackStrategy {

        /**
         * In this case, the {@link Query} will not be routed to any {@link ContentRetriever},
         * thus skipping the RAG flow. No content will be appended to the original {@link UserMessage}.
         */
        DO_NOT_ROUTE,

        /**
         * In this case, the {@link Query} will be routed to all {@link ContentRetriever}s.
         */
        ROUTE_TO_ALL,

        /**
         * In this case, an original exception will be re-thrown, and the RAG flow will fail.
         */
        FAIL
    }

    public static class LanguageModelQueryRouterBuilder {
        private ChatModel chatModel;
        private Map<ContentRetriever, String> retrieverToDescription;
        private PromptTemplate promptTemplate;
        private FallbackStrategy fallbackStrategy;

        LanguageModelQueryRouterBuilder() {
        }

        public LanguageModelQueryRouterBuilder chatModel(ChatModel chatModel) {
            this.chatModel = chatModel;
            return this;
        }

        public LanguageModelQueryRouterBuilder retrieverToDescription(Map<ContentRetriever, String> retrieverToDescription) {
            this.retrieverToDescription = retrieverToDescription;
            return this;
        }

        public LanguageModelQueryRouterBuilder promptTemplate(PromptTemplate promptTemplate) {
            this.promptTemplate = promptTemplate;
            return this;
        }

        public LanguageModelQueryRouterBuilder fallbackStrategy(FallbackStrategy fallbackStrategy) {
            this.fallbackStrategy = fallbackStrategy;
            return this;
        }

        public LanguageModelQueryRouter build() {
            return new LanguageModelQueryRouter(this.chatModel, this.retrieverToDescription, this.promptTemplate, this.fallbackStrategy);
        }
    }
}
