package dev.langchain4j.spi.guardrail.config;

import dev.langchain4j.guardrail.config.OutputGuardrailsConfig;
import java.util.function.Supplier;

/**
 * 输出护栏配置构建者工厂
 * 用于覆盖和/或扩展默认 OutputGuardrailsConfig.OutputGuardrailsConfigBuilder 实现的 SPI。
 * SPI for overriding and/or extending the default {@link OutputGuardrailsConfig.OutputGuardrailsConfigBuilder} implementation.
 */
public interface OutputGuardrailsConfigBuilderFactory
        extends Supplier<OutputGuardrailsConfig.OutputGuardrailsConfigBuilder> {}
