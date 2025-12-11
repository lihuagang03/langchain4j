package dev.langchain4j.agentic.a2a;

import dev.langchain4j.agentic.internal.AgentSpecification;
import io.a2a.spec.AgentCard;

/**
 * A2A客户端规范
 */
public interface A2AClientSpecification extends AgentSpecification {

    /**
     * 输入变量的键的列表
     */
    String[] inputKeys();

    /**
     * 智能体卡片
     */
    AgentCard agentCard();
}
