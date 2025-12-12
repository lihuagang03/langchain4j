package dev.langchain4j.service.guardrail;

import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailExecutor;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailExecutor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 护栏服务的默认实现
 * 负责管理并将输入输出护栏应用于指定 AI 服务类的方法。
 * 护栏通过在类或方法级别的注解定义，用于强制执行处理请求和响应的约束或规则。
 * Responsible for managing and applying input and output guardrails
 * to methods of a specified AI service class. Guardrails are defined through annotations
 * at either the class or method level and are used to enforce constraints or rules for
 * processing requests and responses.
 *
 * 该类为指定的 AI 服务类的所有方法初始化保护措施，允许通过指定的保护措施实现对输入和输出的验证、转换或限制。
 * 保护措施可以通过每种保护措施类型的特定配置进行自定义。
 * This class initializes guardrails for all methods of the specified AI service class,
 * allowing input and output validation, transformation, or restriction through the
 * specified guardrail implementations. The guardrails can be customized through configurations
 * specific to each guardrail type.
 * <p>
 *     Obtain instances via {@link GuardrailService#builder(Class)}
 * </p>
 */
final class DefaultGuardrailService extends AbstractGuardrailService {
    DefaultGuardrailService(
            Class<?> aiServiceClass,
            Map<Object, InputGuardrailExecutor> inputGuardrails,
            Map<Object, OutputGuardrailExecutor> outputGuardrails) {
        super(aiServiceClass, inputGuardrails, outputGuardrails);
    }

    // These methods below really only exist for testing purposes
    // Thats why they are package-scoped
    Optional<dev.langchain4j.guardrail.config.InputGuardrailsConfig> getInputConfig(String methodName) {
        // 输入护栏配置
        return findMethod(methodName).flatMap(super::getInputConfig);
    }

    Optional<dev.langchain4j.guardrail.config.OutputGuardrailsConfig> getOutputConfig(String methodName) {
        // 输出护栏配置
        return findMethod(methodName).flatMap(super::getOutputConfig);
    }

    List<InputGuardrail> getInputGuardrails(String methodName) {
        // 输入护栏列表
        return findMethod(methodName).map(super::getInputGuardrails).orElseGet(List::of);
    }

    List<OutputGuardrail> getOutputGuardrails(String methodName) {
        // 输出护栏列表
        return findMethod(methodName).map(super::getOutputGuardrails).orElseGet(List::of);
    }

    private Optional<Method> findMethod(String methodName) {
        return Stream.of(aiServiceClass().getMethods())
                .filter(method -> methodName.equals(method.getName()))
                .findFirst();
    }
}
