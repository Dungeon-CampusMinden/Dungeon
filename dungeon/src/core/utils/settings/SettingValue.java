package core.utils.settings;

import com.badlogic.gdx.scenes.scene2d.Actor;
import core.language.Translation;
import java.util.function.Consumer;

/**
 * Abstract base class representing a configurable setting with a translation key, value, and
 * optional change listener.
 *
 * @param <T> the type of the setting's value
 */
public abstract class SettingValue<T> {

  private static final Translation TRANSLATION = new Translation();

  private String translationKey;
  private T value;

  private Consumer<T> onChange;

  /**
   * Creates a new SettingValue with the specified translation key and default value.
   *
   * @param translationKey the translation key of the setting label
   * @param defaultValue the default value of the setting
   */
  public SettingValue(String translationKey, T defaultValue) {
    this.translationKey = translationKey;
    this.value = defaultValue;
  }

  /**
   * Creates a new SettingValue with the specified translation key, default value, and change
   * listener.
   *
   * @param translationKey the translation key of the setting label
   * @param defaultValue the default value of the setting
   * @param onChange a Consumer that will be called whenever the setting's value changes, receiving
   *     the new value as an argument
   */
  public SettingValue(String translationKey, T defaultValue, Consumer<T> onChange) {
    this(translationKey, defaultValue);
    this.onChange = onChange;
  }

  /**
   * Returns the translated display name of the setting in the current language.
   *
   * @return the translated label of the setting
   */
  public String name() {
    return TRANSLATION.text(translationKey);
  }

  /**
   * Returns the translation key used for this setting label.
   *
   * @return translation key for this setting label
   */
  public String translationKey() {
    return translationKey;
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
