package feature.prefabs.types;

import engine.Component;
import engine.Entity;
import feature.prefabs.PrefabProperty;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

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
    private final TriggerRepetition repetition;
    private final Set<Entity> players = Collections.newSetFromMap(new IdentityHashMap<>());
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
          return players.add(who);
        }
        default -> {
          return true;
        }
      }
    }
  }
}
