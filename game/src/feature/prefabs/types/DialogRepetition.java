package feature.prefabs.types;

import feature.prefabs.PrefabProperty;
import java.util.Arrays;

/** How often a dialog prefab may show its dialog. */
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
}
