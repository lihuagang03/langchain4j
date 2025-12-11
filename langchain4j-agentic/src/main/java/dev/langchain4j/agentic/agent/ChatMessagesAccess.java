package dev.langchain4j.agentic.agent;

import dev.langchain4j.data.message.UserMessage;

/**
 * 聊天消息访问
 */
public interface ChatMessagesAccess {
    /**
     * 最新的用户消息
     */
    UserMessage lastUserMessage();
}
