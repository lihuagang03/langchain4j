package dev.langchain4j.rag.query.transformer;

import dev.langchain4j.internal.Utils;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.query.Query;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureGreaterThanZero;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

/**
 * 扩展查询转换器，一个使用聊天对话模型扩展给定查询。
 * A {@link QueryTransformer} that utilizes a {@link ChatModel} to expand a given {@link Query}.
 * <br>
 * Refer to {@link #DEFAULT_PROMPT_TEMPLATE} and implementation for more details.
 * <br>
 * <br>
 * Configurable parameters (optional):
 * <br>
 * - {@link #promptTemplate}: The prompt template used to instruct the LLM to expand the provided {@link Query}.
 * <br>
 * - {@link #n}: The number of {@link Query}s to generate. Default value is 3.
 *
 * @see DefaultQueryTransformer
 * @see CompressingQueryTransformer
 */
public class ExpandingQueryTransformer implements QueryTransformer {

    /**
     * 默认的提示模版
     */
    /*
     * 生成用户提供的查询的 {{n}} 个不同版本。
     * 每个版本的措辞应不同，可使用同义词或替代的句子结构，
     * 但它们都应该保留原意。
     * 这些版本将用于检索相关文档。
     * 将每个查询版本放在单独的一行上是非常重要的，
     * 不要有列举、连字符或任何额外格式！
     * 用户查询：{{query}}
     */
    public static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = PromptTemplate.from(
            """
                    Generate {{n}} different versions of a provided user query. \
                    Each version should be worded differently, using synonyms or alternative sentence structures, \
                    but they should all retain the original meaning. \
                    These versions will be used to retrieve relevant documents. \
                    It is very important to provide each query version on a separate line, \
                    without enumerations, hyphens, or any additional formatting!
                    User query: {{query}}"""
    );
    public static final int DEFAULT_N = 3;

    /**
     * 聊天对话模型
     */
    protected final ChatModel chatModel;
    /**
     * 提示模版
     */
    protected final PromptTemplate promptTemplate;
    /**
     * N个不同版本的查询
     */
    protected final int n;

    public ExpandingQueryTransformer(ChatModel chatModel) {
        this(chatModel, DEFAULT_PROMPT_TEMPLATE, DEFAULT_N);
    }

    public ExpandingQueryTransformer(ChatModel chatModel, int n) {
        this(chatModel, DEFAULT_PROMPT_TEMPLATE, n);
    }

    public ExpandingQueryTransformer(ChatModel chatModel, PromptTemplate promptTemplate) {
        this(chatModel, ensureNotNull(promptTemplate, "promptTemplate"), DEFAULT_N);
    }

    public ExpandingQueryTransformer(ChatModel chatModel, PromptTemplate promptTemplate, Integer n) {
        this.chatModel = ensureNotNull(chatModel, "chatModel");
        this.promptTemplate = getOrDefault(promptTemplate, DEFAULT_PROMPT_TEMPLATE);
        this.n = ensureGreaterThanZero(getOrDefault(n, DEFAULT_N), "n");
    }

    public static ExpandingQueryTransformerBuilder builder() {
        return new ExpandingQueryTransformerBuilder();
    }

    @Override
    public Collection<Query> transform(Query query) {
        // 提示
        Prompt prompt = createPrompt(query);
        // 聊天对话
        String response = chatModel.chat(prompt.text());
        // 解析响应内容
        List<String> queries = parse(response);
        return queries.stream()
                .map(queryText -> query.metadata() == null
                        ? Query.from(queryText)
                        : Query.from(queryText, query.metadata()))
                .collect(toList());
    }

    protected Prompt createPrompt(Query query) {
        Map<String, Object> variables = new HashMap<>();
        // 查询文本
        variables.put("query", query.text());
        // N个不同版本的查询
        variables.put("n", n);
        return promptTemplate.apply(variables);
    }

    protected List<String> parse(String queries) {
        return stream(queries.split("\n"))
                .filter(Utils::isNotNullOrBlank)
                .collect(toList());
    }

    public static class ExpandingQueryTransformerBuilder {
        private ChatModel chatModel;
        private PromptTemplate promptTemplate;
        private Integer n;

        ExpandingQueryTransformerBuilder() {
        }

        public ExpandingQueryTransformerBuilder chatModel(ChatModel chatModel) {
            this.chatModel = chatModel;
            return this;
        }

        public ExpandingQueryTransformerBuilder promptTemplate(PromptTemplate promptTemplate) {
            this.promptTemplate = promptTemplate;
            return this;
        }

        public ExpandingQueryTransformerBuilder n(Integer n) {
            this.n = n;
            return this;
        }

        public ExpandingQueryTransformer build() {
            return new ExpandingQueryTransformer(this.chatModel, this.promptTemplate, this.n);
        }
    }
}
