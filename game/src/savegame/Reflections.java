package savegame;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.stream.Stream;
import sun.reflect.ReflectionFactory;

public class Reflections {

    /**
     * Set a field of an object to a new value.
     *
     * @param object Object where the field is located
     * @param fieldName the name of the field
     * @param value the new value
     */
    public static void setFieldValue(Object object, String fieldName, Object value) {
        try {
            Field field;
            try {
                field = object.getClass().getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                field = object.getClass().getSuperclass().getDeclaredField(fieldName);
            }
            field.setAccessible(true);
            if (!Modifier.isStatic(field.getModifiers())) {
                field.set(object, value);
            } else {
                field.set(null, value);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Could not set field " + fieldName + " of " + object.getClass().getName(), e);
        }
    }

    /**
     * Get all fields of a class.
     *
     * @param object the object
     * @param field the field
     * @param value the new value
     */
    public static void setFieldValue(Object object, Field field, Object value) {
        try {
            field.setAccessible(true);
            if (!Modifier.isStatic(field.getModifiers())) {
                field.set(object, value);
            } else {
                field.set(null, value);
            }
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Could not set field " + field.getName() + " of " + object.getClass().getName(),
                    e);
        }
    }

    /**
     * Create a new instance of a class using the default constructor.
     *
     * @param clazz the class to instantiate
     * @return the new instance
     * @param <T> the type of the class
     */
    public static <T> T createInstance(Class<T> clazz, Object... constructorArgs) {
        try {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            Constructor<?> constructor = null;
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == constructorArgs.length) {
                    constructor = c;
                    break;
                }
            }
            if (constructor == null) {
                return createInstance2(clazz);
            }
            constructor.setAccessible(true);
            return (T) constructor.newInstance(constructorArgs);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not instantiate " + clazz.getName(), e);
        }
    }

    /**
     * Get the value of a field of an object.
     *
     * @param object the object
     * @param fieldName the name of the field
     * @return the value of the field
     * @param <T> the type of the field
     */
    public static <T> T getFieldValue(Object object, String fieldName) {
        try {
            Field field;
            try {
                field = object.getClass().getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                field = object.getClass().getSuperclass().getDeclaredField(fieldName);
            }
            return getFieldValue(object, field);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Could not get field " + fieldName + " of " + object.getClass().getName(), e);
        }
    }

    /**
     * Get the value of a field of an object.
     *
     * @param object the object
     * @param field the field
     * @return the value of the field
     * @param <T> the type of the field
     */
    public static <T> T getFieldValue(Object object, Field field) {
        try {
            field.setAccessible(true);
            return (T) field.get(object);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Could not get field " + field.getName() + " of " + object.getClass().getName(),
                    e);
        }
    }

    /**
     * Call a method of an object that does not return a value.
     *
     * @param object the object
     * @param method the name of the method
     */
    public static void callVoidMethod(Object object, String method) {
        try {
            Method func = object.getClass().getDeclaredMethod(method);
            func.setAccessible(true);
            func.invoke(object);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Could not call method " + method + " of " + object.getClass().getName(), e);
        }
    }

    /**
     * Create Instance of given Class using ReflectionFactory.
     *
     * @param clazz the class to instantiate
     * @return the new instance
     * @param <T> the type of the class
     */
    public static <T> T createInstance2(Class<T> clazz) {
        try {
            ReflectionFactory rf = ReflectionFactory.getReflectionFactory();
            Constructor<?> objDef = Object.class.getDeclaredConstructor();
            Constructor<?> seriConstr = rf.newConstructorForSerialization(clazz, objDef);
            return (T) seriConstr.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not instantiate " + clazz.getName(), e);
        }
    }

    /**
     * Get all fields of a class including the fields of the super class.
     *
     * @param clazz the class
     * @param filterFinals if true, final fields are excluded
     * @param filterStatics if true, static fields are excluded
     * @return all fields of the class
     */
    public static Field[] getFieldsOfClass(
            Class<?> clazz, boolean filterFinals, boolean filterStatics) {
        Field[] fields = clazz.getDeclaredFields();
        Field[] superFields = clazz.getSuperclass().getDeclaredFields();
        Field[] allFields = new Field[fields.length + superFields.length];
        System.arraycopy(fields, 0, allFields, 0, fields.length);
        System.arraycopy(superFields, 0, allFields, fields.length, superFields.length);
        return Stream.of(allFields)
                .filter(
                        field -> {
                            int modifiers = field.getModifiers();
                            return !((Modifier.isFinal(modifiers) && filterFinals)
                                    || (Modifier.isStatic(modifiers) && filterStatics));
                        })
                .toArray(Field[]::new);
    }
}
