package configuration.values;

public class ConfigStringValue extends ConfigValue<String> {

    public ConfigStringValue(String value) {
        super(value);
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
}
