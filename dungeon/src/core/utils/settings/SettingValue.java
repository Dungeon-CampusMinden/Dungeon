package core.utils.settings;

import com.badlogic.gdx.scenes.scene2d.Actor;
import java.util.function.Consumer;

/**
 * Abstract base class representing a configurable setting with a name, value, and optional change
 * listener.
 *
 * @param <T> the type of the setting's value
 */
public abstract class SettingValue<T> {

  private String name;
  private T value;

  private Consumer<T> onChange;

  /**
   * Creates a new SettingValue with the specified name and default value.
   *
   * @param name the name of the setting
   * @param defaultValue the default value of the setting
   */
  public SettingValue(String name, T defaultValue) {
    this.name = name;
    this.value = defaultValue;
  }

  /**
   * Creates a new SettingValue with the specified name, default value, and change listener.
   *
   * @param name the name of the setting
   * @param defaultValue the default value of the setting
   * @param onChange a Consumer that will be called whenever the setting's value changes, receiving
   *     the new value as an argument
   */
  public SettingValue(String name, T defaultValue, Consumer<T> onChange) {
    this(name, defaultValue);
    this.onChange = onChange;
  }

  /**
   * Returns the name of the setting.
   *
   * @return the name of the setting
   */
  public String name() {
    return name;
  }

  /**
   * Sets the name of the setting.
   *
   * @param name the new name of the setting
   */
  public void name(String name) {
    this.name = name;
  }

  /**
   * Returns the current value of the setting.
   *
   * @return the current value of the setting
   */
  public T value() {
    return value;
  }

  /**
   * Sets the value of the setting and triggers the onChange listener if it is set.
   *
   * @param value the new value of the setting
   */
  public void value(T value) {
    this.value = value;
    if (onChange != null) {
      onChange.accept(value);
    }
  }

  /**
   * Sets the onChange listener for this setting.
   *
   * @param listener a Consumer that will be called whenever the setting's value changes, receiving
   *     the new value as an argument
   */
  public void onChange(Consumer<T> listener) {
    this.onChange = listener;
  }

  /**
   * Converts this setting into a UI Actor that can be displayed in the game's settings menu.
   *
   * @return an Actor representing this setting in the UI
   */
  public abstract Actor toUIActor();
}
