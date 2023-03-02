package configuration.values;

public class ConfigFloatValue implements ConfigValue<Float> {

    private Float value;

    public ConfigFloatValue(Float value) {
        this.value = value;
    }

    @Override
    public String serialize() {
        return value.toString();
    }

    @Override
    public Float deserialize(String value) {
        this.value = Float.parseFloat(value);
        return this.value;
    }

    @Override
    public Float get() {
        return this.value;
    }

    @Override
    public void set(Float value) {
        this.value = value;
    }
}
