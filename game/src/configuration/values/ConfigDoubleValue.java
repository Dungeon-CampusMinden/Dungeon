package configuration.values;

public class ConfigDoubleValue extends ConfigValue<Double> {

    public ConfigDoubleValue(Double value) {
        super(value);
    }

    @Override
    public String serialize() {
        return value.toString();
    }

    @Override
    public Double deserialize(String value) {
        this.value = Double.parseDouble(value);
        return this.value;
    }
}
