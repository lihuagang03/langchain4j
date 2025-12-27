package dev.langchain4j.spi.guardrail;

import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailExecutor;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import dev.langchain4j.guardrail.config.OutputGuardrailsConfig;
import dev.langchain4j.observability.api.event.OutputGuardrailExecutedEvent;

/**
 * 输出护栏执行器构建者工厂
 * 表示用于创建 OutputGuardrailExecutor.OutputGuardrailExecutorBuilder 实例的工厂。
 * 该接口扩展了 GuardrailExecutorBuilderFactory，专门用于输出防护栏，使用特定于输出上下文的配置、结果、请求和防护栏。
 * Represents a factory for creating instances of {@link OutputGuardrailExecutor.OutputGuardrailExecutorBuilder}.
 * This interface extends {@link GuardrailExecutorBuilderFactory} and is specifically tailored for output guardrails,
 * utilizing configurations, results, requests, and guardrails that are specific to the output context.
 */
public non-sealed interface OutputGuardrailExecutorBuilderFactory
        extends GuardrailExecutorBuilderFactory<
                OutputGuardrailsConfig,
                OutputGuardrailResult,
                OutputGuardrailRequest,
                OutputGuardrail,
                OutputGuardrailExecutedEvent,
                OutputGuardrailExecutor.OutputGuardrailExecutorBuilder> {}
