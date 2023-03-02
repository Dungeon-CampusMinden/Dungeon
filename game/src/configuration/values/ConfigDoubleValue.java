package configuration.values;

public class ConfigDoubleValue implements ConfigValue<Double> {

    private Double value;

    public ConfigDoubleValue(Double value) {
        this.value = value;
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

    @Override
    public Double get() {
        return this.value;
    }

    @Override
    public void set(Double value) {
        this.value = value;
    }
}
