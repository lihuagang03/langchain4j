package dev.langchain4j.agentic.agent;

import dev.langchain4j.agentic.scope.AgenticScope;
import java.util.Map;

/**
 * 智能体请求
 * @param agenticScope 智能体自主范围
 * @param agentName 智能体名称
 * @param inputs 输入参数
 */
public record AgentRequest(AgenticScope agenticScope, String agentName, Map<String, Object> inputs) {
}
