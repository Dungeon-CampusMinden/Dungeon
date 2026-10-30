package feature.interaction.keypad;

import engine.Entity;
import engine.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Encodes the client-visible state of a numeric keypad in generic entity metadata, so a client can
 * render {@link KeypadUI} for a keypad owned by the server.
 */
public final class KeypadSync {

  public static final String ENTERED_DIGITS_KEY = "keypad.enteredDigits";
  public static final String CODE_LENGTH_KEY = "keypad.codeLength";
  public static final String SHOW_DIGIT_COUNT_KEY = "keypad.showDigitCount";
  public static final String UNLOCKED_KEY = "keypad.isUnlocked";

  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(KeypadSync.class);

  private KeypadSync() {}

  /**
   * Encodes the current keypad state, if the entity has a keypad component.
   *
   * <p>Only state needed to render the keypad is included. The code itself is sent as its length
   * only; actions and callbacks remain local to the authoritative server.
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
                    ENTERED_DIGITS_KEY,
                    component.enteredDigits().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining()),
                    CODE_LENGTH_KEY,
                    Integer.toString(component.correctDigits().size()),
                    SHOW_DIGIT_COUNT_KEY,
                    Boolean.toString(component.showDigitCount()),
                    UNLOCKED_KEY,
                    Boolean.toString(component.isUnlocked())))
        .orElseGet(Map::of);
  }

  /**
   * Applies synchronized state to an entity, creating its keypad component if needed.
   *
   * <p>The client knows only the code length, so the correct digits of the client component are
   * placeholders of that length. Metadata without keypad state leaves the entity unchanged.
   *
   * @param entity client-side entity receiving the metadata
   * @param metadata synchronized entity metadata
   */
  public static void apply(Entity entity, Map<String, String> metadata) {
    String enteredValue = metadata.get(ENTERED_DIGITS_KEY);
    if (enteredValue == null) return;

    if (!enteredValue.chars().allMatch(c -> c >= '0' && c <= '9')) {
      LOGGER.warn("Ignoring invalid keypad entered digits for entity {}", entity.id());
      return;
    }
    int codeLength;
    try {
      codeLength = Integer.parseInt(metadata.getOrDefault(CODE_LENGTH_KEY, ""));
    } catch (NumberFormatException e) {
      LOGGER.warn("Ignoring invalid keypad code length for entity {}", entity.id());
      return;
    }
    if (codeLength < 0) {
      LOGGER.warn("Ignoring negative keypad code length for entity {}", entity.id());
      return;
    }
    List<Integer> entered = enteredValue.chars().map(c -> c - '0').boxed().toList();
    boolean showDigitCount = Boolean.parseBoolean(metadata.get(SHOW_DIGIT_COUNT_KEY));
    boolean unlocked = Boolean.parseBoolean(metadata.get(UNLOCKED_KEY));
    List<Integer> placeholder = Collections.nCopies(codeLength, -1);

    KeypadComponent component =
        entity
            .fetch(KeypadComponent.class)
            .orElseGet(
                () -> {
                  KeypadComponent created =
                      new KeypadComponent(placeholder, new ArrayList<>(), false, showDigitCount);
                  entity.add(created);
                  return created;
                });
    if (component.correctDigits().size() != codeLength) component.correctDigits(() -> placeholder);
    component.enteredDigits().clear();
    component.enteredDigits().addAll(entered);
    component.showDigitCount(showDigitCount);
    component.isUnlocked(unlocked);
  }
}
