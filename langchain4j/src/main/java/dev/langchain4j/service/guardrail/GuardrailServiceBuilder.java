package dev.langchain4j.service.guardrail;

import static dev.langchain4j.internal.ValidationUtils.ensureNotNull;

import dev.langchain4j.classinstance.ClassInstanceLoader;
import dev.langchain4j.classloading.ClassMetadataProvider;
import dev.langchain4j.guardrail.Guardrail;
import dev.langchain4j.guardrail.GuardrailRequest;
import dev.langchain4j.guardrail.GuardrailResult;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailExecutor;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailExecutor;
import dev.langchain4j.service.guardrail.GuardrailService.Builder;
import dev.langchain4j.spi.classloading.ClassMetadataProviderFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 护栏服务构建者
 * 一个用于创建和配置 GuardrailService 实例的构建器类，该实例为 AI 服务类提供输入和输出的护栏机制。
 * 此类允许通过配置对象、护栏类或护栏的直接实例来定制护栏。
 * A builder class for creating and configuring a {@link GuardrailService} instance, which provides input and output guardrail
 * mechanisms for an AI service class. This class allows customization of the guardrails through configuration objects,
 * guardrail classes, or direct instances of guardrails.
 * <p>
 * 这个构建器支持通过注解和代码中的显式配置来设置安全防护。
 * 方法级别的注解优先于类级别的注解，并且注解的优先级高于在构建器中以编程方式配置的安全防护。
 * This builder supports setting guardrails via both annotations and explicit configurations in code. Annotations at the
 * method level take precedence over annotations at the class level, and annotations take precedence over guardrails
 * configured programmatically in the builder.
 */
final class GuardrailServiceBuilder implements Builder {
    /**
     * 默认的输入护栏执行器的供应商
     */
    private final Supplier<InputGuardrailExecutor> defaultInputGuardrailSupplier =
            () -> InputGuardrailExecutor.builder()
                    .config(this.inputGuardrailsConfig)
                    .guardrails(
                            getNonAnnotationBasedClassLevelGuardrails(this.inputGuardrails, this.inputGuardrailClasses))
                    .build();

    /**
     * 默认的输出护栏执行器的供应商
     */
    private final Supplier<OutputGuardrailExecutor> defaultOutputGuardrailSupplier =
            () -> OutputGuardrailExecutor.builder()
                    .config(this.outputGuardrailsConfig)
                    .guardrails(getNonAnnotationBasedClassLevelGuardrails(
                            this.outputGuardrails, this.outputGuardrailClasses))
                    .build();

    /**
     * AI 服务类
     */
    private final Class<?> aiServiceClass;
    /**
     * 输入护栏配置
     */
    private dev.langchain4j.guardrail.config.InputGuardrailsConfig inputGuardrailsConfig;
    /**
     * 输出护栏配置
     */
    private dev.langchain4j.guardrail.config.OutputGuardrailsConfig outputGuardrailsConfig;
    /**
     * 输入护栏类列表
     */
    private List<Class<? extends InputGuardrail>> inputGuardrailClasses = new ArrayList<>();
    /**
     * 输出护栏类列表
     */
    private List<Class<? extends OutputGuardrail>> outputGuardrailClasses = new ArrayList<>();
    /**
     * 输入护栏列表
     */
    private List<InputGuardrail> inputGuardrails = new ArrayList<>();
    /**
     * 输出护栏列表
     */
    private List<OutputGuardrail> outputGuardrails = new ArrayList<>();

    GuardrailServiceBuilder(Class<?> aiServiceClass) {
        this.aiServiceClass = ensureNotNull(aiServiceClass, "aiServiceClass");
    }

    /**
     * 配置输入防护措施
     * Configures the input guardrails for the Builder.
     *
     * @param config The configuration for input guardrails. Must not be null.
     * @return The current instance of {@link Builder} for method chaining.
     * @throws IllegalArgumentException if {@code config} is null.
     */
    @Override
    public Builder inputGuardrailsConfig(dev.langchain4j.guardrail.config.InputGuardrailsConfig config) {
        this.inputGuardrailsConfig = ensureNotNull(config, "config");
        return this;
    }

    /**
     * 配置输出防护措施
     * Configures the output guardrails for the Builder.
     *
     * @param config The configuration for output guardrails. Must not be null.
     * @return The current instance of {@link Builder} for method chaining.
     * @throws IllegalArgumentException if {@code config} is null.
     */
    @Override
    public Builder outputGuardrailsConfig(dev.langchain4j.guardrail.config.OutputGuardrailsConfig config) {
        this.outputGuardrailsConfig = ensureNotNull(config, "config");
        return this;
    }

    /**
     * 配置输入防护栏类。
     * Configures the classes of input guardrails for the Builder. Existing input guardrail classes will be cleared.
     *
     * @param guardrailClasses A list of classes implementing the {@link InputGuardrail} interface to be used
     *                         as input guardrails. May be {@code null}.
     * @param <I> The type of {@link InputGuardrail}
     * @return The current instance of {@link Builder} for method chaining.
     */
    @Override
    public <I extends InputGuardrail> Builder inputGuardrailClasses(List<Class<? extends I>> guardrailClasses) {
        this.inputGuardrailClasses.clear();

        if (guardrailClasses != null) {
            this.inputGuardrailClasses.addAll(guardrailClasses);
        }

        return this;
    }

    /**
     * 配置输出防护栏类。
     * Configures the classes of output guardrails for the Builder.
     * Existing output guardrail classes will be cleared.
     *
     * @param guardrailClasses A list of classes implementing the {@link OutputGuardrail} interface to be used
     *                         as output guardrails. May be {@code null}.
     * @param <O> The type of {@link OutputGuardrail}
     * @return The current instance of {@link Builder} for method chaining.
     */
    @Override
    public <O extends OutputGuardrail> Builder outputGuardrailClasses(List<Class<? extends O>> guardrailClasses) {
        this.outputGuardrailClasses.clear();

        if (guardrailClasses != null) {
            this.outputGuardrailClasses.addAll(guardrailClasses);
        }

        return this;
    }

    /**
     * 设置输入护栏。
     * Sets the input guardrails for the Builder. Existing input guardrails
     * will be cleared, and the provided input guardrails will be added.
     *
     * @param guardrails A list of input guardrails implementing the {@link InputGuardrail} interface.
     *                   Can be {@code null}, in which case no guardrails will be added.
     * @return The current instance of {@link Builder} for method chaining.
     */
    @Override
    public <I extends InputGuardrail> Builder inputGuardrails(List<I> guardrails) {
        this.inputGuardrails.clear();

        if (guardrails != null) {
            this.inputGuardrails.addAll(guardrails);
        }

        return this;
    }

    /**
     * 设置输出护栏。
     * Sets the output guardrails for the Builder. Existing output guardrails
     * will be cleared, and the provided output guardrails will be added.
     *
     * @param guardrails A list of output guardrails implementing the {@link OutputGuardrail}
     *                   interface. Can be {@code null}, in which case no guardrails will be added.
     * @return The current instance of {@link Builder} for method chaining.
     */
    @Override
    public <O extends OutputGuardrail> Builder outputGuardrails(List<O> guardrails) {
        this.outputGuardrails.clear();

        if (guardrails != null) {
            this.outputGuardrails.addAll(guardrails);
        }

        return this;
    }

    /**
     * 构建并返回 GuardrailService 实例。
     * 此方法使用构建器中定义的设置配置输入和输出的护栏。
     * 如果未设置，则使用提供的类级或方法级注解。如果没有方法级注解，则使用类级注解。
     * Builds and returns an instance of {@link GuardrailService}.
     * This method configures input and output guardrails using the settings defined in the builder.
     * If not set it then uses the provided class-level or method-level annotations. If no
     * method-level annotations are present, it defers to class-level annotations.
     *
     * @return an instance of {@link GuardrailService} configured with appropriate
     * input and output guardrails.
     */
    @Override
    public DefaultGuardrailService build() {
        // Anything set here in this builder is relevant at the AiService level, NOT at the method level
        // Setting guardrails at the method level can only be done via the annotations

        // Next, compute method-level guardrails based on the annotations
        // Go method-by-method, if there are annotations on the method, then use them
        // Otherwise use the annotations on the class
        // If there aren't any annotations on the class, then use the ones set on this builder
        var inputGuardrailsByMethod = new HashMap<Object, InputGuardrailExecutor>();
        var outputGuardrailsByMethod = new HashMap<Object, OutputGuardrailExecutor>();
        var factory = ClassMetadataProvider.getClassMetadataProviderFactory();

        factory.getNonStaticMethodsOnClass(this.aiServiceClass).forEach(method -> {
            var inputGuardrailsForMethod = computeInputGuardrailsForAiServiceMethod(method, factory);
            var outputGuardrailsForMethod = computeOutputGuardrailsForAiServiceMethod(method, factory);

            if (!inputGuardrailsForMethod.guardrails().isEmpty()) {
                inputGuardrailsByMethod.put(method, inputGuardrailsForMethod);
            }

            if (!outputGuardrailsForMethod.guardrails().isEmpty()) {
                outputGuardrailsByMethod.put(method, outputGuardrailsForMethod);
            }
        });

        return new DefaultGuardrailService(this.aiServiceClass, inputGuardrailsByMethod, outputGuardrailsByMethod);
    }

    private static <P extends GuardrailRequest, R extends GuardrailResult<R>, G extends Guardrail<P, R>>
            List<G> getNonAnnotationBasedClassLevelGuardrails(
                    List<G> guardrails, List<Class<? extends G>> guardrailClasses) {
        ensureNotNull(guardrails, "guardrails");
        ensureNotNull(guardrailClasses, "guardrailClasses");

        var guardrailsSetByBuilderAtClassLevel = guardrails.stream();
        var guardrailsSetByBuilderAtClassLevelByClassName =
                guardrailClasses.stream().map(GuardrailServiceBuilder::getGuardrailClassInstance);

        return Stream.concat(guardrailsSetByBuilderAtClassLevel, guardrailsSetByBuilderAtClassLevelByClassName)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static <P extends GuardrailRequest, R extends GuardrailResult<R>, G extends Guardrail<P, R>>
            G getGuardrailClassInstance(Class<G> guardrailClass) {
        ensureNotNull(guardrailClass, "guardrailClass");
        return ClassInstanceLoader.getClassInstance(guardrailClass);
    }

    private static <I extends InputGuardrail> List<I> getGuardrails(InputGuardrails inputGuardrails) {
        return Stream.of(inputGuardrails.value())
                .map(guardrailClass -> (I) getGuardrailClassInstance(guardrailClass))
                .toList();
    }

    private static <O extends OutputGuardrail> List<O> getGuardrails(OutputGuardrails outputGuardrails) {
        return Stream.of(outputGuardrails.value())
                .map(guardrailClass -> (O) getGuardrailClassInstance(guardrailClass))
                .toList();
    }

    private static dev.langchain4j.guardrail.config.InputGuardrailsConfig computeConfig(InputGuardrails annotation) {
        return dev.langchain4j.guardrail.config.InputGuardrailsConfig.builder().build();
    }

    private static dev.langchain4j.guardrail.config.OutputGuardrailsConfig computeConfig(OutputGuardrails annotation) {

        return dev.langchain4j.guardrail.config.OutputGuardrailsConfig.builder()
                .maxRetries(annotation.maxRetries())
                .build();
    }

    private InputGuardrailExecutor computeInputGuardrails(InputGuardrails annotation) {
        return InputGuardrailExecutor.builder()
                .config(hasInputGuardrailConfigSetOnBuilder() ? this.inputGuardrailsConfig : computeConfig(annotation))
                .guardrails(
                        hasInputGuardrailsSetOnBuilder()
                                ? getNonAnnotationBasedClassLevelGuardrails(
                                        this.inputGuardrails, this.inputGuardrailClasses)
                                : getGuardrails(annotation))
                .build();
    }

    private OutputGuardrailExecutor computeOutputGuardrails(OutputGuardrails annotation) {
        return OutputGuardrailExecutor.builder()
                .config(
                        hasOutputGuardrailConfigSetOnBuilder()
                                ? this.outputGuardrailsConfig
                                : computeConfig(annotation))
                .guardrails(
                        hasOutputGuardrailsSetOnBuilder()
                                ? getNonAnnotationBasedClassLevelGuardrails(
                                        this.outputGuardrails, this.outputGuardrailClasses)
                                : getGuardrails(annotation))
                .build();
    }

    private <MethodKey> InputGuardrailExecutor computeInputGuardrailsForAiServiceMethod(
            MethodKey method, ClassMetadataProviderFactory<MethodKey> factory) {
        // For both input & output guardrails, first check the builder
        // If nothing on the builder, then check the method
        // If nothing on the method, then fall back to the class
        if (inputGuardrailsAndConfigSetOnBuilder()) {
            // Don't need to introspect the annotation at all since everything is set on the builder
            return this.defaultInputGuardrailSupplier.get();
        }

        // If we get here we know we need to introspect the annotation for one reason or another
        return factory.getAnnotation(method, InputGuardrails.class)
                .map(this::computeInputGuardrails)
                // Didn't exist at the method level, so check the class level
                .orElseGet(() -> factory.getAnnotation(this.aiServiceClass, InputGuardrails.class)
                        .map(this::computeInputGuardrails)
                        .orElseGet(this.defaultInputGuardrailSupplier::get));
    }

    private <MethodKey> OutputGuardrailExecutor computeOutputGuardrailsForAiServiceMethod(
            MethodKey method, ClassMetadataProviderFactory<MethodKey> factory) {
        // For both input & output guardrails, first check the builder
        // If nothing on the builder, then check the method
        // If nothing on the method, then fall back to the class
        if (outputGuardrailsAndConfigSetOnBuilder()) {
            return this.defaultOutputGuardrailSupplier.get();
        }

        // If we get here we know we need to introspect the annotation for one reason or another
        return factory.getAnnotation(method, OutputGuardrails.class)
                .map(this::computeOutputGuardrails)
                // Didn't exist at the method level, so check the class level
                .orElseGet(() -> factory.getAnnotation(this.aiServiceClass, OutputGuardrails.class)
                        .map(this::computeOutputGuardrails)
                        .orElseGet(this.defaultOutputGuardrailSupplier::get));
    }

    private boolean hasInputGuardrailsSetOnBuilder() {
        return !this.inputGuardrails.isEmpty() || !this.inputGuardrailClasses.isEmpty();
    }

    private boolean hasInputGuardrailConfigSetOnBuilder() {
        return this.inputGuardrailsConfig != null;
    }

    private boolean hasOutputGuardrailsSetOnBuilder() {
        return !this.outputGuardrails.isEmpty() || !this.outputGuardrailClasses.isEmpty();
    }

    private boolean hasOutputGuardrailConfigSetOnBuilder() {
        return this.outputGuardrailsConfig != null;
    }

    private boolean inputGuardrailsAndConfigSetOnBuilder() {
        return hasInputGuardrailsSetOnBuilder() && hasInputGuardrailConfigSetOnBuilder();
    }

    private boolean outputGuardrailsAndConfigSetOnBuilder() {
        return hasOutputGuardrailsSetOnBuilder() && hasOutputGuardrailConfigSetOnBuilder();
    }
}
