package dev.langchain4j.agentic.scope;

/**
 * 智能体自主范围和调用结果
 * 保存代理调用的结果以及其相关的 AgenticScope。
 * 这对于在返回代理结果的同时，提供对生成该结果的认知范围的访问非常有用。
 * Holds the result of an agent invocation along with its associated {@link AgenticScope}.
 * This is useful for returning results from agents while also providing access to the cognitive
 * scope through which that result has been generated.
 *
 * @param <T> The type of the result.
 */
public record ResultWithAgenticScope<T>(AgenticScope agenticScope, T result) { }
