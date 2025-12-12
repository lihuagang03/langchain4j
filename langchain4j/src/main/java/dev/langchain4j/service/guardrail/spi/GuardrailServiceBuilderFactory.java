package dev.langchain4j.service.guardrail.spi;

import dev.langchain4j.service.guardrail.GuardrailService;

/**
 * 护栏服务构建者工厂
 * A factory for providing instances of {@link GuardrailService.Builder}
 */
public interface GuardrailServiceBuilderFactory {
    /**
     * Gets an instance of the {@link GuardrailService.Builder}
     * @param aiServiceClass The class of the AI service
     * @return The {@link GuardrailService.Builder} instance
     */
    GuardrailService.Builder getBuilder(Class<?> aiServiceClass);
}
