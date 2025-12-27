package dev.langchain4j.guardrail.config;

import dev.langchain4j.spi.guardrail.config.OutputGuardrailsConfigBuilderFactory;
import java.util.ServiceLoader;

/**
 * 输出护栏配置
 * 专门用于输出防护措施的配置。
 * Configuration specifically for output guardrails.
 * <p>
 *     Frameworks that extend this library (like Quarkus or Spring) may provide their own implementations of this configuration.
 * </p>
 */
public interface OutputGuardrailsConfig extends GuardrailsConfig {
    /**
     * 护栏的默认最大重试次数
     * Default maximum number of retries for the guardrail.
     */
    int MAX_RETRIES_DEFAULT = 2;

    /**
     * 配置防护措施的最大重试次数。
     * Configures the maximum number of retries for the guardrail.
     * <p>
     *     Defaults to {@link #MAX_RETRIES_DEFAULT} if not set.
     * </p>
     * Set to {@code 0} to disable retries.
     */
    int maxRetries();

    /**
     * 获取用于构建 OutputGuardrailsConfig 实例的 newBuilder 实例。
     * Gets a newBuilder instance for building {@link OutputGuardrailsConfig} instances.
     * @return A {@link OutputGuardrailsConfigBuilder} for building {@link OutputGuardrailsConfig} instances.
     */
    static OutputGuardrailsConfigBuilder builder() {
        // 输出护栏配置构建者工厂
        return ServiceLoader.load(OutputGuardrailsConfigBuilderFactory.class)
                .findFirst()
                .map(OutputGuardrailsConfigBuilderFactory::get)
                .orElseGet(DefaultOutputGuardrailsConfig::builder);
    }

    /**
     * OutputGuardrailsConfig 实例的构建者。
     * Builder for {@link OutputGuardrailsConfig} instances.
     * <p>
     *     This is needed so other frameworks (like Quarkus and Spring) can extend the configuration mechanism with their own
     *     implementations while also adhering to the interfaces and specs defined here.
     * </p>
     */
    interface OutputGuardrailsConfigBuilder extends GuardrailsConfigBuilder<OutputGuardrailsConfig> {
        /**
         * 设置输出保护机制的最大重试次数。
         * Sets the maximum number of retries for output guardrails.
         * <p>
         *     Defaults to {@link OutputGuardrailsConfig#maxRetries()} if not set.
         * </p>
         * @param maxRetries The maximum number of retries for output guardrails
         * @return The maximum number of retries for output guardrails
         * @see OutputGuardrailsConfig#maxRetries()
         */
        OutputGuardrailsConfigBuilder maxRetries(int maxRetries);
    }
}
