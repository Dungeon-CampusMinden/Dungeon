package configuration.values;

public abstract class ConfigValue<T> {

    protected T value;

    public abstract String serialize();

    public abstract T deserialize(String value);

    public ConfigValue(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
