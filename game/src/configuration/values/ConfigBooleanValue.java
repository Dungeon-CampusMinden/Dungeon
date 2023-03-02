package configuration.values;

public class ConfigBooleanValue implements ConfigValue<Boolean> {

    private Boolean value;

    public ConfigBooleanValue(Boolean value) {
        this.value = value;
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

    @Override
    public Boolean get() {
        return this.value;
    }

    @Override
    public void set(Boolean value) {
        this.value = value;
    }
}
