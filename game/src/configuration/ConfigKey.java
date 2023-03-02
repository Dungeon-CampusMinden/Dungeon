package configuration;

import configuration.values.ConfigValue;
import java.util.Arrays;

public class ConfigKey<Type> {

    protected String[] path;
    protected final ConfigValue<Type> value;

    public ConfigKey(String[] path, ConfigValue<Type> defaultValue) {
        this.path = Arrays.stream(path).map(String::toLowerCase).toArray(String[]::new);
        this.value = defaultValue;
    }

    public ConfigKey(String path, ConfigValue<Type> defaultValue) {
        this.path =
                Arrays.stream(path.split("\\.")).map(String::toLowerCase).toArray(String[]::new);
        this.value = defaultValue;
    }

    public Type get() {
        return this.value.get();
    }

    public void set(Type value) {
        this.value.set(value);
        Configuration.update(this);
    }
}
