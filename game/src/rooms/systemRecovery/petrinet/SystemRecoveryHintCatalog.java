package rooms.systemRecovery.petrinet;

import feature.hints.Hint;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Builds the four localized hint stages attached to each player-facing progress place. */
public final class SystemRecoveryHintCatalog {

  private SystemRecoveryHintCatalog() {}

  /**
   * Creates the staged hints for a place.
   *
   * <p>The first two entries orient the player, the third gives the almost-complete instruction,
   * and the fourth is explicitly marked as the complete solution. The actual question and warning
   * are shown by the telephone, not stored in the Petri net.
   *
   * @param place active progress place
   * @return four localized hints in increasing disclosure order
   */
  public static Hint[] hints(SystemRecoveryProgressPlace place) {
    String topic = SystemRecoveryText.quest(place.riddleKey() + ".tab");
    String nearSolution =
        place.storyKey() == null
            ? SystemRecoveryText.text("hints.generic.near")
            : SystemRecoveryText.text("story." + place.storyKey());

    return new Hint[] {
      new Hint(
          SystemRecoveryText.text("hints.orientation-title"),
          SystemRecoveryText.text("hints.generic.orientation", topic)),
      new Hint(
          SystemRecoveryText.text("hints.approach-title"),
          SystemRecoveryText.text("hints.generic.approach", topic)),
      new Hint(SystemRecoveryText.text("hints.near-title"), nearSolution),
      new Hint(
          SystemRecoveryText.text("hints.solution-title"),
          SystemRecoveryText.text("hints.solution." + place.hintKey()),
          true)
    };
  }
}
