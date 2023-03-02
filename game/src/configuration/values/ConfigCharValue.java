package configuration.values;

public class ConfigCharValue implements ConfigValue<Character> {

    private Character value;

    public ConfigCharValue(Character value) {
        this.value = value;
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

    @Override
    public Character get() {
        return this.value;
    }

    @Override
    public void set(Character value) {
        this.value = value;
    }
}
