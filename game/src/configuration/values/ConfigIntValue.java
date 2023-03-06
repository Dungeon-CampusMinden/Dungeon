package configuration.values;

public class ConfigIntValue extends ConfigValue<Integer> {

    /**
     * Creates a new ConfigValue of type Integer.
     *
     * @param value Value.
     */
    public ConfigIntValue(Integer value) {
        super(value);
    }

    /**
     * Serialize the integer value to a string.
     *
     * @return The serialized value.
     */
    @Override
    public String serialize() {
        return value.toString();
    }

    /**
     * Deserialize the integer value from a string.
     *
     * @param value The string to deserialize.
     * @return The deserialized value.
     */
    @Override
    public Integer deserialize(String value) {
        this.value = Integer.parseInt(value);
        return this.value;
    }
}
