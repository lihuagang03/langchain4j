package dev.langchain4j.agentic.internal;

import dev.langchain4j.Internal;
import dev.langchain4j.agentic.scope.AgenticScopeRegistry;
import dev.langchain4j.agentic.scope.DefaultAgenticScope;

/**
 * 智能体自主范围的所有者
 */
@Internal
public interface AgenticScopeOwner {
    /**
     * @param agenticScope 智能体自主范围
     */
    AgenticScopeOwner withAgenticScope(DefaultAgenticScope agenticScope);

    /**
     * 智能体自主范围的注册表
     */
    AgenticScopeRegistry registry();
}
