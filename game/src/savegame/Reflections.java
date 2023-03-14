package savegame;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class Reflections {

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

    public static <T> T generateInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = (Constructor<T>) clazz.getConstructors()[0];
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not instantiate " + clazz.getName(), e);
        }
    }

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
