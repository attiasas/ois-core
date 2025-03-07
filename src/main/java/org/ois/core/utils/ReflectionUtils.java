package org.ois.core.utils;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import org.ois.core.utils.io.data.DataNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A utility class for reflection operations.
 * This class provides methods to dynamically create instances of classes
 * using their fully qualified class names.
 */
public class ReflectionUtils {

    /**
     * Creates a new instance of the specified class.
     *
     * @param className the fully qualified name of the class to instantiate
     * @param <T>       the type of the instance to be created
     * @return a new instance of the specified class
     * @throws ReflectionException if the class cannot be found or if any reflection-related error occurs
     * @throws NoSuchMethodException if the default constructor is not found
     * @throws InvocationTargetException if the underlying constructor throws an exception
     * @throws InstantiationException if the class that declares the underlying constructor represents an abstract class
     * @throws IllegalAccessException if the constructor is not accessible
     */
    public static <T> T newInstance(String className) throws ReflectionException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> instanceClass = ClassReflection.forName(className);
        Constructor<?> instanceConstructor = instanceClass.getDeclaredConstructor();
        return (T) instanceConstructor.newInstance();
    }

    public static <T> T newInstance(DataNode node) throws ReflectionException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (node == null || !node.contains("class")) {
            throw new RuntimeException("can't create reflected object without 'class' attribute");
        }
        return newInstance(node.get("class").getString());
    }
}
