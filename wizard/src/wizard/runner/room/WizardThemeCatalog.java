package wizard.runner.room;

import contrib.entities.CharacterClass;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable runtime definitions supplied by supported Wizard themes. */
final class WizardThemeCatalog {
  private static final Map<String, List<CharacterClass>> PLAYABLE_CHARACTER_CLASSES =
      Map.of(
          "default",
          List.of(CharacterClass.THE_LAST_HOUR_ROGUE, CharacterClass.THE_LAST_HOUR_CHAR03));

  private WizardThemeCatalog() {}

  static List<CharacterClass> playableCharacterClasses(final String themeId) {
    List<CharacterClass> characterClasses =
        PLAYABLE_CHARACTER_CLASSES.get(Objects.requireNonNull(themeId, "themeId"));
    if (characterClasses == null) {
      throw new IllegalArgumentException(
          "Cannot derive Foundation room for unknown scenario themeId: " + themeId);
    }
    return characterClasses;
  }
}
