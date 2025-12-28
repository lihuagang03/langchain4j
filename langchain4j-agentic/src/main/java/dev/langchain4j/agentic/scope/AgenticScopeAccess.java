package dev.langchain4j.agentic.scope;

/**
 * 智能体自主范围的访问
 * Allow to access the {@link AgenticScope} of any agent extending it.
 */
public interface AgenticScopeAccess {

    /**
     * 返回具有给定聊天记忆 ID 的 AgenticScope，用于此 AI 服务，
     * 如果此类内存不存在，则返回 null。
     * Returns the {@link AgenticScope} with the given id for this AI service or null if such memory doesn't exist.
     *
     * @param memoryId The id of the {@link AgenticScope}.
     * @return The {@link AgenticScope} with the given memoryId or null if such memory doesn't exist.
     */
    AgenticScope getAgenticScope(Object memoryId);

    /**
     * 驱逐具有给定聊天记忆 ID 的 AgenticScope。
     * Evicts the {@link AgenticScope} with the given id.
     *
     * @param memoryId The id of the {@link AgenticScope} to be evicted.
     * @return true if {@link AgenticScope} with the given id existed, and it was successfully evicted, false otherwise.
     */
    boolean evictAgenticScope(Object memoryId);
}
