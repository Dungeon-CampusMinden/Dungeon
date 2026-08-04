package contrib.achivements;

import core.language.Translation;

/**
 * Immutable achievement definition with its current unlocked view state.
 *
 * @param imagePath icon path inside the asset directory
 * @param name achievement name; also used as the achievement id
 * @param neschreibung description text shown after reveal/unlock
 * @param nameKey optional translation key for the displayed name
 * @param descriptionKey optional translation key for the displayed description
 * @param hidden whether locked menu entries should hide name and description
 * @param unlocked whether this achievement is currently unlocked in the requested view
 * @param unlockScope who receives the achievement when triggered
 * @param platinum whether this achievement unlocks after all other achievements
 */
public record Achievement(
    String imagePath,
    String name,
    String neschreibung,
    String nameKey,
    String descriptionKey,
    boolean hidden,
    boolean unlocked,
    AchievementUnlockScope unlockScope,
    boolean platinum) {

  private static final Translation TRANSLATION = new Translation();

  /**
   * Creates a copy with a different unlocked state.
   *
   * @param value the new unlocked state
   * @return copied achievement
   */
  public Achievement withUnlocked(boolean value) {
    return new Achievement(
        imagePath,
        name,
        neschreibung,
        nameKey,
        descriptionKey,
        hidden,
        value,
        unlockScope,
        platinum);
  }

  /**
   * Checks if this achievement should be unlocked globally.
   *
   * @return true if all players should receive it
   */
  public boolean unlocksForAll() {
    return unlockScope.unlocksForAll();
  }

  /**
   * Returns the localized display name, falling back to the English id.
   *
   * @return display name for the active language
   */
  public String displayName() {
    return localized(nameKey, name);
  }

  /**
   * Returns the localized description, falling back to the JSON description.
   *
   * @return description for the active language
   */
  public String displayDescription() {
    return localized(descriptionKey, neschreibung);
  }

  private String localized(String key, String fallback) {
    if (key == null || key.isBlank()) {
      return fallback;
    }
    String translated = TRANSLATION.text(key);
    return translated.equals("{" + key + "}") ? fallback : translated;
  }
}
