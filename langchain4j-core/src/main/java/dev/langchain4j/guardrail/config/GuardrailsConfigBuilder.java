package dev.langchain4j.guardrail.config;

/**
 * 护栏配置构建者
 * Builder for {@link GuardrailsConfig} instances.
 * @param <C> The type of configuration being build
 */
public interface GuardrailsConfigBuilder<C extends GuardrailsConfig> {
    /**
     * Builds the configuration.
     * @return The configuration
     */
    C build();
}
