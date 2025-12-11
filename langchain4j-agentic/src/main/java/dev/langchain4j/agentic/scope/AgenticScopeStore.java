package dev.langchain4j.agentic.scope;

import java.util.Optional;
import java.util.Set;

/**
 * 智能体自主范围的存储
 * 用于 AgenticScope 持久化的服务提供者接口。
 * 实现必须提供存储和检索 AgenticScope 实例的方法。
 * Service Provider Interface for AgenticScope persistence.
 * Implementations must provide ways to store and retrieve AgenticScope instances.
 */
public interface AgenticScopeStore {

    /**
     * 保存或更新一个 AgenticScope 实例。
     * Saves or updates a AgenticScope instance.
     *
     * @param agenticScope the AgenticScope to persist
     * @return true if the operation was successful
     */
    boolean save(AgenticScopeKey key, DefaultAgenticScope agenticScope);

    /**
     * 通过其 ID 加载一个 AgenticScope。
     * Loads a AgenticScope by its ID.
     *
     * @param key the ID of the AgenticScope to load
     * @return an Optional containing the AgenticScope if found, empty otherwise
     */
    Optional<DefaultAgenticScope> load(AgenticScopeKey key);

    /**
     * 通过其 ID 删除一个 AgenticScope。
     * Deletes a AgenticScope by its ID.
     *
     * @param key the ID of the AgenticScope to delete
     * @return true if the AgenticScope was found and deleted
     */
    boolean delete(AgenticScopeKey key);

    /**
     * Gets all available AgenticScope .
     *
     * @return a Set of all AgenticScope keys in the persistence store
     */
    Set<AgenticScopeKey> getAllKeys();
}
