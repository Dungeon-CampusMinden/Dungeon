package wizard.runner.room;

import feature.entities.CharacterClass;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/** Immutable runtime definitions supplied by supported Wizard themes. */
final class WizardThemeCatalog {
  private static final String TEMPLATE_ROOT = "wizard/room-templates/";
  private static final Map<String, List<CharacterClass>> PLAYABLE_CHARACTER_CLASSES =
      Map.of(
          "default",
          List.of(CharacterClass.THE_LAST_HOUR_ROGUE, CharacterClass.THE_LAST_HOUR_CHAR03));
  private static final Map<String, List<String>> TEMPLATE_NAMES =
      Map.of(
          "default",
          List.of(
              "room_small_a",
              "room_small_b",
              "room_medium_a",
              "room_medium_b",
              "room_large_a",
              "room_large_b",
              "corridor_straight_h",
              "corridor_straight_v",
              "corridor_hall",
              "exit_alcove"));
  private static final Map<String, List<RoomTemplate>> TEMPLATES = new ConcurrentHashMap<>();

  private WizardThemeCatalog() {}

  static List<CharacterClass> playableCharacterClasses(final String themeId) {
    return require(PLAYABLE_CHARACTER_CLASSES, themeId);
  }

  /**
   * Returns the checked level templates of one theme, read once from the classpath.
   *
   * @param themeId scenario theme
   * @return templates in catalog order
   */
  static List<RoomTemplate> templates(final String themeId) {
    List<String> names = require(TEMPLATE_NAMES, themeId);
    return TEMPLATES.computeIfAbsent(
        themeId,
        ignored ->
            names.stream()
                .map(name -> RoomTemplate.load(TEMPLATE_ROOT + themeId + "/" + name + ".level"))
                .toList());
  }

  private static <T> T require(final Map<String, T> values, final String themeId) {
    T value = values.get(Objects.requireNonNull(themeId, "themeId"));
    if (value == null) {
      throw new IllegalArgumentException(
          "Cannot derive Foundation room for unknown scenario themeId: " + themeId);
    }
    return value;
  }
}
