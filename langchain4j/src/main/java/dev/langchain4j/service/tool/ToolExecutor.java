package dev.langchain4j.service.tool;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.invocation.InvocationContext;
import dev.langchain4j.invocation.InvocationParameters;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.service.MemoryId;

/**
 * 工具执行器/处理器
 * A low-level executor/handler of a {@link ToolExecutionRequest}.
 */
@FunctionalInterface
public interface ToolExecutor {

    /**
     * 执行工具请求。
     * Executes a tool request.
     *
     * @param request  The tool execution request. Contains tool name and arguments.
     * @param memoryId The ID of the chat memory. .
     * @return The result of the tool execution that will be sent to the LLM.
     */
    String execute(ToolExecutionRequest request, Object memoryId);

    /**
     * 使用上下文执行工具请求。
     * Executes a tool request. Override this method if you wish to:
     * <pre>
     * - access the {@link InvocationParameters} when passing extra data into the tool
     * - propagate the tool result object ({@link ToolExecutionResult#result()}) into the {@link ToolExecution}
     * </pre>
     *
     * @param request The tool execution request. Contains tool name and arguments.
     * @param context The AI Service invocation context, contains {@link ChatMemory} ID
     *                (see {@link MemoryId} for more details), and {@link InvocationParameters}.
     * @return The result of the tool execution that will be sent to the LLM.
     */
    default ToolExecutionResult executeWithContext(ToolExecutionRequest request, InvocationContext context) {
        // 聊天记忆ID
        Object memoryId = context == null ? null : context.chatMemoryId();

        // 执行工具请求
        String result = execute(request, memoryId);

        return ToolExecutionResult.builder()
                .resultText(result)
                .build();
    }
}
