package dev.langchain4j.classloading;

import dev.langchain4j.spi.classloading.ClassMetadataProviderFactory;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;

/**
 * 类元数据提供者
 * 用于返回有关类及其方法的元数据的工具类。
 * 旨在允许下游框架（如 Quarkus 或 Spring）使用它们自己的机制来提供此信息。
 * Utility class for returning metadata about a class and its methods. Intended to allow downstream frameworks (like Quarkus
 * or Spring) to use their own mechanisms for providing this information.
 */
public final class ClassMetadataProvider {
    /**
     * 默认的基于反射的类元数据提供者工厂
     */
    private static final ReflectionBasedClassMetadataProviderFactory DEFAULT_CLASS_METADATA_PROVIDER_FACTORY =
            new ReflectionBasedClassMetadataProviderFactory();

    private ClassMetadataProvider() {}

    /**
     * 获取 ClassMetadataProviderFactory 的实现。
     * 此方法首先通过 ServiceLoader 查找工厂的实现。
     * 它会过滤掉默认的工厂实现（ReflectionBasedClassMetadataProviderFactory），以允许外部框架提供自定义实现。
     * 如果没有可用的自定义实现，该方法将返回默认工厂。
     * Retrieves an implementation of a {@link ClassMetadataProviderFactory}. This method first looks for
     * implementations of the factory via the {@link ServiceLoader}. It filters out the default factory implementation
     * ({@link ReflectionBasedClassMetadataProviderFactory}) to allow for custom implementations provided by external frameworks.
     * If no custom implementations are available, the method returns the default factory.
     *
     * @param <MethodKey> The type of the method key, representing a unique identifier for methods.
     * @return An instance of {@link ClassMetadataProviderFactory} either provided by an external framework or falling back
     *         to the default implementation.
     */
    public static <MethodKey> ClassMetadataProviderFactory<MethodKey> getClassMetadataProviderFactory() {
        // 加载 类元数据提供者工厂
        return ServiceLoader.load(ClassMetadataProviderFactory.class).stream()
                .filter(provider ->
                        !DEFAULT_CLASS_METADATA_PROVIDER_FACTORY.getClass().equals(provider.type()))
                .map(Provider::get)
                .findFirst()
                .orElse(DEFAULT_CLASS_METADATA_PROVIDER_FACTORY);
    }
}
