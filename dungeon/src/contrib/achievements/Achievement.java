package contrib.achievements;

import core.language.Translation;

/**
 * Immutable achievement definition.
 *
 * @param imagePath icon path inside the asset directory
 * @param id achievement id
 * @param hidden whether locked menu entries should hide name and description
 * @param forAll whether all players receive the achievement when it is triggered
 * @param platinum whether this achievement unlocks after all other achievements
 */
public record Achievement(
    String imagePath, String id, boolean hidden, boolean forAll, boolean platinum) {

  private static final Translation TRANSLATION = new Translation();
  private static final String TRANSLATION_PREFIX = "achievements.";

  /**
   * Checks if this achievement should be unlocked globally.
   *
   * @return true if all players should receive it
   */
  public boolean unlocksForAll() {
    return forAll;
  }

  /**
   * Returns the localized display name, falling back to the English id.
   *
   * @return display name for the active language
   */
  public String displayName() {
    return localized(translationKey("name"), id);
  }

  /**
   * Returns the localized description.
   *
   * @return description for the active language
   */
  public String displayDescription() {
    return localized(translationKey("description"), "");
  }

  private String translationKey(String field) {
    return TRANSLATION_PREFIX + id + "." + field;
  }

  private String localized(String key, String fallback) {
    if (key == null || key.isBlank()) {
      return fallback;
    }
    String translated = TRANSLATION.text(key);
    return translated.equals("{" + key + "}") ? fallback : translated;
  }
}
