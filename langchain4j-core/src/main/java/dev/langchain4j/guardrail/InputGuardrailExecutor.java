package dev.langchain4j.guardrail;

import static dev.langchain4j.observability.api.event.InputGuardrailExecutedEvent.InputGuardrailExecutedEventBuilder;

import dev.langchain4j.guardrail.InputGuardrailResult.Failure;
import dev.langchain4j.guardrail.config.InputGuardrailsConfig;
import dev.langchain4j.observability.api.event.InputGuardrailExecutedEvent;
import dev.langchain4j.spi.guardrail.InputGuardrailExecutorBuilderFactory;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 输入护栏执行器
 * 用于输入护栏的 GuardrailExecutor。
 * The {@link GuardrailExecutor} for {@link InputGuardrail}s.
 */
public non-sealed class InputGuardrailExecutor
        extends AbstractGuardrailExecutor<
                InputGuardrailsConfig,
                InputGuardrailRequest,
                InputGuardrailResult,
                InputGuardrail,
                InputGuardrailExecutedEvent,
                Failure> {

    protected InputGuardrailExecutor(InputGuardrailsConfig config, List<InputGuardrail> guardrails) {
        super(config, guardrails);
    }

    /**
     * Creates a failure result from some {@link Failure}s.
     * @param failures The failures
     * @return A {@link InputGuardrailResult} containing the failures
     */
    @Override
    protected InputGuardrailResult createFailure(List<Failure> failures) {
        return new InputGuardrailResult(failures, false);
    }

    /**
     * Creates a success result.
     * @return A {@link InputGuardrailResult} representing success
     */
    @Override
    protected InputGuardrailResult createSuccess() {
        return InputGuardrailResult.success();
    }

    @Override
    protected InputGuardrailException createGuardrailException(String message, Throwable cause) {
        return new InputGuardrailException(message, cause);
    }

    @Override
    protected InputGuardrailExecutedEventBuilder createEmptyObservabilityEventBuilderInstance() {
        return InputGuardrailExecutedEvent.builder();
    }

    /**
     * 在给定的 InputGuardrailRequest 上执行 InputGuardrails。
     * Executes the {@link InputGuardrail}s on the given {@link InputGuardrailRequest}.
     *
     * @param request     The {@link InputGuardrailRequest} to validate
     * @return The {@link InputGuardrailResult} of the validation
     */
    @Override
    public InputGuardrailResult execute(InputGuardrailRequest request) {
        // 执行防护措施
        var result = executeGuardrails(request);

        if (!result.isSuccess()) {
            throw new InputGuardrailException(result.toString(), result.getFirstFailureException());
        }

        return result;
    }

    /**
     * 创建并返回一个用于 InputGuardrailExecutor 的新构建者。
     * Creates and returns a new builder for {@link InputGuardrailExecutor}.
     *
     * 该构建者允许构建和配置 InputGuardrailExecutor 实例，并可自定义参数，如配置和输入安全限制。
     * This builder allows for constructing and configuring an {@link InputGuardrailExecutor}
     * instance, enabling customization of parameters such as the configuration and input guardrails.
     *
     * @return An {@link InputGuardrailExecutorBuilder} used to create {@link InputGuardrailExecutor} instances
     */
    public static InputGuardrailExecutorBuilder builder() {
        return ServiceLoader.load(InputGuardrailExecutorBuilderFactory.class)
                .findFirst()
                .map(InputGuardrailExecutorBuilderFactory::getBuilder)
                .orElseGet(InputGuardrailExecutorBuilder::new);
    }

    /**
     * 用于构建 InputGuardrailExecutor 实例的构建者类。
     * Builder class for constructing instances of {@link InputGuardrailExecutor}.
     *
     * 该构建者允许通过指定相关的配置类型（InputGuardrailsConfig）和要执行的输入安全防护措施来配置 InputGuardrailExecutor。
     * This builder allows configuration of an {@link InputGuardrailExecutor} by specifying the associated configuration
     * type ({@link InputGuardrailsConfig}) and the input guardrails to be executed.
     *
     * 继承自 AbstractGuardrailExecutor.GuardrailExecutorBuilder，用于以下特定类型：
     * - 配置类型：InputGuardrailsConfig
     * - 结果类型：InputGuardrailResult
     * - 参数类型：InputGuardrailRequest
     * - 安全防护类型：InputGuardrail
     * 提供 build() 方法以创建 InputGuardrailExecutor 实例。
     * Extends {@link GuardrailExecutorBuilder} for the specific types:
     * - Configuration type: {@link InputGuardrailsConfig}
     * - Result type: {@link InputGuardrailResult}
     * - Parameter type: {@link InputGuardrailRequest}
     * - Guardrail type: {@link InputGuardrail}
     *
     * Provides the {@code build()} method to create an {@link InputGuardrailExecutor} instance.
     */
    public static non-sealed class InputGuardrailExecutorBuilder
            extends GuardrailExecutorBuilder<
                    InputGuardrailsConfig,
                    InputGuardrailResult,
                    InputGuardrailRequest,
                    InputGuardrail,
                    InputGuardrailExecutedEvent,
                    InputGuardrailExecutorBuilder> {

        public InputGuardrailExecutorBuilder() {
            super(InputGuardrailsConfig.builder().build());
        }

        @Override
        public InputGuardrailExecutor build() {
            return new InputGuardrailExecutor(config(), guardrails());
        }
    }
}
