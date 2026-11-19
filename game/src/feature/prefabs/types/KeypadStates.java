package feature.prefabs.types;

import engine.Entity;
import feature.interaction.keypad.KeypadComponent;
import java.util.List;
import java.util.Optional;
import tools.jackson.databind.JsonNode;

/** Saved state of the keypad prefabs: the typed text and whether the keypad is unlocked. */
final class KeypadStates {

  private static final String TEXT = "text";
  private static final String UNLOCKED = "unlocked";

  private KeypadStates() {}

  /**
   * Captures the state of a spawned keypad.
   *
   * @param entities entities of the keypad spawn
   * @return the state, or empty if the keypad has no entity
   */
  static Optional<JsonNode> capture(List<Entity> entities) {
    return entities.stream()
        .findFirst()
        .flatMap(keypad -> keypad.fetch(KeypadComponent.class))
        .map(
            keypad ->
                PrefabStateJson.object()
                    .put(TEXT, keypad.enteredText())
                    .put(UNLOCKED, keypad.unlocked()));
  }

  /**
   * Restores a captured state without submitting the code.
   *
   * @param saved captured state
   * @param keypad keypad of the new spawn
   */
  static void restore(JsonNode saved, KeypadComponent keypad) {
    keypad.enteredText(saved.path(TEXT).asString(""));
    keypad.unlocked(saved.path(UNLOCKED).asBoolean());
  }
}
