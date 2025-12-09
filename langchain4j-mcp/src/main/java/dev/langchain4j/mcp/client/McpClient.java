package dev.langchain4j.mcp.client;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.service.tool.ToolExecutionResult;
import java.util.List;
import java.util.Map;

/**
 * MCP客户端
 * 表示可以通过指定的传输协议与 MCP 服务器通信的客户端，使用服务器检索并执行工具。
 * Represents a client that can communicate with an MCP server over a given transport protocol,
 * retrieve and execute tools using the server.
 */
public interface McpClient extends AutoCloseable {

    /**
     * 返回此客户端的唯一密钥。
     * Returns the unique key of this client.
     */
    String key();

    /**
     * 从MCP服务器获取工具列表。
     * Obtains a list of tools from the MCP server.
     */
    List<ToolSpecification> listTools();

    /**
     * 在 MCP 服务器上执行工具并返回结果。
     * 目前，这要求工具执行只能包含基于文本的结果或 JSON 结构的内容。
     * Executes a tool on the MCP server and returns the result.
     * Currently, this expects a tool execution to only contain text-based results or JSON structured content.
     */
    ToolExecutionResult executeTool(ToolExecutionRequest executionRequest);

    /**
     * 获取 MCP 服务器上当前可用的资源列表。
     * Obtains the current list of resources available on the MCP server.
     */
    List<McpResource> listResources();

    /**
     * 获取 MCP 服务器上当前可用的资源模板（动态资源）列表。
     * Obtains the current list of resource templates (dynamic resources) available on the MCP server.
     */
    List<McpResourceTemplate> listResourceTemplates();

    /**
     * Retrieves the contents of the resource with the specified URI. This also
     * works for dynamic resources (templates).
     */
    McpReadResourceResult readResource(String uri);

    /**
     * 获取 MCP 服务器上可用的提示列表。
     * Obtain a list of prompts available on the MCP server.
     */
    List<McpPrompt> listPrompts();

    /**
     * Render the contents of a prompt.
     */
    McpGetPromptResult getPrompt(String name, Map<String, Object> arguments);

    /**
     * 执行健康检查，如果 MCP 服务器可访问并能正常响应 ping 请求，则检查返回正常。
     * 如果此方法抛出异常，则认为该 MCP 客户端的健康状况已降低。
     * Performs a health check that returns normally if the MCP server is reachable and
     * properly responding to ping requests. If this method throws an exception,
     * the health of this MCP client is considered degraded.
     */
    void checkHealth();

    /**
     * Sets the roots that are made available to the server upon its request.
     * After calling this method, the client also sends a `notifications/roots/list_changed` message to the server.
     */
    void setRoots(List<McpRoot> roots);
}
