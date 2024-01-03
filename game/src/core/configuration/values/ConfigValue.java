package core.configuration.values;

/**
 * An abstract class representing a generic configuration value.
 *
 * <p>This class provides a foundation for creating specialized configuration value classes for
 * different data types. It includes methods for serializing and deserializing the configuration
 * value to and from strings, as well as getters and setters for the value.
 *
 * <p>Subclasses should implement the abstract methods {@link #serialize()} and {@link
 * #deserialize(String)} to define how the value should be converted to and from a string
 * representation.
 *
 * @param <T> The type of the configuration value.
 */
public abstract class ConfigValue<T> {

  /** The current value of the configuration. */
  protected T value;

  /**
   * Creates a new ConfigValue with an initial value.
   *
   * @param value The initial value.
   */
  public ConfigValue(final T value) {
    this.value = value;
  }

  /**
   * Serialize the value to a string.
   *
   * @return The serialized value.
   */
  public abstract String serialize();

  /**
   * Deserialize the value from a string.
   *
   * @param value The string to deserialize.
   * @return The deserialized value.
   */
  public abstract T deserialize(final String value);

  /**
   * Get the current value.
   *
   * @return The current value.
   */
  public T value() {
    return value;
  }

  /**
   * Set the value.
   *
   * @param value The new value.
   */
  public void value(final T value) {
    this.value = value;
  }
}
