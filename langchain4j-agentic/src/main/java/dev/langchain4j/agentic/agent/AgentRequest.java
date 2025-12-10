package dev.langchain4j.agentic.agent;

import dev.langchain4j.agentic.scope.AgenticScope;
import java.util.Map;

/**
 * 代理请求
 * @param agenticScope 代理范围
 * @param agentName 代理名称
 * @param inputs 输入参数
 */
public record AgentRequest(AgenticScope agenticScope, String agentName, Map<String, Object> inputs) {
}
