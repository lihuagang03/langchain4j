package dev.langchain4j.agentic.supervisor;

import dev.langchain4j.memory.ChatMemory;

/**
 * 主管上下文策略
 * 为主管智能体提供上下文的策略。
 * Strategy for providing context to the supervisor agent.
 */
public enum SupervisorContextStrategy {
    /**
     * 仅使用主管的聊天记忆（默认）。
     * Use only the supervisors {@link ChatMemory} (default).
     */
    CHAT_MEMORY,
    /**
     * 仅使用主管与其子智能体之间互动的摘要。
     * Use only a summarization of the interaction of the supervisor with its sub-agents.
     */
    SUMMARIZATION,
    /**
     * 同时使用主管的聊天记忆和主管与其子智能体互动的总结。
     * Use both the supervisor's {@link ChatMemory} and a summarization of the interaction of the supervisor with its sub-agents.
     */
    CHAT_MEMORY_AND_SUMMARIZATION
}
