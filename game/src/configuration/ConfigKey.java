package configuration;

import configuration.values.ConfigValue;
import java.util.Arrays;
import java.util.Optional;

/**
 * A ConfigKey is a key to a value in the configuration file.
 *
 * @param <Type> The type of the value.
 */
public class ConfigKey<Type> {

    protected String[] path;
    protected final ConfigValue<Type> value;
    protected Optional<Configuration> configuration = Optional.empty();

    /**
     * Creates a new ConfigKey.
     *
     * @param path The path to the value in the configuration file as array of strings.
     * @param defaultValue The default value for this key.
     */
    public ConfigKey(String[] path, ConfigValue<Type> defaultValue) {
        this.path = Arrays.stream(path).map(String::toLowerCase).toArray(String[]::new);
        this.value = defaultValue;
    }

    /**
     * Creates a new ConfigKey.
     *
     * @param path The path to the value in the configuration file as string, seperated by dots.
     * @param defaultValue The default value for this key.
     */
    public ConfigKey(String path, ConfigValue<Type> defaultValue) {
        this.path =
                Arrays.stream(path.split("\\.")).map(String::toLowerCase).toArray(String[]::new);
        this.value = defaultValue;
    }

    /**
     * Get the current value of this key. If not set, the default value will be returned.
     *
     * @return The current or default value of this key.
     */
    public Type get() {
        return this.value.get();
    }

    /**
     * Set the value of this key. This will also update the configuration file.
     *
     * @param value The new value of this key.
     */
    public void set(Type value) {
        this.value.set(value);
        this.configuration.ifPresent(c -> c.update(this));
    }
}
