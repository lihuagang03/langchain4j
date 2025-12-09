package dev.langchain4j.mcp.client.transport;

import com.fasterxml.jackson.databind.JsonNode;
import dev.langchain4j.mcp.client.protocol.McpClientMessage;
import dev.langchain4j.mcp.client.protocol.McpInitializeRequest;
import java.io.Closeable;
import java.util.concurrent.CompletableFuture;

/**
 * MCP传输
 */
public interface McpTransport extends Closeable {

    /**
     * 创建与 MCP 服务器的连接（如有需要，会将服务器作为子进程运行）。
     * 这还不会发送“初始化”消息以协商功能。
     * Creates a connection to the MCP server (runs the server as a subprocess if needed).
     * This does NOT yet send the "initialize" message to negotiate capabilities.
     */
    void start(McpOperationHandler messageHandler);

    /**
     * 向 MCP 服务器发送“initialize”消息以协商功能、支持的协议版本等。
     * 当此方法成功返回时，传输已完全初始化并可以使用。
     * 此方法必须在调用“start”方法之后调用。
     * Sends the "initialize" message to the MCP server to negotiate
     * capabilities, supported protocol version etc. When this method
     * returns successfully, the transport is fully initialized and ready to
     * be used. This has to be called AFTER the "start" method.
     */
    CompletableFuture<JsonNode> initialize(McpInitializeRequest request);

    /**
     * Executes an operation that expects a response from the server.
     */
    CompletableFuture<JsonNode> executeOperationWithResponse(McpClientMessage request);

    /**
     * Sends a message that does not expect a response from the server - either a
     * client-initiated notification or a response to a server-initiated request.
     */
    void executeOperationWithoutResponse(McpClientMessage request);

    /**
     * 执行特定于传输的健康检查（如适用）。
     * Performs transport-specific health checks, if applicable. This is called
     * by `McpClient.checkHealth()` as the first check before performing a check
     * by sending a 'ping' over the MCP protocol. The purpose is that the
     * transport may have some specific and faster ways to detect that it is broken,
     * like for example, the STDIO transport can fail the check if it detects
     * that the server subprocess isn't alive anymore.
     */
    void checkHealth();

    void onFailure(Runnable actionOnFailure);
}
