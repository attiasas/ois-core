package org.ois.core.utils;

import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ReflectionUtils {

    public static <T> T newInstance(String className) throws ReflectionException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        System.err.println("try create reflect " + className);
        Class<?> instanceClass = ClassReflection.forName(className);
        Constructor<?> instanceConstructor = instanceClass.getDeclaredConstructor();
        System.err.println("gor the constructr");
//        instanceConstructor.setAccessible(true);
        return (T) instanceConstructor.newInstance();
    }
}
