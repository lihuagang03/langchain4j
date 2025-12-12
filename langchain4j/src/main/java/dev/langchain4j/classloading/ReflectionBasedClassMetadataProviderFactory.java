package dev.langchain4j.classloading;

import dev.langchain4j.spi.classloading.ClassMetadataProviderFactory;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 基于反射的类元数据提供者工厂
 * 使用 Java 反射实现 ClassMetadataProviderFactory 接口。
 * 该类提供通过基于反射的机制从类中获取注解和方法元数据的方法。
 * Implementation of the {@link ClassMetadataProviderFactory} interface using Java Reflection.
 * This class provides methods to retrieve annotations and method metadata from classes
 * via reflection-based mechanisms.
 */
public final class ReflectionBasedClassMetadataProviderFactory implements ClassMetadataProviderFactory<Method> {
    @Override
    public <T extends Annotation> Optional<T> getAnnotation(Method method, Class<T> annotationClass) {
        return Optional.ofNullable(method.getAnnotation(annotationClass));
    }

    @Override
    public <T extends Annotation> Optional<T> getAnnotation(Class<?> clazz, Class<T> annotationClass) {
        return Optional.ofNullable(clazz.getAnnotation(annotationClass));
    }

    @Override
    public Iterable<Method> getNonStaticMethodsOnClass(Class<?> clazz) {
        return Stream.of(clazz.getMethods())
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .toList();
    }
}
