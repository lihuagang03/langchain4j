package dev.langchain4j.guardrail;

import dev.langchain4j.guardrail.config.GuardrailsConfig;
import dev.langchain4j.observability.api.event.GuardrailExecutedEvent;
import java.util.List;

/**
 * 护栏执行器
 * 表示一种在给定参数上执行一组防护措施的机制。
 * 此接口定义了使用多个防护措施验证交互（输入或输出）的契约。
 * Represents a mechanism to execute a set of guardrails on given parameters.
 * This interface defines the contract for validating interactions (input or output)
 * using multiple guardrails.
 *
 * @param <C>
 *            The type of {@link GuardrailsConfig} to use for configuration
 * @param <P>
 *            The type of {@link GuardrailRequest} to validate
 * @param <R>
 *            The type of {@link GuardrailResult} to return
 * @param <G>
 *            The type of {@link Guardrail}s being executed
 * @param <E> The type of {@link GuardrailExecutedEvent} to be fired
 */
public sealed interface GuardrailExecutor<
                C extends GuardrailsConfig,
                P extends GuardrailRequest<P>,
                R extends GuardrailResult<R>,
                G extends Guardrail<P, R>,
                E extends GuardrailExecutedEvent<P, R, G>>
        permits AbstractGuardrailExecutor {

    /**
     * 用于配置护栏执行的 GuardrailsConfig
     * The {@link GuardrailsConfig} to use for configuration of the guardrail execution
     * @return The {@link GuardrailsConfig} to use for configuration of the guardrail execution
     */
    C config();

    /**
     * 检索与该实施相关的安全措施。
     * Retrieves the guardrails associated with the implementation.
     * @return The guardrails which can be used for validating inputs or outputs against predefined rules.
     */
    List<G> guardrails();

    /**
     * 在给定参数上执行提供的防护措施。
     * Executes the provided guardrails on the given parameters.
     * @param request The {@link GuardrailRequest} to validate
     * @return The {@link GuardrailResult} of the validation
     */
    R execute(P request);
}
