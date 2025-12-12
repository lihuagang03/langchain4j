package dev.langchain4j.memory.chat;

import static dev.langchain4j.internal.ValidationUtils.ensureGreaterThanZero;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.service.memory.ChatMemoryService;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;

/**
 * 消息窗口的聊天记忆
 * 此聊天记忆以最大消息数为滑动窗口进行操作。
 * 它会保留尽可能多的最近消息，以适应窗口的容量。
 * 如果没有足够的空间容纳新消息，最旧的消息将被移除。
 * This chat memory operates as a sliding window of {@link #maxMessages} messages.
 * It retains as many of the most recent messages as can fit into the window.
 * If there isn't enough space for a new message, the oldest one is evicted.
 * <p>
 * 一旦添加，系统消息将始终保留。
 * 一次只能保留一条系统消息。
 * 如果添加了内容相同的新系统消息，将被忽略。
 * 如果添加了内容不同的新系统消息，则会删除之前的系统消息。
 * Once added, a {@link SystemMessage} is always retained.
 * Only one {@code SystemMessage} can be held at a time.
 * If a new {@code SystemMessage} with the same content is added, it is ignored.
 * If a new {@code SystemMessage} with different content is added, the previous {@code SystemMessage} is removed.
 * <p>
 * 如果包含 ToolExecutionRequest 的 AiMessage 被驱逐，以下孤立的 ToolExecutionResultMessage 也会被自动驱逐，
 * 以避免一些 LLM 提供商（例如 OpenAI）禁止在请求中发送孤立的 ToolExecutionResultMessage 所带来的问题。
 * If an {@link AiMessage} containing {@link ToolExecutionRequest}(s) is evicted,
 * the following orphan {@link ToolExecutionResultMessage}(s) are also automatically evicted
 * to avoid problems with some LLM providers (such as OpenAI)
 * that prohibit sending orphan {@code ToolExecutionResultMessage}(s) in the request.
 * <p>
 * 聊天记忆的状态存储在 ChatMemoryStore 中（默认使用 SingleSlotChatMemoryStore）。
 * The state of chat memory is stored in {@link ChatMemoryStore} ({@link SingleSlotChatMemoryStore} is used by default).
 */
public class MessageWindowChatMemory implements ChatMemory {

    /**
     * 聊天记忆ID
     */
    private final Object id;
    /**
     * 最大聊天消息数量
     */
    private final Integer maxMessages;
    /**
     * 聊天记忆存储
     */
    private final ChatMemoryStore store;

    private MessageWindowChatMemory(Builder builder) {
        this.id = ensureNotNull(builder.id, "id");
        this.maxMessages = ensureGreaterThanZero(builder.maxMessages, "maxMessages");
        this.store = ensureNotNull(builder.store(), "store");
    }

    @Override
    public Object id() {
        return id;
    }

    @Override
    public void add(ChatMessage message) {
        // 聊天消息列表
        List<ChatMessage> messages = messages();
        if (message instanceof SystemMessage) {
            // 系统消息
            Optional<SystemMessage> systemMessage = SystemMessage.findFirst(messages);
            if (systemMessage.isPresent()) {
                if (systemMessage.get().equals(message)) {
                    return; // do not add the same system message
                } else {
                    messages.remove(systemMessage.get()); // need to replace existing system message
                }
            }
        }
        messages.add(message);
        // 确保容量
        ensureCapacity(messages, maxMessages);
        store.updateMessages(id, messages);
    }

    @Override
    public List<ChatMessage> messages() {
        List<ChatMessage> messages = new LinkedList<>(store.getMessages(id));
        // 确保容量
        ensureCapacity(messages, maxMessages);
        return messages;
    }

    private static void ensureCapacity(List<ChatMessage> messages, int maxMessages) {
        while (messages.size() > maxMessages) {

            int messageToEvictIndex = 0;
            if (messages.get(0) instanceof SystemMessage) {
                messageToEvictIndex = 1;
            }

            ChatMessage evictedMessage = messages.remove(messageToEvictIndex);
            if (evictedMessage instanceof AiMessage aiMessage && aiMessage.hasToolExecutionRequests()) {
                while (messages.size() > messageToEvictIndex
                        && messages.get(messageToEvictIndex) instanceof ToolExecutionResultMessage) {
                    // Some LLMs (e.g. OpenAI) prohibit ToolExecutionResultMessage(s) without corresponding AiMessage,
                    // so we have to automatically evict orphan ToolExecutionResultMessage(s) if AiMessage was evicted
                    messages.remove(messageToEvictIndex);
                }
            }
        }
    }

    @Override
    public void clear() {
        store.deleteMessages(id);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Object id = ChatMemoryService.DEFAULT;
        private Integer maxMessages;
        private ChatMemoryStore store;

        /**
         * @param id The ID of the {@link ChatMemory}.
         *           If not provided, a "default" will be used.
         * @return builder
         */
        public Builder id(Object id) {
            this.id = id;
            return this;
        }

        /**
         * @param maxMessages The maximum number of messages to retain.
         *                    If there isn't enough space for a new message, the oldest one is evicted.
         * @return builder
         */
        public Builder maxMessages(Integer maxMessages) {
            this.maxMessages = maxMessages;
            return this;
        }

        /**
         * @param store The chat memory store responsible for storing the chat memory state.
         *              If not provided, an {@link SingleSlotChatMemoryStore} will be used.
         * @return builder
         */
        public Builder chatMemoryStore(ChatMemoryStore store) {
            this.store = store;
            return this;
        }

        private ChatMemoryStore store() {
            return store != null ? store : new SingleSlotChatMemoryStore(id);
        }

        public MessageWindowChatMemory build() {
            return new MessageWindowChatMemory(this);
        }
    }

    public static MessageWindowChatMemory withMaxMessages(int maxMessages) {
        return builder().maxMessages(maxMessages).build();
    }
}
