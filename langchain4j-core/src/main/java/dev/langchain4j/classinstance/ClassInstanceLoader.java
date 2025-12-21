package dev.langchain4j.classinstance;

import dev.langchain4j.spi.classloading.ClassInstanceFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.ServiceLoader;

/**
 * 类实例加载器
 * 用于创建和获取指定类类型实例的工具类。
 * 该类提供了一种机制，可以将实例创建委托给工厂（如果可用），或者回退到使用无参构造函数直接实例化。
 * Utility class for creating and retrieving instances of specified class types.
 * This class provides a mechanism to delegate instance creation to a factory, if available,
 * or fallback to direct instantiation using the no-argument constructor.
 * <p>
 * 这在需要利用依赖注入框架或其他托管对象工厂进行对象创建的场景中非常有用。
 * This is useful in scenarios where dependency injection frameworks or other managed
 * object factories might need to be leveraged for object creation.
 * </p>
 */
public final class ClassInstanceLoader {
    private ClassInstanceLoader() {}

    /**
     * Retrieves an instance of the specified class type. This method first attempts to obtain the instance
     * through a {@link ClassInstanceFactory}, if available. If no factory is present, it creates a new
     * instance of the class using its no-argument constructor.
     *
     * @param <T> the type of the class
     * @param clazz the class object representing the type whose instance is to be created
     * @return an instance of the specified class type
     */
    public static <T> T getClassInstance(Class<T> clazz) {
        return ServiceLoader.load(ClassInstanceFactory.class)
                .findFirst()
                .map(classInstanceFactory -> classInstanceFactory.getInstanceOfClass(clazz))
                .orElseGet(() -> createNewClassInstance(clazz));
    }

    private static <T> T createNewClassInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
