package savegame;

import com.badlogic.gdx.utils.JsonValue;
import ecs.entities.Entity;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

public interface IFieldSerializing extends ISerializable {

    HashSet<Class<?>> ignoredTypes =
            new HashSet<>(List.of(new Class<?>[] {Logger.class, Entity.class}));

    @Override
    default JsonValue serialize() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        Field[] fields = Reflections.getFieldsOfClass(this.getClass(), false, true);

        fieldLoop:
        for (Field field : fields) {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();

            // Check if ignored
            for (Class<?> ignoredType : ignoredTypes) {
                if (ignoredType.isAssignableFrom(fieldType)) {
                    continue fieldLoop;
                }
            }
            Object obj = Reflections.getFieldValue(this, field);
            // Lamdba fields must not be serialized
            if (obj.getClass().getName().contains("$$Lambda$")) {
                continue;
            }

            json.addChild(field.getName(), GameSerialization.serialize(obj));
        }
        return json;
    }

    @Override
    default void deserialize(JsonValue data) {

        Field[] fields = Reflections.getFieldsOfClass(this.getClass(), false, true);
        fieldLoop:
        for (Field field : fields) {
            if (!data.has(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            Class<?> fieldType = field.getType();

            // Check if ignored
            for (Class<?> ignoredType : ignoredTypes) {
                if (ignoredType.isAssignableFrom(fieldType)) {
                    continue fieldLoop;
                }
            }

            Reflections.setFieldValue(
                    this, field, GameSerialization.deserialize(data.get(field.getName())));
        }
    }
}
