package dev.langchain4j.agentic.internal;

/**
 * 智能体规范的提供者
 */
public interface AgentSpecsProvider {

    /**
     * 输入变量的键
     */
    String inputKey();

    /**
     * 输出变量的键
     */
    String outputKey();

    /**
     * 智能体的描述
     */
    String description();

    /**
     * 是否异步调用
     */
    boolean async();
}
