package configuration.values;

/**
 * Allows Storing and Retrieving an enum value from the configuration
 *
 * @param <T> the enum class which should be stored or retrieved from the Config
 */
public class ConfigEnumValue<T extends Enum<T>> extends ConfigValue<T> {

    private Class<T> enumClass;
    /**
     * Creates a new ConfigValue.
     *
     * @param value Value.
     */
    public ConfigEnumValue(T value) {
        super(value);
        enumClass = value.getDeclaringClass();
    }

    @Override
    public String serialize() {
        return this.value.name();
    }

    @Override
    public T deserialize(String value) {
        return Enum.valueOf(enumClass, value);
    }
}
