package core.configuration;

import core.configuration.values.ConfigValue;
import java.util.Arrays;

/**
 * A ConfigKey is a key to a value in the configuration file.
 *
 * @param <Type> The type of the value.
 */
public class ConfigKey<Type> {

  protected final ConfigValue<Type> value;
  protected String[] path;
  protected Configuration configuration;

  /**
   * Creates a new ConfigKey.
   *
   * @param path The path to the value in the configuration file as array of strings.
   * @param defaultValue The default value for this key.
   */
  public ConfigKey(String[] path, ConfigValue<Type> defaultValue) {
    this.path = Arrays.stream(path).map(String::toLowerCase).toArray(String[]::new);
    this.value = defaultValue;
  }

  /**
   * Get the current value of this key. If not set, the default value will be returned.
   *
   * @return The current or default value of this key.
   */
  public Type value() {
    return this.value.value();
  }

  /**
   * Set the value of this key. This will also update the configuration file.
   *
   * @param value The new value of this key.
   */
  public void value(Type value) {
    this.value.value(value);
    if (configuration != null) configuration.update((this));
  }
}
