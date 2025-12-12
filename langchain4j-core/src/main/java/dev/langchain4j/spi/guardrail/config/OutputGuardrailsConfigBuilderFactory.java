package dev.langchain4j.spi.guardrail.config;

import dev.langchain4j.guardrail.config.OutputGuardrailsConfig;
import java.util.function.Supplier;

/**
 * 输出护栏配置构建者工厂
 * SPI for overriding and/or extending the default {@link OutputGuardrailsConfig.OutputGuardrailsConfigBuilder} implementation.
 */
public interface OutputGuardrailsConfigBuilderFactory
        extends Supplier<OutputGuardrailsConfig.OutputGuardrailsConfigBuilder> {}
