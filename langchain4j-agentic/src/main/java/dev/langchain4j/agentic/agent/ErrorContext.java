package dev.langchain4j.agentic.agent;

import dev.langchain4j.agentic.scope.AgenticScope;

/**
 * 错误上下文
 * @param agentName 代理名称
 * @param agenticScope 代理范围
 * @param exception 代理调用异常
 */
public record ErrorContext(String agentName, AgenticScope agenticScope, AgentInvocationException exception) {
}
