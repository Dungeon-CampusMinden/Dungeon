package core.utils.settings;

import contrib.entities.CharacterClass;
import contrib.entities.deco.Deco;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class ClientSettings {

  private static ClientSettings instance = null;

  private final HashMap<String, SettingValue<?>> settings;

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

  private void init() {
    IntSliderSetting masterVolume = new IntSliderSetting("Master Volume", 70, 0, 100, 5);
    IntSliderSetting effectsVolume = new IntSliderSetting("Effects Volume", 70, 0, 100, 5);
    IntSliderSetting musicVolume = new IntSliderSetting("Music Volume", 50, 0, 100, 5);

    masterVolume.onChange((v) -> {
      System.out.println("MasterVolume changed to " + v);
    });

    EnumSetting<CharacterClass> heroClassSetting =
        new EnumSetting<>(
            "Hero Class",
            CharacterClass.WIZARD,
            CharacterClass.values(),
            ClientSettings::formatEnumTitle);

    registerSetting("masterVolume", masterVolume);
    registerSetting("effectsVolume", effectsVolume);
    registerSetting("musicVolume", musicVolume);

    registerSetting("hero", heroClassSetting);
    registerSetting("deco", new EnumSetting<>("Selected Deco", Deco.Desk, Deco.values(), ClientSettings::formatEnumTitle));

    registerSetting("colorblind", new BoolSetting("Colorblind Mode 1", false));
    registerSetting("colorblind2", new BoolSetting("Colorblind Mode 2", false));
    registerSetting("colorblind3", new BoolSetting("Colorblind Mode 3", false));
    registerSetting("colorblind4", new BoolSetting("Colorblind Mode 4", false));
  }

  public static int masterVolume() {
    return (int) getSetting("masterVolume").value();
  }

  public static int effectsVolume() {
    return (int) getSetting("effectsVolume").value();
  }

  public static int musicVolume() {
    return (int) getSetting("musicVolume").value();
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
