package configuration.values;

public abstract class ConfigValue<T> {

    protected T value;

    /**
     * Serialize the value to a string.
     *
     * @return The serialized value.
     */
    public abstract String serialize();

    /**
     * Deserialize the value from a string.
     *
     * @param value The string to deserialize.
     * @return The deserialized value.
     */
    public abstract T deserialize(String value);

    /**
     * Creates a new ConfigValue.
     *
     * @param value Value.
     */
    public ConfigValue(T value) {
        this.value = value;
    }

    /**
     * Get the current value.
     *
     * @return The current value.
     */
    public T get() {
        return value;
    }

    /**
     * Set the value.
     *
     * @param value The new value.
     */
    public void set(T value) {
        this.value = value;
    }
}
