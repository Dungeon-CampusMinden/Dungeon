package savegame;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Could not set field " + fieldName + " of " + object.getClass().getName(), e);
        }
    }

    /**
     * Create a new instance of a class using the default constructor.
     *
     * @param clazz the class to instantiate
     * @return the new instance
     * @param <T> the type of the class
     */
    public static <T> T createInstance(Class<T> clazz, Object ... constructorArgs) {
        try {
            Constructor<?>[] constructors = clazz.getConstructors();
            Constructor<?> constructor = null;
            for (Constructor<?> c : constructors) {
                if (c.getParameterCount() == constructorArgs.length) {
                    constructor = c;
                    break;
                }
            }
            if (constructor == null) {
                constructor = constructors[0];
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
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (T) field.get(object);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(
                    "Could not get field " + fieldName + " of " + object.getClass().getName(), e);
        }
    }

    /**
     * Call a method of an object that does not return a value.
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

}
