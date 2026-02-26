package core.utils.settings;

import com.badlogic.gdx.Input;
import contrib.entities.CharacterClass;
import contrib.entities.deco.Deco;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

/** Manages client-side settings for the game, such as volume levels and control bindings. */
public class ClientSettings {

  /** Key for master volume setting. */
  public static final String MASTER_VOLUME = "masterVolume";

  /** Key for effects volume setting. */
  public static final String EFFECTS_VOLUME = "effectsVolume";

  /** Key for music volume setting. */
  public static final String MUSIC_VOLUME = "musicVolume";

  private static ClientSettings instance = null;
  private final HashMap<String, SettingValue<?>> settings;
  private BiConsumer<String, Integer> onVolumeChange = (key, value) -> {};

  private ClientSettings() {
    settings = new LinkedHashMap<>();
  }

  /**
   * Creates and or returns the singleton instance of ClientSettings. Initializes default settings
   * on first call.
   *
   * @return the singleton instance of ClientSettings
   */
  public static ClientSettings getInstance() {
    if (instance == null) {
      instance = new ClientSettings();
      instance.init();
    }
    return instance;
  }

  /**
   * Sets the callback function to be called when a volume setting changes. The callback receives
   * the key of the changed setting and its new value.
   *
   * @param onChange a BiConsumer that takes a String key and an Integer value, to be called on
   *     volume change
   */
  public static void setOnVolumeChange(BiConsumer<String, Integer> onChange) {
    getInstance().onVolumeChange = onChange;
  }

  private void init() {
    IntSliderSetting masterVolume = new IntSliderSetting("Master Volume", 70, 0, 100, 5);
    IntSliderSetting effectsVolume = new IntSliderSetting("Effects Volume", 70, 0, 100, 5);
    IntSliderSetting musicVolume = new IntSliderSetting("Music Volume", 0, 0, 100, 5);

    masterVolume.onChange((v) -> onVolumeChange.accept(MASTER_VOLUME, v));
    effectsVolume.onChange((v) -> onVolumeChange.accept(EFFECTS_VOLUME, v));
    musicVolume.onChange((v) -> onVolumeChange.accept(MUSIC_VOLUME, v));

    EnumSetting<CharacterClass> heroClassSetting =
        new EnumSetting<>(
            "Hero Class",
            CharacterClass.WIZARD,
            CharacterClass.values(),
            ClientSettings::formatEnumTitle);

    registerSetting(MASTER_VOLUME, masterVolume);
    registerSetting(EFFECTS_VOLUME, effectsVolume);
    registerSetting(MUSIC_VOLUME, musicVolume);

    registerSetting("hero", heroClassSetting);
    registerSetting(
        "deco",
        new EnumSetting<>(
            "Selected Deco", Deco.Desk, Deco.values(), ClientSettings::formatEnumTitle));

    registerSetting("colorblind", new BoolSetting("Colorblind Mode", false));

    registerSetting("section1", new SectionDividerSetting("Controls"));
    registerSetting("controls1", new ButtonBindingSetting("Pause", Input.Keys.P, false));
    registerSetting("controls2", new ButtonBindingSetting("Interact", Input.Keys.E));
  }

  /**
   * Convenience methods for accessing the master volume.
   *
   * @return the current master volume level as an integer
   */
  public static int masterVolume() {
    return (int) getSetting(MASTER_VOLUME).value();
  }

  /**
   * Convenience methods for accessing the effects volume.
   *
   * @return the current effects volume level as an integer
   */
  public static int effectsVolume() {
    return (int) getSetting(EFFECTS_VOLUME).value();
  }

  /**
   * Convenience methods for accessing the music volume.
   *
   * @return the current music volume level as an integer
   */
  public static int musicVolume() {
    return (int) getSetting(MUSIC_VOLUME).value();
  }

  /**
   * Registers a new setting with the given key. If a setting with the same key already exists, it
   * will be overwritten.
   *
   * @param key the unique identifier for the setting
   * @param setting the SettingValue object representing the setting to be registered
   */
  public static void registerSetting(String key, SettingValue<?> setting) {
    getInstance().settings.put(key, setting);
  }

  /**
   * Retrieves the setting associated with the given key. Returns null if no setting is found for
   * the key.
   *
   * @param name the unique identifier for the setting to retrieve
   * @return the SettingValue object associated with the key, or null if not found
   */
  public static SettingValue<?> getSetting(String name) {
    return getInstance().settings.get(name);
  }

  /**
   * Retrieves the value of the setting associated with the given key, cast to the specified type.
   * Returns null if no setting is found for the key.
   *
   * @param name the unique identifier for the setting to retrieve
   * @param type the Class object representing the expected type of the setting's value
   * @param <T> the expected type of the setting's value
   * @return the value of the setting cast to the specified type, or null if not found
   * @throws ClassCastException if the setting's value cannot be cast to the specified type
   */
  public static <T> T getSettingValue(String name, Class<T> type) {
    SettingValue<?> setting = getInstance().settings.get(name);
    if (setting == null) return null;

    Object value = setting.value();
    if (!type.isInstance(value)) {
      throw new ClassCastException("Setting '" + name + "' is not " + type.getSimpleName());
    }
    return type.cast(value);
  }

  /**
   * Returns the entire map of settings, allowing for iteration or bulk operations.
   *
   * @return a HashMap containing all registered settings, where keys are setting identifiers and
   *     values are SettingValue objects
   */
  public static HashMap<String, SettingValue<?>> getSettings() {
    return getInstance().settings;
  }

  /**
   * Serializes the settings to a JSON file for persistent storage.
   *
   * <p>TODO: Implement this
   */
  public static void save() {
    // Serialize to json
  }

  /**
   * Loads the settings from a JSON file, restoring previously saved settings.
   *
   * <p>TODO: Implement this
   */
  public static void load() {
    // load from json
  }

  private static String formatEnumTitle(Enum<?> e) {
    String[] parts = e.name().toLowerCase().split("_");
    StringBuilder out = new StringBuilder();
    for (String p : parts) {
      out.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1)).append(' ');
    }
    return out.toString().trim();
  }
}
