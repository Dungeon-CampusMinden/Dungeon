package configuration.values;

public class ConfigIntValue extends ConfigValue<Integer> {

    public ConfigIntValue(Integer value) {
        super(value);
    }

    @Override
    public String serialize() {
        return value.toString();
    }

    @Override
    public Integer deserialize(String value) {
        this.value = Integer.parseInt(value);
        return this.value;
    }
}
