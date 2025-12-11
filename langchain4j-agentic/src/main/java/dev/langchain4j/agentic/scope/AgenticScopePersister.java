package dev.langchain4j.agentic.scope;

import java.util.ServiceLoader;

/**
 * 智能体自主范围的持久化
 */
public enum AgenticScopePersister {

    INSTANCE;

    /**
     * 智能体自主范围的存储
     */
    static AgenticScopeStore store;

    AgenticScopePersister() {
        setStore(loadStore());
    }

    private static AgenticScopeStore loadStore() {
        // SPI
        ServiceLoader<AgenticScopeStore> loader =
                ServiceLoader.load(AgenticScopeStore.class);

        for (AgenticScopeStore provider : loader) {
            return provider; // Return the first provider found
        }
        return null; // No provider found
    }

    /**
     * Explicitly set a persistence provider.
     */
    public static void setStore(AgenticScopeStore store) {
        AgenticScopePersister.store = store;
    }
}
