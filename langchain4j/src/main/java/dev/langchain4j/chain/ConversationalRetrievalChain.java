package dev.langchain4j.chain;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.rag.AugmentationRequest;
import dev.langchain4j.rag.AugmentationResult;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Metadata;
import dev.langchain4j.service.AiServices;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * 对话检索的链步骤
 * A chain for conversing with a specified {@link ChatModel}
 * based on the information retrieved by a specified {@link ContentRetriever}.
 * Includes a default {@link ChatMemory} (a message window with maximum 10 messages), which can be overridden.
 * You can fully customize RAG behavior by providing an instance of a {@link RetrievalAugmentor},
 * such as {@link DefaultRetrievalAugmentor}, or your own custom implementation.
 * <br>
 * Chains are not going to be developed further, it is recommended to use {@link AiServices} instead.
 */
public class ConversationalRetrievalChain implements Chain<String, String> {

    /**
     * 聊天模型
     */
    private final ChatModel chatModel;
    /**
     * 聊天记忆
     */
    private final ChatMemory chatMemory;
    /**
     * 检索增强器
     */
    private final RetrievalAugmentor retrievalAugmentor;

    public ConversationalRetrievalChain(ChatModel chatModel,
                                        ChatMemory chatMemory,
                                        ContentRetriever contentRetriever) {
        // 检索增强器的默认实现
        this(
                chatModel,
                chatMemory,
                DefaultRetrievalAugmentor.builder()
                        .contentRetriever(contentRetriever)
                        .build()
        );
    }

    public ConversationalRetrievalChain(ChatModel chatModel,
                                        ChatMemory chatMemory,
                                        RetrievalAugmentor retrievalAugmentor) {
        this.chatModel = ensureNotNull(chatModel, "chatModel");
        // 默认是 消息窗口的聊天记忆
        this.chatMemory = getOrDefault(chatMemory, () -> MessageWindowChatMemory.withMaxMessages(10));
        this.retrievalAugmentor = ensureNotNull(retrievalAugmentor, "retrievalAugmentor");
    }

    @Override
    public String execute(String query) {

        // 用户消息
        UserMessage userMessage = UserMessage.from(query);
        // 检索增强
        userMessage = augment(userMessage);
        // 添加用户消息到聊天记忆
        chatMemory.add(userMessage);

        // 与 AI 聊天
        AiMessage aiMessage = chatModel.chat(chatMemory.messages()).aiMessage();

        // 添加AI消息到聊天记忆
        chatMemory.add(aiMessage);

        return aiMessage.text();
    }

    /**
     * 检索增强用户消息
     * @param userMessage 用户消息
     * @return 增强的聊天消息
     */
    private UserMessage augment(UserMessage userMessage) {
        // 元数据
        Metadata metadata = Metadata.from(userMessage, chatMemory.id(), chatMemory.messages());

        // 增强请求
        AugmentationRequest augmentationRequest = new AugmentationRequest(userMessage, metadata);

        // 使用检索到的内容增强增强请求中提供的聊天对话消息
        AugmentationResult augmentationResult = retrievalAugmentor.augment(augmentationRequest);

        // 增强的聊天消息
        return (UserMessage) augmentationResult.chatMessage();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private ChatModel chatModel;
        private ChatMemory chatMemory;
        private RetrievalAugmentor retrievalAugmentor;

        public Builder chatModel(ChatModel chatModel) {
            this.chatModel = chatModel;
            return this;
        }

        public Builder chatMemory(ChatMemory chatMemory) {
            this.chatMemory = chatMemory;
            return this;
        }

        public Builder contentRetriever(ContentRetriever contentRetriever) {
            if (contentRetriever != null) {
                this.retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                        .contentRetriever(contentRetriever)
                        .build();
            }
            return this;
        }

        public Builder retrievalAugmentor(RetrievalAugmentor retrievalAugmentor) {
            this.retrievalAugmentor = retrievalAugmentor;
            return this;
        }

        public ConversationalRetrievalChain build() {
            return new ConversationalRetrievalChain(chatModel, chatMemory, retrievalAugmentor);
        }
    }
}
