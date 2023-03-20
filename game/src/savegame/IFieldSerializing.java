package savegame;

import com.badlogic.gdx.utils.JsonValue;
import java.io.Serializable;
import java.lang.reflect.Field;

public interface IFieldSerializing extends ISerializable {

    @Override
    default JsonValue serialize() {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        Field[] fields = Reflections.getFieldsOfClass(this.getClass(), false, true);
        for (Field field : fields) {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            try {
                if (ISerializable.class.isAssignableFrom(fieldType)
                        || Serializable.class.isAssignableFrom(
                                fieldType)) { // Class is serializable by ISerializable
                    json.addChild(field.getName(), GameSerialization.serialize(field));
                } else {
                    if (fieldType.isPrimitive()) {
                        json.addChild(field.getName(), getJsonValueOfPrimitiveField(field, this));
                    } else if (fieldType == String.class) {
                        json.addChild(
                                field.getName(),
                                new JsonValue((String) Reflections.getFieldValue(this, field)));
                    } else {
                        throw new UnserializableFieldException(
                                "Field "
                                        + field.getName()
                                        + " of class "
                                        + this.getClass().getName()
                                        + " is not serializable! (Type: "
                                        + fieldType.getName()
                                        + ")");
                    }
                }
            } catch (
                    UnserializableFieldException
                            ex) { // Catch own Exception to print it but not crash program
                ex.printStackTrace();
            }
        }
        return json;
    }

    @Override
    default void deserialize(JsonValue data) {

        Field[] fields = Reflections.getFieldsOfClass(this.getClass(), false, true);
        for (Field field : fields) {
            if (!data.has(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            try {
                if (ISerializable.class.isAssignableFrom(fieldType)
                        || Serializable.class.isAssignableFrom(
                                fieldType)) { // Class is serializable by ISerializable
                    GameSerialization.deserialize(data.get(field.getName()));
                } else {
                    if (fieldType.isPrimitive()) {
                        Reflections.setFieldValue(this, field, getPrimitiveFieldValue(field, data));
                    } else if (fieldType == String.class) {
                        Reflections.setFieldValue(this, field, data.getString(field.getName()));
                    } else {
                        throw new UnserializableFieldException(
                                "Field "
                                        + field.getName()
                                        + " of class "
                                        + this.getClass().getName()
                                        + " is not serializable! (Type: "
                                        + fieldType.getName()
                                        + ")");
                    }
                }
            } catch (
                    UnserializableFieldException
                            ex) { // Catch own Exception to print it but not crash program
                ex.printStackTrace();
            }
        }
    }

    private static JsonValue getJsonValueOfPrimitiveField(Field field, Object obj) {
        try {
            if (field.getType() == int.class) {
                return new JsonValue(field.getInt(obj));
            } else if (field.getType() == float.class) {
                return new JsonValue(field.getFloat(obj));
            } else if (field.getType() == double.class) {
                return new JsonValue(field.getDouble(obj));
            } else if (field.getType() == boolean.class) {
                return new JsonValue(field.getBoolean(obj));
            } else if (field.getType() == long.class) {
                return new JsonValue(field.getLong(obj));
            } else if (field.getType() == short.class) {
                return new JsonValue(field.getShort(obj));
            } else if (field.getType() == byte.class) {
                return new JsonValue(field.getByte(obj));
            } else if (field.getType() == char.class) {
                return new JsonValue(field.getChar(obj));
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Object getPrimitiveFieldValue(Field field, JsonValue data) {
        if (field.getType() == int.class) {
            return data.getInt(field.getName());
        } else if (field.getType() == float.class) {
            return data.getFloat(field.getName());
        } else if (field.getType() == double.class) {
            return data.getDouble(field.getName());
        } else if (field.getType() == boolean.class) {
            return data.getBoolean(field.getName());
        } else if (field.getType() == long.class) {
            return data.getLong(field.getName());
        } else if (field.getType() == short.class) {
            return data.getShort(field.getName());
        } else if (field.getType() == byte.class) {
            return data.getByte(field.getName());
        } else if (field.getType() == char.class) {
            return data.getChar(field.getName());
        }
        return null;
    }
}
