package dev.langchain4j.spi.guardrail;

import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailExecutor;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import dev.langchain4j.guardrail.config.InputGuardrailsConfig;
import dev.langchain4j.observability.api.event.InputGuardrailExecutedEvent;

/**
 * 输入护栏执行器构建者工厂
 * 表示用于创建 InputGuardrailExecutor.InputGuardrailExecutorBuilder 实例的工厂。
 * 这个非密封接口继承自密封接口 GuardrailExecutorBuilderFactory，专门用于输入防护栅栏。
 * 它提供方法来配置和构建作用于输入的防护栅栏的执行环境，确保它们遵守预定义的规则或约束。
 * Represents a factory for creating instances of {@link InputGuardrailExecutor.InputGuardrailExecutorBuilder}.
 * This non-sealed interface extends from the sealed interface {@link GuardrailExecutorBuilderFactory} and is specifically tailored
 * for input guardrails. It provides methods to configure and build execution environments for guardrails that operate on inputs,
 * ensuring that they adhere to predefined rules or constraints.
 */
public non-sealed interface InputGuardrailExecutorBuilderFactory
        extends GuardrailExecutorBuilderFactory<
                InputGuardrailsConfig,
                InputGuardrailResult,
                InputGuardrailRequest,
                InputGuardrail,
                InputGuardrailExecutedEvent,
                InputGuardrailExecutor.InputGuardrailExecutorBuilder> {}
