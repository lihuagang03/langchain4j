package dev.langchain4j.agentic.supervisor;

/**
 * 主管响应策略
 * 决定主管智能体应返回哪个响应的策略。
 * Strategy to decide which response the supervisor agent should return.
 */
public enum SupervisorResponseStrategy {
    /**
     * 使用内部大语言模型对上一次回复以及主管与其子智能体互动的总结进行评分，评估其是否符合原始用户请求，并返回得分较高的那一个。
     * Use an internal LLM to score the last response and the summarization of the interaction of the supervisor
     * with its sub-agents against the original user request, and return the one with the higher score.
     */
    SCORED,
    /**
     * 返回主管与其子智能体互动的总结。
     * Return a summarization of the interaction of the supervisor with its sub-agents.
     */
    SUMMARY,
    /**
     * 仅返回最后调用的子智能体的最终响应（默认）。
     * Return only the final response of the last invoked sub-agent (default).
     */
    LAST
}
