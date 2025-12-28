package dev.langchain4j.agentic.agent;

import dev.langchain4j.agentic.scope.AgenticScope;
import java.util.Map;

/**
 * 智能体响应
 * @param agenticScope 智能体自主范围
 * @param agentName 智能体名称
 * @param inputs 输入参数的映射表
 * @param output 输出结果
 */
public record AgentResponse(AgenticScope agenticScope, String agentName, Map<String, Object> inputs, Object output) {
}
