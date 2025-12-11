package dev.langchain4j.agentic.internal;

import java.util.Map;

/**
 * 智能体调用
 * 表示代理的调用
 * @param agentName 智能体名称
 * @param input 输入参数
 * @param output 输出结果
 */
public record AgentInvocation(String agentName, Map<String, Object> input, Object output) {

    @Override
    public Object output() {
        return output instanceof AsyncResponse<?> asyncResponse ? asyncResponse.result() : output;
    }

    @Override
    public String toString() {
        return "AgentInvocation{" +
                "agentName=" + agentName +
                ", input=" + input +
                ", output=" + output +
                '}';
    }
}
