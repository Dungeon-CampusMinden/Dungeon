package configuration.values;

public class ConfigFloatValue extends ConfigValue<Float> {

    /**
     * Creates a new ConfigValue of type Float.
     *
     * @param value Value.
     */
    public ConfigFloatValue(Float value) {
        super(value);
    }

    /**
     * Serialize the float value to a string.
     *
     * @return The serialized value.
     */
    @Override
    public String serialize() {
        return value.toString();
    }

    /**
     * Deserialize the float value from a string.
     *
     * @param value The string to deserialize.
     * @return The deserialized value.
     */
    @Override
    public Float deserialize(String value) {
        this.value = Float.parseFloat(value);
        return this.value;
    }
}
