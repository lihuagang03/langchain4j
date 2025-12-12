package dev.langchain4j.service;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.output.FinishReason;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.service.tool.ToolExecution;
import java.util.List;

import static dev.langchain4j.internal.Utils.copy;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * AI服务调用的结果
 * 表示 AI 服务调用的结果。
 * 它包含实际内容（大型语言模型的响应）以及与之相关的附加信息，
 * Represents the result of an AI Service invocation.
 * It contains actual content (LLM response) and additional information associated with it,
 * such as:
 * <pre>
 * - Aggregate {@link TokenUsage} over all calls to the {@link ChatModel}
 *   对所有聊天模型调用的词元使用量进行汇总
 * - {@link FinishReason} of the final {@link ChatResponse}
 *   最终聊天响应的完成原因
 * - sources ({@link Content}s) retrieved during RAG retrieval
 *   在RAG检索过程中获取的来源（内容）
 * - all executed tools (both requests and results)
 *   所有执行过的工具（包括请求和结果）
 * - all intermediate {@link ChatResponse}s
 *   所有中间聊天响应
 * - final {@link ChatResponse}
 *   最终聊天响应
 * </pre>
 *
 * @param <T> The type of the content. Can be of any return type supported by AI Services,
 *            such as String, Enum, MyCustomPojo, etc.
 */
public class Result<T> {

    /**
     * 结果内容
     */
    private final T content;
    /**
     * 对所有聊天模型调用的词元使用量进行汇总
     */
    private final TokenUsage tokenUsage;
    /**
     * 在RAG检索过程中获取的来源（内容）
     */
    private final List<Content> sources;
    /**
     * 最终聊天响应的完成原因
     */
    private final FinishReason finishReason;
    /**
     * 所有执行过的工具（包括请求和结果）
     */
    private final List<ToolExecution> toolExecutions;
    /**
     * 所有中间聊天响应
     */
    private final List<ChatResponse> intermediateResponses;
    /**
     * 最终聊天响应
     */
    private final ChatResponse finalResponse;

    /**
     * @since 1.2.0
     */
    public Result(ResultBuilder<T> builder) {
        this.content = builder.content;
        this.tokenUsage = builder.tokenUsage;
        this.sources = copy(builder.sources);
        this.finishReason = builder.finishReason;
        this.toolExecutions = copy(builder.toolExecutions);
        this.intermediateResponses = copy(builder.intermediateResponses);
        this.finalResponse = builder.finalResponse;
    }

    public Result(T content,
                  TokenUsage tokenUsage,
                  List<Content> sources,
                  FinishReason finishReason,
                  List<ToolExecution> toolExecutions) {
        this.content = content;
        this.tokenUsage = tokenUsage;
        this.sources = copy(sources);
        this.finishReason = finishReason;
        this.toolExecutions = copy(toolExecutions);
        this.intermediateResponses = List.of();
        this.finalResponse = null;
    }

    public static <T> ResultBuilder<T> builder() {
        return new ResultBuilder<>();
    }

    public T content() {
        return content;
    }

    /**
     * Returns aggregate token usage over all calls to the {@link ChatModel}.
     */
    public TokenUsage tokenUsage() {
        return tokenUsage;
    }

    /**
     * Returns all sources returned during RAG retrieval.
     */
    public List<Content> sources() {
        return sources;
    }

    /**
     * Returns finish reason of the final {@link ChatModel} response (taken from {@link #finalResponse()}).
     */
    public FinishReason finishReason() {
        return finishReason;
    }

    /**
     * Returns all tool executions that happened during AI Service invocation.
     */
    public List<ToolExecution> toolExecutions() {
        return toolExecutions;
    }

    /**
     * Returns all intermediate chat responses that were returned by the {@link ChatModel}.
     * All of these responses contain {@link ToolExecutionRequest}s.
     *
     * @since 1.2.0
     */
    public List<ChatResponse> intermediateResponses() {
        return intermediateResponses;
    }

    /**
     * Returns final chat response returned by the {@link ChatModel}.
     * This response does not contain {@link ToolExecutionRequest}s.
     *
     * @since 1.2.0
     */
    public ChatResponse finalResponse() {
        return finalResponse;
    }

    public static class ResultBuilder<T> {

        private T content;
        private TokenUsage tokenUsage;
        private List<Content> sources;
        private FinishReason finishReason;
        private List<ToolExecution> toolExecutions;
        private List<ChatResponse> intermediateResponses;
        private ChatResponse finalResponse;

        ResultBuilder() {
        }

        public ResultBuilder<T> content(T content) {
            this.content = content;
            return this;
        }

        public ResultBuilder<T> tokenUsage(TokenUsage tokenUsage) {
            this.tokenUsage = tokenUsage;
            return this;
        }

        public ResultBuilder<T> sources(List<Content> sources) {
            this.sources = sources;
            return this;
        }

        public ResultBuilder<T> finishReason(FinishReason finishReason) {
            this.finishReason = finishReason;
            return this;
        }

        public ResultBuilder<T> toolExecutions(List<ToolExecution> toolExecutions) {
            this.toolExecutions = toolExecutions;
            return this;
        }

        /**
         * @since 1.2.0
         */
        public ResultBuilder<T> intermediateResponses(List<ChatResponse> intermediateResponses) {
            this.intermediateResponses = intermediateResponses;
            return this;
        }

        /**
         * @since 1.2.0
         */
        public ResultBuilder<T> finalResponse(ChatResponse finalResponse) {
            this.finalResponse = finalResponse;
            return this;
        }

        public Result<T> build() {
            return new Result<>(this);
        }
    }
}
