package edu.school21.reflection.app;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ClassInfoHandler {

    public static Field[] getClassesFields(String className) {
        Field[] fields;
        try {
            fields = Class.forName(className).getDeclaredFields();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return fields;
    }

    public static Method[] getClassesMethods(String className) {
        Method[] methods;
        try {
            methods = Class.forName(className).getDeclaredMethods();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return methods;
    }

    public static Object createObject(String className) {
        Class<?> myClass;
        Constructor<?> constructor;
        Object o;
        try {
            myClass = Class.forName(className);
            constructor = myClass.getConstructor();
            o = constructor.newInstance();
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return o;
    }

    public static void setField(Field field, Object object, String value) {
        field.setAccessible(true);
        try {
            if (field.getType().equals(String.class)) {
                field.set(object, value);
            } else if (field.getType().equals(boolean.class)) {
                field.set(object, Boolean.valueOf(value));
            } else if (field.getType().equals(int.class)) {
                field.set(object, Integer.valueOf(value));
            } else if (field.getType().equals(double.class)) {
                field.set(object, Double.valueOf(value));
            } else if (field.getType().equals(long.class)) {
                field.set(object, Long.valueOf(value));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void callMethod(Object o, String methodName, Object... objects) {
        Class<?>[] classes = new Class[objects.length];
        for (int i = 0; i < objects.length; ++i) {
            if (objects[i].getClass().getSimpleName().equals("Integer")
                    || objects[i].getClass().getSimpleName().equals("int")) {
                classes[i] = Integer.TYPE;
            } else if (objects[i].getClass().getSimpleName().equals("String")) {
                classes[i] = String.class;
            } else if (objects[i].getClass().getSimpleName().equals("Boolean")
                    || objects[i].getClass().getSimpleName().equals("boolean")) {
                classes[i] = Boolean.TYPE;
            } else if (objects[i].getClass().getSimpleName().equals("Double")
                    || objects[i].getClass().getSimpleName().equals("double")) {
                classes[i] = Double.TYPE;
            } else if (objects[i].getClass().getSimpleName().equals("Long")
                    || objects[i].getClass().getSimpleName().equals("long")) {
                classes[i] = Long.TYPE;
            }
        }
        try {
            Method method = o.getClass().getMethod(methodName, classes);
            method.invoke(o, objects);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

//    public Object getClassesMethods(String className, Field[] fields) {
//        return Class.forName(className).getConstructor().newInstance();
//    }
}
