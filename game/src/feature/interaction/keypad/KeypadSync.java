package feature.interaction.keypad;

import engine.Entity;
import engine.utils.logging.DungeonLogger;
import java.util.Map;

/**
 * Encodes the client-visible state of a numeric or text keypad in generic entity metadata, so a
 * client can render the {@link KeypadUI} of a keypad owned by the server.
 */
public final class KeypadSync {

  public static final String ENTERED_TEXT_KEY = "keypad.enteredText";
  public static final String MAX_LENGTH_KEY = "keypad.maxLength";
  public static final String SHOW_LENGTH_KEY = "keypad.showLength";
  public static final String UNLOCKED_KEY = "keypad.unlocked";

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(KeypadSync.class);

  private KeypadSync() {}

  /**
   * Encodes the current keypad state, if the entity has a keypad component.
   *
   * <p>Only state needed to render the keypad is included. Answers, tasks and callbacks remain
   * local to the authoritative server; of a numeric code, only the input limit is sent.
   *
   * @param entity entity to inspect
   * @return an immutable metadata map, or an empty map for entities without a keypad
   */
  public static Map<String, String> metadata(Entity entity) {
    return entity
        .fetch(KeypadComponent.class)
        .map(
            component ->
                Map.of(
                    ENTERED_TEXT_KEY,
                    component.enteredText(),
                    MAX_LENGTH_KEY,
                    Integer.toString(component.maxLength()),
                    SHOW_LENGTH_KEY,
                    Boolean.toString(component.showLength()),
                    UNLOCKED_KEY,
                    Boolean.toString(component.unlocked())))
        .orElseGet(Map::of);
  }

  /**
   * Applies synchronized state to an entity, creating or replacing its keypad component if needed.
   *
   * <p>Metadata without keypad state leaves the entity unchanged.
   *
   * @param entity client-side entity receiving the metadata
   * @param metadata synchronized entity metadata
   */
  public static void apply(Entity entity, Map<String, String> metadata) {
    String enteredText = metadata.get(ENTERED_TEXT_KEY);
    if (enteredText == null) return;

    int maxLength;
    try {
      maxLength = Integer.parseInt(metadata.getOrDefault(MAX_LENGTH_KEY, ""));
    } catch (NumberFormatException e) {
      LOGGER.warn("Ignoring invalid keypad max length for entity {}", entity.id());
      return;
    }
    boolean showLength = Boolean.parseBoolean(metadata.get(SHOW_LENGTH_KEY));
    boolean unlocked = Boolean.parseBoolean(metadata.get(UNLOCKED_KEY));

    KeypadComponent component =
        entity
            .fetch(KeypadComponent.class)
            .filter(
                current -> current.maxLength() == maxLength && current.showLength() == showLength)
            .orElseGet(
                () -> {
                  KeypadComponent created = new KeypadComponent(() -> maxLength, showLength);
                  entity.add(created);
                  return created;
                });
    component.enteredText(enteredText);
    component.unlocked(unlocked);
  }
}
