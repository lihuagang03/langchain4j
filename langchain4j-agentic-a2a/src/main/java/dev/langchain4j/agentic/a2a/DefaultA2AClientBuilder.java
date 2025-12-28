package dev.langchain4j.agentic.a2a;

import static dev.langchain4j.agentic.internal.AgentUtil.uniqueAgentName;

import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.agentic.agent.AgentRequest;
import dev.langchain4j.agentic.agent.AgentResponse;
import dev.langchain4j.agentic.internal.A2AClientBuilder;
import dev.langchain4j.agentic.internal.AgentSpecification;
import dev.langchain4j.exception.LangChain4jException;
import io.a2a.A2A;
import io.a2a.client.Client;
import io.a2a.client.ClientEvent;
import io.a2a.client.MessageEvent;
import io.a2a.client.TaskEvent;
import io.a2a.client.TaskUpdateEvent;
import io.a2a.client.config.ClientConfig;
import io.a2a.client.transport.jsonrpc.JSONRPCTransport;
import io.a2a.client.transport.jsonrpc.JSONRPCTransportConfig;
import io.a2a.spec.A2AClientError;
import io.a2a.spec.A2AClientException;
import io.a2a.spec.AgentCard;
import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.TextPart;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 智能体到智能体的客户端构建者的默认实现
 */
public class DefaultA2AClientBuilder<T> implements A2AClientBuilder<T> {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultA2AClientBuilder.class);

    /**
     * 智能体服务实现类
     */
    private final Class<T> agentServiceClass;

    /**
     * 智能体卡片
     */
    private final AgentCard agentCard;
    /**
     * 智能体到智能体的客户端
     */
    private final Client a2aClient;

    /**
     * 名称
     */
    private String name;
    /**
     * 唯一的名称
     */
    private String uniqueName;
    /**
     * 输入变量的键列表
     */
    private String[] inputKeys;
    /**
     * 输出变量的键
     */
    private String outputKey;
    private boolean async;

    /**
     * 在调用之前的监听器
     */
    private Consumer<AgentRequest> beforeListener = request -> {};
    /**
     * 在调用之后的监听器
     */
    private Consumer<AgentResponse> afterListener = response -> {};

    DefaultA2AClientBuilder(String a2aServerUrl, Class<T> agentServiceClass) {
        this.agentCard = agentCard(a2aServerUrl);
        this.name = agentCard.name();
        this.uniqueName = uniqueAgentName(this.name);
        try {
            // 智能体到智能体的客户端
            this.a2aClient = Client.builder(agentCard)
                    .clientConfig(new ClientConfig.Builder()
                            .setStreaming(false) // Disabling streaming
                            .build())
                    .withTransport(JSONRPCTransport.class, new JSONRPCTransportConfig())
                    .build();
        } catch (A2AClientException e) {
            throw new LangChain4jException(e);
        }
        this.agentServiceClass = agentServiceClass;
    }

    private static AgentCard agentCard(String a2aServerUrl) {
        try {
            return A2A.getAgentCard(a2aServerUrl);
        } catch (A2AClientError e) {
            throw new LangChain4jException(e);
        }
    }

    @Override
    public T build() {
        if (agentServiceClass == UntypedAgent.class && inputKeys == null) {
            throw new IllegalArgumentException("Input names must be provided for UntypedAgent.");
        }

        // 智能体对象，新的代理实例
        Object agent = Proxy.newProxyInstance(
                agentServiceClass.getClassLoader(),
                new Class<?>[] {agentServiceClass, A2AClientSpecification.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
                        if (method.getDeclaringClass() == AgentSpecification.class) {
                            return switch (method.getName()) {
                                case "name" -> name;
                                case "uniqueName" -> uniqueName;
                                case "description" -> agentCard.description();
                                case "outputKey" -> outputKey;
                                case "async" -> async;
                                case "beforeInvocation" -> {
                                    beforeListener.accept((AgentRequest) args[0]);
                                    yield null;
                                }
                                case "afterInvocation" -> {
                                    afterListener.accept((AgentResponse) args[0]);
                                    yield null;
                                }
                                default ->
                                    throw new UnsupportedOperationException(
                                            "Unknown method on AgentInstance class : " + method.getName());
                            };
                        }

                        if (method.getDeclaringClass() == A2AClientSpecification.class) {
                            return switch (method.getName()) {
                                case "agentCard" -> agentCard;
                                case "inputKeys" -> inputKeys;
                                default ->
                                    throw new UnsupportedOperationException(
                                            "Unknown method on A2AClientInstance class : " + method.getName());
                            };
                        }

                        return invokeAgent(args);
                    }
                });

        return (T) agent;
    }

    private Object invokeAgent(Object[] args) throws A2AClientException {
        List<Part<?>> parts = new ArrayList<>();

        if (agentServiceClass == UntypedAgent.class) {
            Map<String, Object> params = (Map<String, Object>) args[0];
            for (String inputKey : inputKeys) {
                parts.add(new TextPart(params.get(inputKey).toString()));
            }
        } else {
            for (Object arg : args) {
                parts.add(new TextPart(arg.toString()));
            }
        }

        // 用户消息
        Message message =
                new Message.Builder().role(Message.Role.USER).parts(parts).build();

        final CompletableFuture<String> messageResponse = new CompletableFuture<>();
        List<BiConsumer<ClientEvent, AgentCard>> consumers = List.of((event, card) -> {
            if (event instanceof MessageEvent messageEvent) {
                messageResponse.complete(messageEvent.getMessage().getParts().stream()
                        .filter(TextPart.class::isInstance)
                        .map(TextPart.class::cast)
                        .map(TextPart::getText)
                        .collect(Collectors.joining("\n")));
            } else if (event instanceof TaskEvent taskEvent) {
                messageResponse.complete(taskEvent.getTask().getArtifacts().stream()
                        .flatMap(a -> a.parts().stream())
                        .filter(TextPart.class::isInstance)
                        .map(TextPart.class::cast)
                        .map(TextPart::getText)
                        .collect(Collectors.joining("\n")));
            } else if (event instanceof TaskUpdateEvent updateEvent) {
                if (updateEvent.getTask().getArtifacts() != null) {
                    messageResponse.complete(updateEvent.getTask().getArtifacts().stream()
                            .flatMap(a -> a.parts().stream())
                            .filter(TextPart.class::isInstance)
                            .map(TextPart.class::cast)
                            .map(TextPart::getText)
                            .collect(Collectors.joining("\n")));
                }
            } else {
                messageResponse.completeExceptionally(
                        new IllegalArgumentException("The event expected should be of type " + event.getClass()));
            }
        });
        // Create error handler for streaming errors
        Consumer<Throwable> streamingErrorHandler = (error) -> {
            LOG.error("Streaming error occurred: " + error.getMessage(), error);
            messageResponse.completeExceptionally(error);
        };
        a2aClient.sendMessage(message, consumers, streamingErrorHandler);
        try {
            String responseText = messageResponse.get();
            LOG.debug("Response: " + responseText);
            return responseText;
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Failed to get response: " + e.getMessage(), e);
            throw new RuntimeException("Failed to get response: " + e.getMessage(), e);
        }
    }

    @Override
    public DefaultA2AClientBuilder<T> inputKeys(String... inputKeys) {
        this.inputKeys = inputKeys;
        return this;
    }

    @Override
    public DefaultA2AClientBuilder<T> outputKey(String outputKey) {
        this.outputKey = outputKey;
        return this;
    }

    @Override
    public DefaultA2AClientBuilder<T> async(boolean async) {
        this.async = async;
        return this;
    }

    @Override
    public DefaultA2AClientBuilder<T> beforeAgentInvocation(Consumer<AgentRequest> beforeListener) {
        this.beforeListener = this.beforeListener.andThen(beforeListener);
        return this;
    }

    @Override
    public DefaultA2AClientBuilder<T> afterAgentInvocation(Consumer<AgentResponse> afterListener) {
        this.afterListener = this.afterListener.andThen(afterListener);
        return this;
    }
}
