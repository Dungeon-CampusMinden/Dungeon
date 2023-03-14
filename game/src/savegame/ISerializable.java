package savegame;

import com.badlogic.gdx.utils.JsonValue;

/** Classes that implement this interface can be serialized and deserialized. */
public interface ISerializable {

    /**
     * Serialize the object into a JsonValue.
     *
     * @return the serialized object
     */
    default JsonValue serialize() {
        return new JsonValue(JsonValue.ValueType.object);
    }

    /**
     * Deserialize the object from a JsonValue.
     *
     * @param data the data to deserialize
     */
    default void deserialize(JsonValue data) {}
}
