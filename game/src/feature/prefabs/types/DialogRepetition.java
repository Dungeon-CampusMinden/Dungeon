package feature.prefabs.types;

import engine.Component;
import engine.Entity;
import feature.prefabs.PrefabProperty;
import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** How often a dialog or image prefab may show its dialog or image. */
enum DialogRepetition {
  REPEATABLE("Repeatable"),
  ONCE_PER_PLAYER("Once per player"),
  ONCE_GLOBALLY("Once globally");

  private final String label;

  DialogRepetition(String label) {
    this.label = label;
  }

  /**
   * Creates the shared selection property for choosing a dialog repetition mode.
   *
   * @return selection property storing the mode label
   */
  static PrefabProperty<String> property() {
    return PrefabProperty.selection(
        "repetition",
        "Repetition",
        REPEATABLE.label,
        Arrays.stream(values()).map(value -> value.label).toList());
  }

  /**
   * Resolves a mode from its serialized label.
   *
   * @param label serialized label
   * @return matching mode
   */
  static DialogRepetition fromLabel(String label) {
    for (DialogRepetition value : values()) {
      if (value.label.equals(label)) return value;
    }
    throw new IllegalArgumentException("Unknown dialog repetition: " + label);
  }

  /**
   * Spawn-scoped history of a prefab, shared by every way of showing its dialog or image, so the
   * repetition applies no matter whether a player entered the region or an action was run.
   */
  static final class History implements Component {
    private final DialogRepetition repetition;
    private final Set<Entity> players = Collections.newSetFromMap(new IdentityHashMap<>());
    private boolean shown;

    /**
     * Creates an empty history.
     *
     * @param repetition how often the dialog or image may be shown
     */
    History(DialogRepetition repetition) {
      this.repetition = repetition;
    }

    /**
     * Records that the dialog or image is about to be shown to a player, if the repetition allows
     * it.
     *
     * @param who player to show it to
     * @return true if it may be shown
     */
    boolean tryShow(Entity who) {
      switch (repetition) {
        case ONCE_GLOBALLY -> {
          if (shown) return false;
          shown = true;
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
