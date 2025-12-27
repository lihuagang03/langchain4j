package dev.langchain4j.guardrail.config;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

/**
 * 输入护栏配置的默认实现
 * 如果没有其他库提供自己的实现，这是该库的 InputGuardrailsConfig 的默认实现。
 * The default implementation of {@link InputGuardrailsConfig} for this library if no other libraries provide their own implementations.
 */
final class DefaultInputGuardrailsConfig implements InputGuardrailsConfig {
    DefaultInputGuardrailsConfig(Builder builder) {
        ensureNotNull(builder, "builder");
    }

    /**
     * 获取用于构建 DefaultInputGuardrailsConfig 实例的构建者实例。
     * Gets a builder instance for building {@link DefaultInputGuardrailsConfig} instances.
     * @return The builder instance for building {@link DefaultInputGuardrailsConfig} instances.
     */
    static Builder builder() {
        return new Builder();
    }

    /**
     * 用于 DefaultInputGuardrailsConfig 实例的构建者。
     * Builder for {@link DefaultInputGuardrailsConfig} instances.
     */
    static class Builder implements InputGuardrailsConfigBuilder {
        @Override
        public InputGuardrailsConfig build() {
            return new DefaultInputGuardrailsConfig(this);
        }
    }
}
