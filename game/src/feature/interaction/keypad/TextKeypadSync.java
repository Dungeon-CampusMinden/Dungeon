package feature.interaction.keypad;

import engine.Entity;
import engine.utils.logging.DungeonLogger;
import java.util.Map;

/** Encodes the small client-visible state of a text keypad in generic entity metadata. */
public final class TextKeypadSync {

  public static final String ENTERED_TEXT_KEY = "textKeypad.enteredText";
  public static final String UNLOCKED_KEY = "textKeypad.isUnlocked";

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(TextKeypadSync.class);

  private TextKeypadSync() {}

  /**
   * Encodes the current text keypad state, if the entity has a text keypad component.
   *
   * <p>Only state needed to render the keypad is included. Accepted answers, task state, actions
   * and callbacks remain local to the authoritative server.
   *
   * @param entity entity to inspect
   * @return an immutable metadata map, or an empty map for entities without a text keypad
   */
  public static Map<String, String> metadata(Entity entity) {
    return entity
        .fetch(TextKeyPadComponent.class)
        .map(
            component -> {
              String enteredText = component.enteredText();
              if (enteredText == null) {
                LOGGER.warn(
                    "Cannot encode text keypad state for entity {}: entered text is null",
                    entity.id());
                return Map.<String, String>of();
              }
              return Map.of(
                  ENTERED_TEXT_KEY,
                  enteredText,
                  UNLOCKED_KEY,
                  Boolean.toString(component.isUnlocked()));
            })
        .orElseGet(Map::of);
  }

  /**
   * Applies synchronized state to an entity while preserving all unrelated local component state.
   *
   * <p>Missing metadata keys leave the corresponding component values unchanged. If this entity
   * does not yet have a text keypad component, one is created with its no-op action before the
   * supplied display state is applied.
   *
   * @param entity client-side entity receiving the metadata
   * @param metadata synchronized entity metadata
   */
  public static void apply(Entity entity, Map<String, String> metadata) {
    boolean hasEnteredText = metadata.containsKey(ENTERED_TEXT_KEY);
    boolean hasUnlocked = metadata.containsKey(UNLOCKED_KEY);
    if (!hasEnteredText && !hasUnlocked) return;

    String enteredText = metadata.get(ENTERED_TEXT_KEY);
    if (hasEnteredText && enteredText == null) {
      LOGGER.warn(
          "Ignoring invalid text keypad entered-text metadata for entity {}: value is null",
          entity.id());
      hasEnteredText = false;
    }

    String unlockedValue = metadata.get(UNLOCKED_KEY);
    Boolean unlocked = null;
    if (hasUnlocked) {
      if ("true".equals(unlockedValue)) {
        unlocked = true;
      } else if ("false".equals(unlockedValue)) {
        unlocked = false;
      } else {
        LOGGER.warn(
            "Ignoring invalid text keypad unlocked metadata for entity {}: expected true or false",
            entity.id());
      }
    }

    if (!hasEnteredText && unlocked == null) return;

    TextKeyPadComponent component =
        entity
            .fetch(TextKeyPadComponent.class)
            .orElseGet(
                () -> {
                  TextKeyPadComponent created = new TextKeyPadComponent("", false);
                  entity.add(created);
                  return created;
                });
    if (hasEnteredText) component.setEnteredText(enteredText);
    if (unlocked != null) component.isUnlocked(unlocked);
  }
}
