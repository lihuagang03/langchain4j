package dev.langchain4j.service.guardrail;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.Internal;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailExecutor;
import dev.langchain4j.guardrail.InputGuardrailRequest;
import dev.langchain4j.guardrail.InputGuardrailResult;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailExecutor;
import dev.langchain4j.guardrail.OutputGuardrailRequest;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 护栏服务的实现基类
 * 负责管理和应用输入和输出的防护措施到指定 AI 服务类的方法。
 * 防护措施通过类级或方法级的注解定义，用于对 AI 服务方法的输入和输出执行约束、验证或转换逻辑。
 * Responsible for managing and applying input and output guardrails to methods
 * of a specified AI service class. Guardrails are defined through annotations at either the
 * class or method level and are used to enforce constraints, validation, or transformation
 * logic on inputs and outputs of AI service methods.
 *
 * 此类负责处理关联 AI 服务类方法的输入和输出保护逻辑的初始化、配置和执行，确保在调用方法时自动应用输入和输出约束。
 * This class handles the initialization, configuration, and execution of input and output
 * guardrail logic for the methods of the associated AI service class, ensuring both input
 * and output constraints are applied automatically when methods are invoked.
 */
@Internal
public abstract class AbstractGuardrailService implements GuardrailService {
    /**
     * 空键
     */
    private static final Object NULL_KEY = new Object();

    /**
     * AI 服务类
     */
    private final Class<?> aiServiceClass;
    /**
     * 方法的键到输入护栏执行器的映射表
     */
    private final Map<Object, InputGuardrailExecutor> inputGuardrails = new ConcurrentHashMap<>();
    /**
     * 方法的键到输出护栏执行器的映射表
     */
    private final Map<Object, OutputGuardrailExecutor> outputGuardrails = new ConcurrentHashMap<>();

    // Caches for whether or not a method has input or output guardrails
    private final Map<Object, Boolean> inputGuardrailMethods = new ConcurrentHashMap<>();
    private final Map<Object, Boolean> outputGuardrailMethods = new ConcurrentHashMap<>();

    protected AbstractGuardrailService(
            Class<?> aiServiceClass,
            Map<Object, InputGuardrailExecutor> inputGuardrails,
            Map<Object, OutputGuardrailExecutor> outputGuardrails) {
        this.aiServiceClass = ensureNotNull(aiServiceClass, "aiServiceClass");
        Optional.ofNullable(inputGuardrails).ifPresent(this.inputGuardrails::putAll);
        Optional.ofNullable(outputGuardrails).ifPresent(this.outputGuardrails::putAll);
    }

    @Override
    public Class<?> aiServiceClass() {
        return this.aiServiceClass;
    }

    @Override
    public <MethodKey> InputGuardrailResult executeInputGuardrails(MethodKey method, InputGuardrailRequest request) {
        // 执行输入防护措施
        return Optional.ofNullable(method)
                .map(this.inputGuardrails::get)
                .map(executor -> executor.execute(request))
                .orElseGet(InputGuardrailResult::success);
    }

    @Override
    public <MethodKey> OutputGuardrailResult executeOutputGuardrails(MethodKey method, OutputGuardrailRequest request) {
        // 执行输出防护措施
        return Optional.ofNullable(method)
                .map(this.outputGuardrails::get)
                .map(executor -> executor.execute(request))
                .orElseGet(OutputGuardrailResult::success);
    }

    @Override
    public <MethodKey> boolean hasInputGuardrails(MethodKey method) {
        return this.inputGuardrailMethods.computeIfAbsent(
                checkMethodKey(method), m -> !getInputGuardrails(m).isEmpty());
    }

    @Override
    public <MethodKey> boolean hasOutputGuardrails(MethodKey method) {
        return this.outputGuardrailMethods.computeIfAbsent(
                checkMethodKey(method), m -> !getOutputGuardrails(m).isEmpty());
    }

    private static <MethodKey> MethodKey checkMethodKey(MethodKey method) {
        return (method != null) ? method : (MethodKey) NULL_KEY;
    }

    // These methods below really only exist for testing purposes
    // That's why they are package-scoped
    int getInputGuardrailMethodCount() {
        return this.inputGuardrails.size();
    }

    int getOutputGuardrailMethodCount() {
        return this.outputGuardrails.size();
    }

    <MethodKey> Optional<dev.langchain4j.guardrail.config.InputGuardrailsConfig> getInputConfig(MethodKey method) {
        // 输入护栏配置
        // 输入护栏执行器
        return Optional.ofNullable(this.inputGuardrails.get(method)).map(InputGuardrailExecutor::config);
    }

    <MethodKey> Optional<dev.langchain4j.guardrail.config.OutputGuardrailsConfig> getOutputConfig(MethodKey method) {
        // 输出护栏配置
        // 输出护栏执行器
        return Optional.ofNullable(this.outputGuardrails.get(method)).map(OutputGuardrailExecutor::config);
    }

    <MethodKey> List<InputGuardrail> getInputGuardrails(MethodKey method) {
        // 输入护栏执行器
        return Optional.ofNullable(method)
                .map(this.inputGuardrails::get)
                .map(InputGuardrailExecutor::guardrails)
                .orElseGet(List::of);
    }

    <MethodKey> List<OutputGuardrail> getOutputGuardrails(MethodKey method) {
        // 输出护栏执行器
        return Optional.ofNullable(method)
                .map(this.outputGuardrails::get)
                .map(OutputGuardrailExecutor::guardrails)
                .orElseGet(List::of);
    }
}
