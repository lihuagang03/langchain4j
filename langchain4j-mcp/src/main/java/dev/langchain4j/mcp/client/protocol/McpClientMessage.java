package dev.langchain4j.mcp.client.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.langchain4j.Internal;

/**
 * MCP客户端消息
 */
@Internal
public class McpClientMessage {

    @JsonInclude
    public final String jsonrpc = "2.0";

    /**
     * 消息ID
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;

    public McpClientMessage(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }
}
