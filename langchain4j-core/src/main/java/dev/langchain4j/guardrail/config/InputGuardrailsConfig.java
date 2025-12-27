package dev.langchain4j.guardrail.config;

import dev.langchain4j.spi.guardrail.config.InputGuardrailsConfigBuilderFactory;
import java.util.ServiceLoader;

/**
 * 输入护栏配置
 * 专门用于输入防护措施的配置。
 * Configuration specifically for input guardrails.
 * <p>
 *     Frameworks that extend this library (like Quarkus or Spring) may provide their own implementations of this configuration.
 * </p>
 */
public interface InputGuardrailsConfig extends GuardrailsConfig {
    /**
     * 获取一个用于构建 InputGuardrailsConfig 实例的构建者实例。
     * Gets a builder instance for building {@link InputGuardrailsConfig} instances.
     * @return A {@link InputGuardrailsConfigBuilder} for building {@link InputGuardrailsConfig} instances.
     */
    static InputGuardrailsConfigBuilder builder() {
        // 输入护栏配置构建者工厂
        return ServiceLoader.load(InputGuardrailsConfigBuilderFactory.class)
                .findFirst()
                .map(InputGuardrailsConfigBuilderFactory::get)
                .orElseGet(DefaultInputGuardrailsConfig::builder);
    }

    /**
     * InputGuardrailsConfig 实例的构建者。
     * Builder for {@link InputGuardrailsConfig} instances.
     * <p>
     *     This is needed so other frameworks (like Quarkus and Spring) can extend the configuration mechanism with their own
     *     implementations while also adhering to the interfaces and specs defined here.
     * </p>
     */
    interface InputGuardrailsConfigBuilder extends GuardrailsConfigBuilder<InputGuardrailsConfig> {}
}
