package dev.langchain4j.agentic.agent;

import dev.langchain4j.agentic.scope.AgenticScope;
import java.util.Map;

/**
 * 代理响应
 * @param agenticScope 代理范围
 * @param agentName 代理名称
 * @param inputs 输入参数
 * @param output 输出结果
 */
public record AgentResponse(AgenticScope agenticScope, String agentName, Map<String, Object> inputs, Object output) {
}
