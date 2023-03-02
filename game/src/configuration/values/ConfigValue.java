package configuration.values;

public interface ConfigValue<T> {

    String serialize();

    T deserialize(String value);

    T get();

    void set(T value);
}
