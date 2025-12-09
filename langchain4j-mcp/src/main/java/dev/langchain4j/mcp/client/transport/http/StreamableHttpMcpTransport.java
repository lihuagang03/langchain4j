package dev.langchain4j.mcp.client.transport.http;

import static dev.langchain4j.internal.Utils.getOrDefault;
import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.mcp.client.protocol.McpClientMessage;
import dev.langchain4j.mcp.client.protocol.McpInitializationNotification;
import dev.langchain4j.mcp.client.protocol.McpInitializeRequest;
import dev.langchain4j.mcp.client.transport.McpOperationHandler;
import dev.langchain4j.mcp.client.transport.McpTransport;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MCP传输的Streamable-HTTP实现
 */
public class StreamableHttpMcpTransport implements McpTransport {

    private static final Logger DEFAULT_TRAFFIC_LOG = LoggerFactory.getLogger("MCP");
    private static final Logger LOG = LoggerFactory.getLogger(StreamableHttpMcpTransport.class);
    /**
     * MCP服务器地址
     */
    private final String url;
    /**
     * 自定义的头信息
     */
    private final Map<String, String> customHeaders;
    /**
     * 是否记录响应内容
     */
    private final boolean logResponses;
    /**
     * 是否记录请求参数
     */
    private final boolean logRequests;
    /**
     * 流量日志记录器
     */
    private final Logger trafficLog;
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final AtomicReference<CompletableFuture<JsonNode>> initializeInProgress = new AtomicReference<>(null);
    /**
     * 操作处理器
     */
    private volatile McpOperationHandler operationHandler;
    /**
     * HTTP客户端
     */
    private final HttpClient httpClient;
    /**
     * 初始化请求
     */
    private McpInitializeRequest initializeRequest;
    /**
     * 会话ID
     */
    private final AtomicReference<String> mcpSessionId = new AtomicReference<>();

    public StreamableHttpMcpTransport(StreamableHttpMcpTransport.Builder builder) {
        url = ensureNotNull(builder.url, "Missing server endpoint URL");
        logRequests = builder.logRequests;
        logResponses = builder.logResponses;
        trafficLog = getOrDefault(builder.logger, DEFAULT_TRAFFIC_LOG);
        Duration timeout = getOrDefault(builder.timeout, Duration.ofSeconds(60));
        customHeaders = getOrDefault(builder.customHeaders, Map.of());
        HttpClient.Builder clientBuilder = HttpClient.newBuilder();
        if (builder.executor != null) {
            clientBuilder.executor(builder.executor);
        }
        httpClient = clientBuilder.connectTimeout(timeout).build();
    }

    @Override
    public void start(McpOperationHandler operationHandler) {
        this.operationHandler = operationHandler;
    }

    @Override
    public CompletableFuture<JsonNode> initialize(McpInitializeRequest operation) {
        this.initializeRequest = operation;
        CompletableFuture<JsonNode> completableFuture = execute(operation, operation.getId());
        initializeInProgress.set(completableFuture);
        return completableFuture
                .thenCompose(originalResponse -> {
                    initializeInProgress.set(null);
                    return CompletableFuture.completedFuture(originalResponse);
                })
                .thenCompose(originalResponse -> execute(new McpInitializationNotification(), null)
                        .thenCompose(nullNode -> CompletableFuture.completedFuture(originalResponse)));
    }

    private HttpRequest createRequest(McpClientMessage message) throws JsonProcessingException {
        String body = OBJECT_MAPPER.writeValueAsString(message);
        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(body);
        if (logRequests) {
            trafficLog.info("Request: {}", body);
        }
        final HttpRequest.Builder builder = HttpRequest.newBuilder();
        // 会话ID
        String sessionId = mcpSessionId.get();
        if (sessionId != null && !(message instanceof McpInitializeRequest)) {
            // MCP会话ID的头信息
            builder.header("Mcp-Session-Id", sessionId);
        }
        customHeaders.forEach(builder::header);
        return builder.uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json,text/event-stream")
                .POST(bodyPublisher)
                .build();
    }

    @Override
    public CompletableFuture<JsonNode> executeOperationWithResponse(McpClientMessage operation) {
        return execute(operation, operation.getId());
    }

    @Override
    public void executeOperationWithoutResponse(McpClientMessage operation) {
        execute(operation, null);
    }

    @Override
    public void checkHealth() {
        // no transport-specific checks right now
    }

    @Override
    public void onFailure(Runnable actionOnFailure) {
        // nothing to do here, we don't maintain a long-running SSE channel (yet)
    }

    private CompletableFuture<JsonNode> execute(McpClientMessage message, Long id) {
        return execute(message, id, false);
    }

    private CompletableFuture<JsonNode> execute(McpClientMessage message, Long id, boolean isRetry) {
        CompletableFuture<JsonNode> reinitializeInProgress = this.initializeInProgress.get();
        if (reinitializeInProgress != null) {
            reinitializeInProgress.join();
        }
        HttpRequest request = null;
        try {
            request = createRequest(message);
        } catch (JsonProcessingException e) {
            return CompletableFuture.failedFuture(e);
        }
        CompletableFuture<JsonNode> future = new CompletableFuture<>();
        if (id != null) {
            operationHandler.startOperation(id, future);
        }

        httpClient
                .sendAsync(request, responseInfo -> {
                    if (!isExpectedStatusCode(responseInfo.statusCode())) {
                        if (!(message instanceof McpInitializeRequest) && responseInfo.statusCode() == 404) {
                            if (!isRetry) {
                                initialize(StreamableHttpMcpTransport.this.initializeRequest)
                                        .thenAccept(node -> {
                                            execute(message, id, true)
                                                    .thenAccept(future::complete)
                                                    .exceptionally(t -> {
                                                        future.completeExceptionally(t);
                                                        return null;
                                                    });
                                        })
                                        .exceptionally(t -> {
                                            future.completeExceptionally(t);
                                            return null;
                                        });
                            }
                        } else {
                            future.completeExceptionally(
                                    new RuntimeException("Unexpected status code: " + responseInfo.statusCode()));
                        }
                        return HttpResponse.BodySubscribers.discarding();
                    } else {
                        Optional<String> contentType = responseInfo.headers().firstValue("Content-Type");
                        // MCP会话ID的头信息
                        Optional<String> mcpSessionId = responseInfo.headers().firstValue("Mcp-Session-Id");
                        if (mcpSessionId.isPresent()) {
                            LOG.debug("Assigned MCP session ID: {}", mcpSessionId);
                            StreamableHttpMcpTransport.this.mcpSessionId.set(mcpSessionId.get());
                        }
                        if (id != null
                                && contentType.isPresent()
                                && contentType.get().contains("text/event-stream")) {
                            // the server has started an SSE stream
                            return HttpResponse.BodySubscribers.fromLineSubscriber(
                                    new SseSubscriber(future, logResponses, operationHandler, trafficLog));
                        } else {
                            // the server has returned a regular HTTP response
                            return HttpResponse.BodySubscribers.mapping(
                                    HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8), responseBody -> {
                                        if (logResponses) {
                                            trafficLog.info("Response: {}", responseBody);
                                        }
                                        if (id == null) {
                                            future.complete(null);
                                        }
                                        try {
                                            JsonNode node = OBJECT_MAPPER.readTree(responseBody);
                                            operationHandler.handle(node);
                                            return null;
                                        } catch (IOException e) {
                                            future.completeExceptionally(e);
                                            return null;
                                        }
                                    });
                        }
                    }
                })
                .exceptionally(t -> {
                    future.completeExceptionally(t);
                    return null;
                });
        return future;
    }

    private boolean isExpectedStatusCode(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    @Override
    public void close() throws IOException {
        // The httpClient.close() method only exists on JDK 21+, so invoke it only if we can.
        // Replace this with a normal method call when switching the base to JDK 21+.
        try {
            httpClient.getClass().getMethod("close").invoke(httpClient);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
    }

    public static class Builder {

        /**
         * HTTP客户端的执行器
         */
        private Executor executor;
        private String url;
        private Map<String, String> customHeaders;
        /**
         * 连接超时时间
         */
        private Duration timeout;
        private boolean logRequests = false;
        private boolean logResponses = false;
        private Logger logger;

        /**
         * The URL of the MCP server.
         */
        public StreamableHttpMcpTransport.Builder url(String url) {
            this.url = url;
            return this;
        }

        /**
         * The request headers of the MCP server.
         */
        public StreamableHttpMcpTransport.Builder customHeaders(Map<String, String> customHeaders) {
            this.customHeaders = customHeaders;
            return this;
        }

        /**
         * The connection timeout (applied on the http client level). Application-level
         * timeouts are handled by the MCP client itself.
         */
        public StreamableHttpMcpTransport.Builder timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        /**
         * Whether to log all requests that are sent over this transport.
         */
        public StreamableHttpMcpTransport.Builder logRequests(boolean logRequests) {
            this.logRequests = logRequests;
            return this;
        }

        /**
         * Whether to log all responses received over this transport.
         */
        public StreamableHttpMcpTransport.Builder logResponses(boolean logResponses) {
            this.logResponses = logResponses;
            return this;
        }

        /**
         * Sets a custom {@link Logger} to be used for traffic logging (both requests and responses).
         * This logger will be used for both regular HTTP responses and Server-Sent Events (SSE) traffic.
         * If not specified, a default logger will be used.
         *
         * @param logger an alternate {@link Logger} to be used instead of the default one provided by Langchain4J for traffic logging.
         * @return {@code this}.
         */
        public StreamableHttpMcpTransport.Builder logger(Logger logger) {
            this.logger = logger;
            return this;
        }

        /**
         * An optional {@link Executor} that will be used for executing requests and handling responses.
         */
        public StreamableHttpMcpTransport.Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public StreamableHttpMcpTransport build() {
            return new StreamableHttpMcpTransport(this);
        }
    }
}
