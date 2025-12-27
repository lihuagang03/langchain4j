package dev.langchain4j.guardrail;

import static dev.langchain4j.guardrail.OutputGuardrailResult.successWith;
import static dev.langchain4j.observability.api.event.OutputGuardrailExecutedEvent.OutputGuardrailExecutedEventBuilder;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.OutputGuardrailResult.Failure;
import dev.langchain4j.guardrail.config.OutputGuardrailsConfig;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.observability.api.event.OutputGuardrailExecutedEvent;
import dev.langchain4j.spi.guardrail.OutputGuardrailExecutorBuilderFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * 输出护栏执行器
 * 用于输出护栏的 GuardrailExecutor。
 * The {@link GuardrailExecutor} for {@link OutputGuardrail}s.
 * <p>
 *     在执行输出防护时，如果任何输出防护触发了重新提示或重试，
 *     新响应必须重新经过整个输出防护链，以确保新响应通过所有输出防护。
 *     When executing output guardrails, if any {@link OutputGuardrail} triggers a reprompt or retry,
 *     the new response has to go back through the entire chain of output guardrails to ensure the new response
 *     passes all the output guardrails.
 * </p>
 */
public non-sealed class OutputGuardrailExecutor
        extends AbstractGuardrailExecutor<
                OutputGuardrailsConfig,
                OutputGuardrailRequest,
                OutputGuardrailResult,
                OutputGuardrail,
                OutputGuardrailExecutedEvent,
                Failure> {

    /**
     * 输出验证失败。护栏已达到最大重试次数。
     * 护栏消息：
     *
     * %s
     */
    public static final String MAX_RETRIES_MESSAGE_TEMPLATE =
            """
            Output validation failed. The guardrails have reached the maximum number of retries.
            Guardrail messages:

            %s
            """;

    protected OutputGuardrailExecutor(OutputGuardrailsConfig config, List<OutputGuardrail> guardrails) {
        super(config, guardrails);
    }

    /**
     * 在给定的 OutputGuardrailRequest 上执行 OutputGuardrails。
     * Executes the {@link OutputGuardrail}s on the given {@link OutputGuardrailRequest}.
     *
     * @param request     The {@link OutputGuardrailRequest} to validate
     * @return The {@link OutputGuardrailResult} of the validation
     */
    @Override
    public OutputGuardrailResult execute(OutputGuardrailRequest request) {
        OutputGuardrailResult result = null;
        var accumulatedRequest = request;
        var attempt = 0;
        // 最大尝试次数
        var maxAttempts = config().maxRetries();

        if (maxAttempts == 0) {
            maxAttempts = 1;
        } else if (maxAttempts < 0) {
            maxAttempts = OutputGuardrailsConfig.MAX_RETRIES_DEFAULT;
        }

        while (attempt < maxAttempts) {
            // 重写结果
            result = rewriteResult(request, accumulatedRequest, executeGuardrails(accumulatedRequest));

            if (result.isSuccess()) {
                return result;
            }

            // Not successful
            if (!result.isRetry()) {
                // Not any kind of retry, so just stop here
                throw new OutputGuardrailException(result.toString(), result.getFirstFailureException());
            }

            if (++attempt < maxAttempts) {
                // 如果我们到这里，我们就知道这是某种重试
                // 我们不想在内存中添加中间的用户消息
                // If we get here we know it is some kind of retry
                // We don't want to add intermediary UserMessages to the memory
                var chatMessages = Optional.ofNullable(
                                accumulatedRequest.requestParams().chatMemory())
                        .map(ChatMemory::messages)
                        .orElseGet(ArrayList::new);
                result.getReprompt().map(UserMessage::from).ifPresent(chatMessages::add);

                // 重新执行带有附加消息的请求
                // 但不要将其或生成的消息添加到记忆中
                // Re-execute the request with the appended message
                // But don't add it or the resulting message to the memory
                var response = accumulatedRequest.chatExecutor().execute(chatMessages);
                accumulatedRequest = OutputGuardrailRequest.builder()
                        .responseFromLLM(response)
                        .chatExecutor(accumulatedRequest.chatExecutor())
                        .requestParams(accumulatedRequest.requestParams())
                        .build();
            }
        }

        if (attempt == maxAttempts) {
            var failureMessages = result.failures().stream()
                    .map(GuardrailResult.Failure::message)
                    .collect(Collectors.joining(System.lineSeparator()));

            throw new OutputGuardrailException(MAX_RETRIES_MESSAGE_TEMPLATE.formatted(failureMessages));
        }

        return result;
    }

    private OutputGuardrailResult rewriteResult(OutputGuardrailRequest originalRequest, OutputGuardrailRequest validatedRequest, OutputGuardrailResult result) {
        if (result.isSuccess() && !result.hasRewrittenResult()) {
            String originalText = originalRequest.responseFromLLM().aiMessage().text();
            String validatedText = validatedRequest.responseFromLLM().aiMessage().text();
            if (!originalText.equals(validatedText)) {
                // 由于成功的重新提示，输出护栏验证的文本与原始文本不同，
                // 因此我们需要使用新文本创建一个新的成功结果
                // The text validated by the output guardrail is different form the original one because of a
                // successful reprompt, so we need to create a new success result with the new text
                return successWith(originalRequest.responseFromLLM()
                        .aiMessage()
                        .withText(validatedText));
            }
        }
        return result;
    }

    /**
     * Creates a failure result from some {@link Failure}s.
     * @param failures The failures
     * @return A {@link OutputGuardrailResult} containing the failures
     */
    @Override
    protected OutputGuardrailResult createFailure(List<Failure> failures) {
        return OutputGuardrailResult.failure(failures);
    }

    /**
     * Creates a success result.
     * @return A {@link OutputGuardrailResult} representing success
     */
    @Override
    protected OutputGuardrailResult createSuccess() {
        return OutputGuardrailResult.success();
    }

    @Override
    protected OutputGuardrailException createGuardrailException(String message, Throwable cause) {
        return new OutputGuardrailException(message, cause);
    }

    @Override
    protected OutputGuardrailResult handleFatalResult(
            OutputGuardrailResult accumulatedResult, OutputGuardrailResult result) {
        return accumulatedResult.hasRewrittenResult() ? result.blockRetry() : result;
    }

    @Override
    protected OutputGuardrailExecutedEventBuilder createEmptyObservabilityEventBuilderInstance() {
        return OutputGuardrailExecutedEvent.builder();
    }

    /**
     * 创建一个新的 OutputGuardrailExecutor.OutputGuardrailExecutorBuilder 实例。
     * 该构建者用于构造和配置 OutputGuardrailExecutor.OutputGuardrailExecutorBuilder 的实例。
     * Creates a new instance of {@link OutputGuardrailExecutorBuilder}.
     * The builder is used to construct and configure instances of {@link OutputGuardrailExecutorBuilder}.
     * @return A new {@link OutputGuardrailExecutorBuilder} instance.
     */
    public static OutputGuardrailExecutorBuilder builder() {
        return ServiceLoader.load(OutputGuardrailExecutorBuilderFactory.class)
                .findFirst()
                .map(OutputGuardrailExecutorBuilderFactory::getBuilder)
                .orElseGet(OutputGuardrailExecutorBuilder::new);
    }

    /**
     * 用于构建 OutputGuardrailExecutor 实例的构建者类。
     * Builder class for constructing instances of {@link OutputGuardrailExecutor}.
     *
     * 该构建者允许通过指定关联的配置类型（OutputGuardrailsConfig）和要执行的输出安全栏来配置 OutputGuardrailExecutor。
     * This builder allows configuration of an {@link OutputGuardrailExecutor} by specifying the associated configuration
     * type ({@link OutputGuardrailsConfig}) and the output guardrails to be executed.
     *
     * 扩展 AbstractGuardrailExecutor.GuardrailExecutorBuilder，针对以下特定类型：
     * - 配置类型：OutputGuardrailsConfig
     * - 结果类型：OutputGuardrailResult
     * - 参数类型：OutputGuardrailRequest
     * - 安全栅栏类型：OutputGuardrail
     * Extends {@link GuardrailExecutorBuilder} for the specific types:
     * - Configuration type: {@link OutputGuardrailsConfig}
     * - Result type: {@link OutputGuardrailResult}
     * - Parameter type: {@link OutputGuardrailRequest}
     * - Guardrail type: {@link OutputGuardrail}
     *
     * 提供 build() 方法以创建 OutputGuardrailExecutor 实例。
     * Provides the {@code build()} method to create an {@link OutputGuardrailExecutor} instance.
     */
    public static non-sealed class OutputGuardrailExecutorBuilder
            extends GuardrailExecutorBuilder<
                    OutputGuardrailsConfig,
                    OutputGuardrailResult,
                    OutputGuardrailRequest,
                    OutputGuardrail,
                    OutputGuardrailExecutedEvent,
                    OutputGuardrailExecutorBuilder> {

        protected OutputGuardrailExecutorBuilder() {
            super(OutputGuardrailsConfig.builder().build());
        }

        @Override
        public OutputGuardrailExecutor build() {
            return new OutputGuardrailExecutor(config(), guardrails());
        }
    }
}
