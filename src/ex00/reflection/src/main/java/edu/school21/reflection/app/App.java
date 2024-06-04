package edu.school21.reflection.app;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App 
{
    public static final String packageName = "edu.school21.reflection.classes.";
    public static Scanner sc;
    public static void main( String[] args )
    {
        printClasses();
        System.out.println("Введите имя класса:");
        sc = new Scanner(System.in);
        String className = sc.nextLine();

        printFieldAndMethods(className);
        Object o = createObject(className);
        changeField(o);

        callMethod(o);

    }

    private static void callMethod(Object o) {
        System.out.println("Enter name of the method for call:");
        String[] methodSignature = sc.nextLine()
                .replace("(", ",")
                .replace(")", ",")
                .replace(" ", "")
                .split(",");
        ArrayList<Object> parameters = new ArrayList<>();
        for (int i = 1; i < methodSignature.length; ++i) {
            String parameterLine = sc.nextLine();
            Object parameter = null;
            if (methodSignature[i].equals("Integer") || methodSignature[i].equals("int")) {
                parameters.add(Integer.valueOf(parameterLine));
            } else if (methodSignature[i].equals("String")) {
                parameters.add(parameterLine);
            } else if (methodSignature[i].equals("Boolean") || methodSignature[i].equals("boolean")) {
                parameters.add(Boolean.valueOf(parameterLine));
            } else if (methodSignature[i].equals("Double") || methodSignature[i].equals("double")) {
                parameters.add(Double.valueOf(parameterLine));
            } else if (methodSignature[i].equals("Long") || methodSignature[i].equals("long")) {
                parameters.add(Long.valueOf(parameterLine));
            }
        }
        ClassInfoHandler.callMethod(o, methodSignature[0], parameters.toArray());
        System.out.println(o.toString());

    }

    private static void changeField(Object o) {
        System.out.println("Enter name of the field for changing:");
        String name = sc.nextLine();
        System.out.println("Enter value:");
        String value = sc.nextLine();
        try {
            Field field = o.getClass().getDeclaredField(name);
            ClassInfoHandler.setField(field, o, value);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        System.out.println("object updated: " + o.toString());
        printDelimiter();
    }

    private static Object createObject(String className) {
        System.out.println("Lest`s create an object.");
        Field[] fields = ClassInfoHandler.getClassesFields(packageName + className);
        Object object = ClassInfoHandler.createObject(packageName + className);
        for (Field field : fields) {
            System.out.println(field.getName() + ":");
            String value = sc.nextLine();
            ClassInfoHandler.setField(field, object, value);
        }
        System.out.println("object created: " + object.toString());
        printDelimiter();
        return object;
    }

    private static void printFieldAndMethods(String className) {
        printFields(className);
        printMethods(className);
        printDelimiter();
    }

    private static void printMethods(String className) {
        Method[] methods = ClassInfoHandler.getClassesMethods(packageName + className);
        System.out.println("methods :");
        for (Method method : methods) {
            System.out.println("\t" + method.getReturnType().getSimpleName()
                    + " " + method.getName()
                    + "(" + Arrays.asList(method.getParameterTypes()).toString()
                    .replace("[", "")
                    .replace("]", "")
                    + ")");
        }
    }

    private static void printFields(String className) {
        Field[] fields = ClassInfoHandler.getClassesFields(packageName + className);
        System.out.println("fields :");
        for (Field field : fields) {
            System.out.println("\t" + field.getType().getSimpleName() + " " + field.getName() );
        }
    }

    private static void printClasses() {
        System.out.println("User");
        System.out.println("Car");
        printDelimiter();
    }

    private static void printDelimiter() {
        System.out.println("------------------------");
    }


}
