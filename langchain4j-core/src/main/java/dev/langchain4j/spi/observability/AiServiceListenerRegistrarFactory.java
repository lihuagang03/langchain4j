package dev.langchain4j.spi.observability;

import dev.langchain4j.observability.api.AiServiceListenerRegistrar;
import java.util.function.Supplier;

/**
 * AI服务监听注册器工厂
 * A factory for creating {@link AiServiceListenerRegistrar} instances.
 */
public interface AiServiceListenerRegistrarFactory extends Supplier<AiServiceListenerRegistrar> {}
