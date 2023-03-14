package configuration.values;

public class ConfigStringValue extends ConfigValue<String> {

    /**
     * Creates a new ConfigValue of type String.
     *
     * @param value Value.
     */
    public ConfigStringValue(String value) {
        super(value);
    }

    /**
     * Serialize the string value to a string.
     *
     * @return The serialized value.
     */
    @Override
    public String serialize() {
        return value;
    }

    /**
     * Deserialize the string value from a string.
     *
     * @param value The string to deserialize.
     * @return The deserialized value.
     */
    @Override
    public String deserialize(String value) {
        this.value = value;
        return this.value;
    }
}
