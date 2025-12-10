package dev.langchain4j.service.tool;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.invocation.InvocationParameters;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.service.MemoryId;

/**
 * 工具提供者
 * 每次调用 AI 服务时都会调用它，并为该特定调用提供工具。
 * A tool provider. It is called each time the AI service is called and supplies tools for that specific call.
 * <p>
 * Tools returned in {@link ToolProviderResult} will be included in the request to the LLM.
 **/
@FunctionalInterface
public interface ToolProvider {

    /**
     * 提供用于向大语言模型请求的工具。
     * Provides tools for the request to the LLM.
     *
     * @param request the {@link ToolProviderRequest}, contains {@link UserMessage},
     *                {@link ChatMemory} ID (see {@link MemoryId}) and {@link InvocationParameters}.
     * @return {@link ToolProviderResult} contains tools that should be included in the request to the LLM.
     */
    ToolProviderResult provideTools(ToolProviderRequest request);
}
