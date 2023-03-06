package configuration.values;

public class ConfigDoubleValue extends ConfigValue<Double> {

    /**
     * Creates a new ConfigValue of type Double.
     *
     * @param value Value.
     */
    public ConfigDoubleValue(Double value) {
        super(value);
    }

    /**
     * Serialize the double value to a string.
     *
     * @return The serialized value.
     */
    @Override
    public String serialize() {
        return value.toString();
    }

    /**
     * Deserialize the double value from a string.
     *
     * @param value The string to deserialize.
     * @return The deserialized value.
     */
    @Override
    public Double deserialize(String value) {
        this.value = Double.parseDouble(value);
        return this.value;
    }
}
