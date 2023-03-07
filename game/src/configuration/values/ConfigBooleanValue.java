package configuration.values;

public class ConfigBooleanValue extends ConfigValue<Boolean> {

    /**
     * Creates a new ConfigValue of type Boolean.
     *
     * @param value Value.
     */
    public ConfigBooleanValue(Boolean value) {
        super(value);
    }

    /**
     * Serialize the boolean value to a string.
     *
     * @return The serialized value.
     */
    @Override
    public String serialize() {
        return value.toString();
    }

    /**
     * Deserialize the boolean value from a string.
     *
     * @param value The string to deserialize.
     * @return
     */
    @Override
    public Boolean deserialize(String value) {
        this.value = Boolean.parseBoolean(value);
        return this.value;
    }
}
