package dev.langchain4j.spi.guardrail.config;

import dev.langchain4j.guardrail.config.InputGuardrailsConfig;
import java.util.function.Supplier;

/**
 * 输入护栏配置构建者工厂
 * 用于覆盖和/或扩展默认 InputGuardrailsConfig.InputGuardrailsConfigBuilder 实现的 SPI。
 * SPI for overriding and/or extending the default {@link InputGuardrailsConfig.InputGuardrailsConfigBuilder} implementation.
 */
public interface InputGuardrailsConfigBuilderFactory
        extends Supplier<InputGuardrailsConfig.InputGuardrailsConfigBuilder> {}
