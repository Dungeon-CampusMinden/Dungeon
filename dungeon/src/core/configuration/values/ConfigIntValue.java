package core.configuration.values;

/**
 * ConfigIntValue is a specialized configuration value class for storing integer values.
 *
 * <p>This class extends {@link ConfigValue} and is designed specifically for handling integer
 * configuration values. It provides methods to serialize and deserialize integer values as strings.
 *
 * <p>This class is particularly useful for storing key references in libGDX, where keys are
 * represented by integers (see {@link com.badlogic.gdx.Input.Keys}).
 *
 * @see ConfigValue
 * @see com.badlogic.gdx.Input.Keys
 */
public final class ConfigIntValue extends ConfigValue<Integer> {

  /**
   * Creates a new ConfigValue of type Integer.
   *
   * <p>This is used to store key references. In libGDX, keys are represented by integers.
   *
   * @param value Value.
   * @see com.badlogic.gdx.Input.Keys
   */
  public ConfigIntValue(final Integer value) {
    super(value);
  }

  /**
   * Serialize the integer value to a string.
   *
   * @return The serialized value.
   */
  @Override
  public String serialize() {
    return value.toString();
  }

  /**
   * Deserialize the integer value from a string.
   *
   * @param value The string to deserialize.
   * @return The deserialized value.
   */
  @Override
  public Integer deserialize(final String value) {
    this.value = Integer.parseInt(value);
    return this.value;
  }
}
