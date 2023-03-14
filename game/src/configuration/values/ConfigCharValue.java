package configuration.values;

public class ConfigCharValue extends ConfigValue<Character> {

    /**
     * Creates a new ConfigValue of type Character.
     *
     * @param value Value.
     */
    public ConfigCharValue(Character value) {
        super(value);
    }

    /**
     * Serialize the char value to a string.
     *
     * @return The serialized value.
     */
    @Override
    public String serialize() {
        return value.toString();
    }

    /**
     * Deserialize the char value from a string.
     *
     * @param value The string to deserialize.
     * @return The deserialized value.
     */
    @Override
    public Character deserialize(String value) {
        if (value.length() > 1) {
            throw new IllegalArgumentException(
                    "Cannot deserialize a string with length > 1 to a char");
        }
        this.value = value.charAt(0);
        return this.value;
    }
}
