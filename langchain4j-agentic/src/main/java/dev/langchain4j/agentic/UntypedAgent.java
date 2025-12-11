package dev.langchain4j.agentic;

import dev.langchain4j.agentic.scope.AgenticScopeAccess;
import dev.langchain4j.agentic.scope.ResultWithAgenticScope;

import java.util.Map;

/**
 * 无类型的智能体
 */
public interface UntypedAgent extends AgenticScopeAccess {
    /**
     * 调用智能体
     * @param input 输入参数
     */
    @Agent
    Object invoke(Map<String, Object> input);

    /**
     * 智能体自主范围和调用结果
     * @param input 输入参数
     */
    ResultWithAgenticScope<String> invokeWithAgenticScope(Map<String, Object> input);
}
