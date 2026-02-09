package core.utils.settings;

import com.badlogic.gdx.Input;
import contrib.entities.CharacterClass;
import contrib.entities.deco.Deco;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

public class ClientSettings {

  public static final String MASTER_VOLUME = "masterVolume";
  public static final String EFFECTS_VOLUME = "effectsVolume";
  public static final String MUSIC_VOLUME = "musicVolume";

  private static ClientSettings instance = null;
  private final HashMap<String, SettingValue<?>> settings;
  private BiConsumer<String, Integer> onVolumeChange = (key, value) -> {};

  private ClientSettings() {
    settings = new LinkedHashMap<>();
  }

  public static ClientSettings getInstance() {
    if (instance == null) {
      instance = new ClientSettings();
      instance.init();
    }
    return instance;
  }

  public static void setOnVolumeChange(BiConsumer<String, Integer> onChange) {
    getInstance().onVolumeChange = onChange;
  }

  private void init() {
    IntSliderSetting masterVolume = new IntSliderSetting("Master Volume", 70, 0, 100, 5);
    IntSliderSetting effectsVolume = new IntSliderSetting("Effects Volume", 70, 0, 100, 5);
    IntSliderSetting musicVolume = new IntSliderSetting("Music Volume", 10, 0, 100, 5);

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
    registerSetting("deco", new EnumSetting<>("Selected Deco", Deco.Desk, Deco.values(), ClientSettings::formatEnumTitle));

    registerSetting("colorblind", new BoolSetting("Colorblind Mode", false));

    registerSetting("section1", new SectionDividerSetting("Controls"));
    registerSetting("controls1", new ButtonBindingSetting("Pause", Input.Keys.P));
    registerSetting("controls2", new ButtonBindingSetting("Interact", Input.Keys.E));
  }

  public static int masterVolume() {
    return (int) getSetting(MASTER_VOLUME).value();
  }

  public static int effectsVolume() {
    return (int) getSetting(EFFECTS_VOLUME).value();
  }

  public static int musicVolume() {
    return (int) getSetting(MUSIC_VOLUME).value();
  }

  public static void registerSetting(String key, SettingValue<?> setting) {
    getInstance().settings.put(key, setting);
  }

  public static SettingValue<?> getSetting(String name) {
    return getInstance().settings.get(name);
  }

  public static <T> T getSettingValue(String name, Class<T> type) {
    SettingValue<?> setting = getInstance().settings.get(name);
    if (setting == null) return null;

    Object value = setting.value();
    if (!type.isInstance(value)) {
      throw new ClassCastException("Setting '" + name + "' is not " + type.getSimpleName());
    }
    return type.cast(value);
  }

  public static HashMap<String, SettingValue<?>> getSettings() {
    return getInstance().settings;
  }



  public static void save(){
    // Serialize to json
  }
  public static void load(){
    // load from json
  }

  private static String formatEnumTitle(Enum<?> e) {
    String[] parts = e.name().toLowerCase().split("_");
    StringBuilder out = new StringBuilder();
    for (String p : parts) {
      out.append(Character.toUpperCase(p.charAt(0)))
        .append(p.substring(1))
        .append(' ');
    }
    return out.toString().trim();
  }
}
