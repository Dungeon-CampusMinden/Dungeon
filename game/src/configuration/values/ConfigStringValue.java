package configuration.values;

public class ConfigStringValue implements ConfigValue<String> {

    private String value;

    public ConfigStringValue(String value) {
        this.value = value;
    }

    @Override
    public String serialize() {
        return value;
    }

    @Override
    public String deserialize(String value) {
        this.value = value;
        return this.value;
    }

    @Override
    public String get() {
        return this.value;
    }

    @Override
    public void set(String value) {
        this.value = value;
    }
}
