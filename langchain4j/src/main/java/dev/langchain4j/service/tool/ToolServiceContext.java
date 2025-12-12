package dev.langchain4j.service.tool;

import dev.langchain4j.Internal;
import dev.langchain4j.agent.tool.ToolSpecification;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 工具服务的上下文
 */
@Internal
public class ToolServiceContext {

    /**
     * 工具规格列表
     */
    private final List<ToolSpecification> toolSpecifications;
    /**
     * 工具名称到工具执行器的映射表
     */
    private final Map<String, ToolExecutor> toolExecutors;

    public ToolServiceContext(List<ToolSpecification> toolSpecifications, Map<String, ToolExecutor> toolExecutors) {
        this.toolSpecifications = toolSpecifications;
        this.toolExecutors = toolExecutors;
    }

    public List<ToolSpecification> toolSpecifications() {
        return toolSpecifications;
    }

    public Map<String, ToolExecutor> toolExecutors() {
        return toolExecutors;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ToolServiceContext) obj;
        return Objects.equals(this.toolSpecifications, that.toolSpecifications) &&
                Objects.equals(this.toolExecutors, that.toolExecutors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toolSpecifications, toolExecutors);
    }

    @Override
    public String toString() {
        return "ToolServiceContext[" +
                "toolSpecifications=" + toolSpecifications + ", " +
                "toolExecutors=" + toolExecutors + ']';
    }

    public static class Empty extends ToolServiceContext {

        public static final Empty INSTANCE = new Empty();

        private Empty() {
            super(List.of(), Map.of());
        }
    }
}
