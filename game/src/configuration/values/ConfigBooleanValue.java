package configuration.values;

public class ConfigBooleanValue extends ConfigValue<Boolean> {

    public ConfigBooleanValue(Boolean value) {
        super(value);
    }

    @Override
    public String serialize() {
        return value.toString();
    }

    @Override
    public Boolean deserialize(String value) {
        this.value = Boolean.parseBoolean(value);
        return this.value;
    }
}
