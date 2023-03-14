package savegame;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class Reflections {

    /**
     * Set a final field of an object to a new value.
     *
     * @param object Object where the field is located
     * @param fieldName the name of the field
     * @param value the new value
     */
    public static void setFinalField(Object object, String fieldName, Object value) {
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
    public static <T> T createInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = (Constructor<T>) clazz.getConstructors()[0];
            constructor.setAccessible(true);
            return constructor.newInstance();
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
}
