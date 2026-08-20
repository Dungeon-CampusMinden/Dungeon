package feature.leveleditor;

import engine.utils.JsonHandler;
import engine.utils.logging.DungeonLogger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/** Persists level-editor settings in the user's application preferences. */
public final class LevelEditorSettings {

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(LevelEditorSettings.class);
  private static final String SETTINGS_KEY = "settingsByLevel";
  private static final String SAVE_PATH = "savePath";
  private static final String AUTO_SAVE = "autoSave";

  private static Map<String, Object> settingsByLevel;

  private LevelEditorSettings() {}

  /**
   * Loads settings for a level.
   *
   * @param levelAssetPath the level asset path used as the settings key.
   * @param defaultSavePath the save path used when no settings exist for the level.
   * @param defaultAutoSave the auto-save value used when no settings exist for the level.
   * @return the stored settings or the supplied defaults.
   */
  public static Values load(
      String levelAssetPath, String defaultSavePath, boolean defaultAutoSave) {
    ensureLoaded();
    Object stored = levelAssetPath == null ? null : settingsByLevel.get(levelAssetPath);
    if (!(stored instanceof Map<?, ?> levelSettings)) {
      return new Values(defaultSavePath, defaultAutoSave);
    }

    String savePath =
        levelSettings.get(SAVE_PATH) instanceof String storedSavePath
            ? storedSavePath
            : defaultSavePath;
    boolean autoSave =
        levelSettings.get(AUTO_SAVE) instanceof Boolean storedAutoSave
            ? storedAutoSave
            : defaultAutoSave;
    return new Values(savePath, autoSave);
  }

  /**
   * Saves settings for a level.
   *
   * @param levelAssetPath the level asset path used as the settings key.
   * @param values the settings to store.
   */
  public static void save(String levelAssetPath, Values values) {
    if (levelAssetPath == null || levelAssetPath.isBlank()) return;
    ensureLoaded();

    Map<String, Object> levelSettings = new LinkedHashMap<>();
    levelSettings.put(SAVE_PATH, values.savePath());
    levelSettings.put(AUTO_SAVE, values.autoSave());
    settingsByLevel.put(levelAssetPath, levelSettings);

    Preferences preferences = preferences();
    preferences.put(SETTINGS_KEY, JsonHandler.writeJson(settingsByLevel, true));
    try {
      preferences.flush();
    } catch (BackingStoreException e) {
      LOGGER.error("Failed to persist level editor settings.", e);
    }
  }

  private static void ensureLoaded() {
    if (settingsByLevel != null) return;

    settingsByLevel = new HashMap<>();
    String storedSettings = preferences().get(SETTINGS_KEY, "");
    if (storedSettings.isBlank()) return;

    try {
      settingsByLevel = JsonHandler.readJson(storedSettings);
    } catch (IllegalArgumentException e) {
      LOGGER.warn("Failed to parse level editor settings: {}", e.getMessage());
    }
  }

  private static Preferences preferences() {
    return Preferences.userNodeForPackage(LevelEditorSettings.class);
  }

  /**
   * The settings associated with one level asset.
   *
   * @param savePath the folder where the level is saved.
   * @param autoSave whether the level is saved after an edit.
   */
  public record Values(String savePath, boolean autoSave) {}
}
