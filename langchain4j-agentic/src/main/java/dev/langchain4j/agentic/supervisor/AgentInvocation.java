package dev.langchain4j.agentic.supervisor;

import java.util.Map;

/**
 * 智能体调用
 */
public class AgentInvocation {

    /**
     * 智能体名称
     */
    private String agentName;
    /**
     * 输入参数的映射表
     */
    private Map<String, String> arguments;

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(final String agentName) {
        this.agentName = agentName;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    public void setArguments(final Map<String, String> arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return "AgentInvocation{" +
                "agentName='" + agentName + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}
