package dev.langchain4j.rag.query.transformer;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.input.Prompt;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.rag.query.Query;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

/**
 * 压缩查询转换器，利用聊天对话模型将给定的查询以及聊天记忆（之前的对话历史）压缩成一个简明的查询。
 * 只有在使用聊天记忆时才适用。
 * A {@link QueryTransformer} that leverages a {@link ChatModel} to condense a given {@link Query}
 * along with a chat memory (previous conversation history) into a concise {@link Query}.
 * This is applicable only when a {@link ChatMemory} is in use.
 * Refer to {@link #DEFAULT_PROMPT_TEMPLATE} and implementation for more details.
 * <br>
 * <br>
 * Configurable parameters (optional):
 * <br>
 * - {@link #promptTemplate}: The prompt template used to instruct the LLM to compress the specified {@link Query}.
 *
 * @see DefaultQueryTransformer
 * @see ExpandingQueryTransformer
 */
public class CompressingQueryTransformer implements QueryTransformer {

    /**
     * 默认的提示模版
     */
    /*
     * 阅读并理解用户与人工智能之间的对话。
     * 然后，分析用户的新查询。
     * 从对话和新查询中识别所有相关的细节、术语和上下文。
     * 将此查询重新表述为清晰、简明且自包含的格式，以便信息检索。
     *
     * 对话：
     * {{chatMemory}}
     *
     * 用户查询：{{query}}
     *
     * 非常重要的是，你只能提供重新表述的查询，别的不要提供！
     * 不要在查询前加上任何内容！
     */
    public static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = PromptTemplate.from(
            """
                    Read and understand the conversation between the User and the AI. \
                    Then, analyze the new query from the User. \
                    Identify all relevant details, terms, and context from both the conversation and the new query. \
                    Reformulate this query into a clear, concise, and self-contained format suitable for information retrieval.
                    
                    Conversation:
                    {{chatMemory}}
                    
                    User query: {{query}}
                    
                    It is very important that you provide only reformulated query and nothing else! \
                    Do not prepend a query with anything!"""
    );

    /**
     * 提示模版
     */
    protected final PromptTemplate promptTemplate;
    /**
     * 聊天对话模型
     */
    protected final ChatModel chatModel;

    public CompressingQueryTransformer(ChatModel chatModel) {
        this(chatModel, DEFAULT_PROMPT_TEMPLATE);
    }

    public CompressingQueryTransformer(ChatModel chatModel, PromptTemplate promptTemplate) {
        this.chatModel = ensureNotNull(chatModel, "chatModel");
        this.promptTemplate = getOrDefault(promptTemplate, DEFAULT_PROMPT_TEMPLATE);
    }

    public static CompressingQueryTransformerBuilder builder() {
        return new CompressingQueryTransformerBuilder();
    }

    @Override
    public Collection<Query> transform(Query query) {

        // 聊天记忆
        List<ChatMessage> chatMemory = query.metadata().chatMemory();
        if (chatMemory.isEmpty()) {
            // no need to compress if there are no previous messages
            return singletonList(query);
        }

        // 提示
        Prompt prompt = createPrompt(query, format(chatMemory));
        String compressedQueryText = chatModel.chat(prompt.text());
        Query compressedQuery = query.metadata() == null
                ? Query.from(compressedQueryText)
                : Query.from(compressedQueryText, query.metadata());
        return singletonList(compressedQuery);
    }

    protected String format(List<ChatMessage> chatMemory) {
        // 聊天对话消息列表，分隔符：换行
        return chatMemory.stream()
                .map(this::format)
                .filter(Objects::nonNull)
                .collect(joining("\n"));
    }

    protected String format(ChatMessage message) {
        // 聊天对话消息
        if (message instanceof UserMessage userMessage) {
            //
            return "User: " + userMessage.singleText();
        } else if (message instanceof AiMessage aiMessage) {
            // AI消息，包含工具执行请求列表
            if (aiMessage.hasToolExecutionRequests()) {
                // 工具执行请求列表
                return null;
            }
            // AI消息
            return "AI: " + aiMessage.text();
        } else {
            return null;
        }
    }

    protected Prompt createPrompt(Query query, String chatMemory) {
        Map<String, Object> variables = new HashMap<>();
        // 查询文本
        variables.put("query", query.text());
        // 聊天记忆
        variables.put("chatMemory", chatMemory);
        return promptTemplate.apply(variables);
    }

    public static class CompressingQueryTransformerBuilder {
        private ChatModel chatModel;
        private PromptTemplate promptTemplate;

        CompressingQueryTransformerBuilder() {
        }

        public CompressingQueryTransformerBuilder chatModel(ChatModel chatModel) {
            this.chatModel = chatModel;
            return this;
        }

        public CompressingQueryTransformerBuilder promptTemplate(PromptTemplate promptTemplate) {
            this.promptTemplate = promptTemplate;
            return this;
        }

        public CompressingQueryTransformer build() {
            return new CompressingQueryTransformer(this.chatModel, this.promptTemplate);
        }
    }
}
