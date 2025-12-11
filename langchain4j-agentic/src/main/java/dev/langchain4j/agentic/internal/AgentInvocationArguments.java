package dev.langchain4j.agentic.internal;

import java.util.Map;

/**
 * 智能体调用参数列表
 * @param namedArgs 命名参数
 * @param positionalArgs 位置参数
 */
public record AgentInvocationArguments(Map<String, Object> namedArgs, Object[] positionalArgs) {
}
