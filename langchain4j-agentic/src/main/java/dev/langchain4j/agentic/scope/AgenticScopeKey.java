package dev.langchain4j.agentic.scope;

/**
 * 智能体自主范围的键
 * @param agentId 智能体ID
 * @param memoryId 聊天记忆ID
 */
public record AgenticScopeKey(String agentId, Object memoryId) {
}
