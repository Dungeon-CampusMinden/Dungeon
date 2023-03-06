package configuration.values;

public class ConfigCharValue extends ConfigValue<Character> {

    public ConfigCharValue(Character value) {
        super(value);
    }

    @Override
    public String serialize() {
        return value.toString();
    }

    @Override
    public Character deserialize(String value) {
        if (value.length() > 1) {
            throw new IllegalArgumentException(
                    "Cannot deserialize a string with length > 1 to a char");
        }
        this.value = value.charAt(0);
        return this.value;
    }
}
