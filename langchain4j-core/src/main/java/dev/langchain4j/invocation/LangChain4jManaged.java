package dev.langchain4j.invocation;

import java.util.Map;
import dev.langchain4j.Internal;

/**
 * 一个用于由 LangChain4j 框架管理的组件的标记接口。
 * A marker interface for components that are managed by LangChain4j framework.
 * <p>
 * Implementing this interface indicates that the component is internally managed by LangChain4j,
 * and doesn't require to be instatiated or passed around by the user or LLM.
 *
 * @since 1.8.0
 */
@Internal
public interface LangChain4jManaged {

    ThreadLocal<Map<Class<? extends LangChain4jManaged>, LangChain4jManaged>> CURRENT = new ThreadLocal<>();

    static void setCurrent(Map<Class<? extends LangChain4jManaged>, LangChain4jManaged> current) {
        CURRENT.set(current);
    }

    static Map<Class<? extends LangChain4jManaged>, LangChain4jManaged> current() {
        return CURRENT.get();
    }

    static void removeCurrent() {
        CURRENT.remove();
    }
}
