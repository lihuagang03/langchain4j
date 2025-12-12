package dev.langchain4j.spi.services;

import dev.langchain4j.Internal;
import dev.langchain4j.service.AiServiceContext;

/**
 * AI服务的上下文的工厂
 */
@Internal
public interface AiServiceContextFactory {

    AiServiceContext create(Class<?> aiServiceClass);
}
