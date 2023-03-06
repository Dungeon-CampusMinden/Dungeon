package configuration.values;

public class ConfigFloatValue extends ConfigValue<Float> {

    public ConfigFloatValue(Float value) {
        super(value);
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
}
