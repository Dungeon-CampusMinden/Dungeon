package feature.prefabs.types;

import engine.Component;
import engine.Entity;
import engine.components.PlayerComponent;
import feature.prefabs.PrefabProperty;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/** How often a trigger prefab may raise its event. */
enum TriggerRepetition {
  ALWAYS("Always"),
  ONCE_PER_PLAYER("Once per player"),
  ONCE_GLOBALLY("Once globally");

  private final String label;

  TriggerRepetition(String label) {
    this.label = label;
  }

  /**
   * Creates the shared selection property for choosing a repetition mode.
   *
   * @return selection property storing the mode label
   */
  static PrefabProperty<String> property() {
    return PrefabProperty.selection(
        "repetition",
        "Repetition",
        ALWAYS.label,
        Arrays.stream(values()).map(value -> value.label).toList());
  }

  /**
   * Resolves a mode from its serialized label.
   *
   * @param label serialized label
   * @return matching mode
   */
  static TriggerRepetition fromLabel(String label) {
    for (TriggerRepetition value : values()) {
      if (value.label.equals(label)) return value;
    }
    throw new IllegalArgumentException("Unknown trigger repetition: " + label);
  }

  /** Spawn-scoped history of a trigger, so a respawn or reset allows triggering again. */
  static final class History implements Component {
    private static final String TRIGGERED = "triggered";
    private static final String PLAYERS = "players";

    private final TriggerRepetition repetition;
    // Names of the players that triggered, so the history survives reconnects and saves.
    private final Set<String> players = new LinkedHashSet<>();
    private boolean triggered;

    /**
     * Creates an empty history.
     *
     * @param repetition how often the trigger may be triggered
     */
    History(TriggerRepetition repetition) {
      this.repetition = repetition;
    }

    /**
     * Records that a player is about to trigger, if the repetition allows it.
     *
     * @param who triggering player
     * @return true if the trigger may raise its event
     */
    boolean tryTrigger(Entity who) {
      switch (repetition) {
        case ONCE_GLOBALLY -> {
          if (triggered) return false;
          triggered = true;
          return true;
        }
        case ONCE_PER_PLAYER -> {
          return players.add(playerName(who));
        }
        default -> {
          return true;
        }
      }
    }

    /**
     * Captures who already triggered, for a save of the game.
     *
     * @return the history, or empty if nobody triggered yet
     */
    Optional<JsonNode> capture() {
      if (!triggered && players.isEmpty()) return Optional.empty();
      ObjectNode state = PrefabStateJson.object().put(TRIGGERED, triggered);
      ArrayNode names = state.putArray(PLAYERS);
      players.forEach(names::add);
      return Optional.of(state);
    }

    /**
     * Restores a history captured by {@link #capture()}.
     *
     * @param saved captured history
     */
    void restore(JsonNode saved) {
      triggered = saved.path(TRIGGERED).asBoolean();
      saved.path(PLAYERS).forEach(name -> players.add(name.asString()));
    }

    private static String playerName(Entity who) {
      return who.fetch(PlayerComponent.class)
          .map(PlayerComponent::playerName)
          .orElse("entity-" + who.id());
    }
  }
}
