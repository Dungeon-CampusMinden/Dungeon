package core.utils.settings;

import core.language.Language;
import core.language.Localization;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.function.BiConsumer;

/** Manages client-side settings for the game, such as volume levels and control bindings. */
public class ClientSettings {

  /** Key for master volume setting. */
  public static final String KEY_MASTER_VOLUME = "settings.master_volume";

  /** Key for effects volume setting. */
  public static final String KEY_EFFECTS_VOLUME = "settings.effects_volume";

  /** Key for music volume setting. */
  public static final String KEY_MUSIC_VOLUME = "settings.music_volume";

  /** Key for language setting. */
  public static final String KEY_LANGUAGE = "settings.language";

  private static ClientSettings instance = null;
  private final HashMap<String, SettingValue<?>> settings;
  private final HashMap<String, Boolean> onlyIngame;
  private BiConsumer<String, Integer> onVolumeChange = (key, value) -> {};

  private ClientSettings() {
    settings = new LinkedHashMap<>();
    onlyIngame = new HashMap<>();
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
    Localization localization = Localization.getInstance();
    IntSliderSetting masterVolume = new IntSliderSetting(KEY_MASTER_VOLUME, 70, 0, 100, 5);
    IntSliderSetting effectsVolume = new IntSliderSetting(KEY_EFFECTS_VOLUME, 70, 0, 100, 5);
    IntSliderSetting musicVolume = new IntSliderSetting(KEY_MUSIC_VOLUME, 5, 0, 100, 5);

    masterVolume.onChange((v) -> onVolumeChange.accept(KEY_MASTER_VOLUME, v));
    effectsVolume.onChange((v) -> onVolumeChange.accept(KEY_EFFECTS_VOLUME, v));
    musicVolume.onChange((v) -> onVolumeChange.accept(KEY_MUSIC_VOLUME, v));

    registerSetting(masterVolume);
    registerSetting(effectsVolume);
    registerSetting(musicVolume);

    EnumSetting<Language> language =
        new EnumSetting<>(KEY_LANGUAGE, localization.currentLanguage());
    language.onChange(localization::currentLanguage);
    registerSetting(language);
  }

  /**
   * Convenience methods for accessing the master volume.
   *
   * @return the current master volume level as an integer
   */
  public static int masterVolume() {
    return (int) getSetting(KEY_MASTER_VOLUME).value();
  }

  /**
   * Convenience methods for accessing the effects volume.
   *
   * @return the current effects volume level as an integer
   */
  public static int effectsVolume() {
    return (int) getSetting(KEY_EFFECTS_VOLUME).value();
  }

  /**
   * Convenience methods for accessing the music volume.
   *
   * @return the current music volume level as an integer
   */
  public static int musicVolume() {
    return (int) getSetting(KEY_MUSIC_VOLUME).value();
  }

  /**
   * Registers a new setting. The setting's translation key is used as unique key. If a setting with
   * the same key already exists, it will be overwritten.
   *
   * @param setting the SettingValue object representing the setting to be registered
   */
  public static void registerSetting(SettingValue<?> setting) {
    Objects.requireNonNull(setting, "setting");
    getInstance().settings.put(setting.translationKey(), setting);
  }

  /**
   * Marks whether a setting should only be shown while in-game and hidden in the main menu.
   *
   * <p>By default every setting is visible in both the main menu and the in-game settings. Calling
   * this with {@code ingame == true} restricts the setting to the in-game settings only.
   *
   * @param key the translation key identifying the setting
   * @param ingame {@code true} if the setting should only be shown in-game (hidden in the main
   *     menu); {@code false} to show it in both places
   */
  public static void setOnlyIngame(String key, boolean ingame) {
    Objects.requireNonNull(key, "key");
    getInstance().onlyIngame.put(key, ingame);
  }

  /**
   * Returns whether the setting with the given key is restricted to the in-game settings.
   *
   * @param key the translation key identifying the setting
   * @return {@code true} if the setting should only be shown in-game
   */
  public static boolean isOnlyIngame(String key) {
    return getInstance().onlyIngame.getOrDefault(key, false);
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
   * Returns the settings that should be shown for the given context, preserving insertion order.
   *
   * <p>When {@code ingame} is {@code false} (main menu), settings marked via {@link
   * #setOnlyIngame(String, boolean)} are omitted. When {@code ingame} is {@code true}, all settings
   * are returned.
   *
   * @param ingame {@code true} if the settings are built for the in-game menu, {@code false} for
   *     the main menu
   * @return an ordered map of the settings visible in the requested context
   */
  public static LinkedHashMap<String, SettingValue<?>> getSettings(boolean ingame) {
    LinkedHashMap<String, SettingValue<?>> visible = new LinkedHashMap<>();
    getInstance()
        .settings
        .forEach(
            (key, setting) -> {
              if (ingame || !isOnlyIngame(key)) {
                visible.put(key, setting);
              }
            });
    return visible;
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
}
