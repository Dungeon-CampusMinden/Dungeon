package configuration.values;

public class ConfigIntValue implements ConfigValue<Integer> {

    private Integer value;

    public ConfigIntValue(Integer value) {
        this.value = value;
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

    @Override
    public Integer get() {
        return this.value;
    }

    @Override
    public void set(Integer value) {
        this.value = value;
    }
}
