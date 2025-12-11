package dev.langchain4j.agentic.agent;

import dev.langchain4j.agentic.scope.AgenticScope;

/**
 * 错误上下文
 * @param agentName 智能体名称
 * @param agenticScope 智能体自主范围
 * @param exception 智能体调用异常
 */
public record ErrorContext(String agentName, AgenticScope agenticScope, AgentInvocationException exception) {
}
