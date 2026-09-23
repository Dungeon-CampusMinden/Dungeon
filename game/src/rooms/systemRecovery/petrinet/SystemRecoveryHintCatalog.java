package rooms.systemRecovery.petrinet;

import feature.hints.Hint;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Builds the four localized hint stages attached to each player-facing progress place. */
public final class SystemRecoveryHintCatalog {

  private static final String[] STAGES = {"orientation", "approach", "near", "solution"};

  private SystemRecoveryHintCatalog() {}

  /**
   * Creates the staged hints for a place.
   *
   * <p>All four entries are dedicated to this step; there is intentionally no generic fallback. The
   * actual question and warning are shown by the telephone, not stored in the Petri net.
   *
   * @param step active learning step
   * @return four localized hints in increasing disclosure order
   */
  public static Hint[] hints(SystemRecoveryLearningStep step) {
    if (step == null || !step.isLearningStep()) {
      throw new IllegalArgumentException("Only learning steps own hints.");
    }
    return new Hint[] {
      new Hint(
          SystemRecoveryText.key("hints.orientation-title"),
          SystemRecoveryText.key("hints.steps." + step.hintKey() + ".orientation")),
      new Hint(
          SystemRecoveryText.key("hints.approach-title"),
          SystemRecoveryText.key("hints.steps." + step.hintKey() + ".approach")),
      new Hint(
          SystemRecoveryText.key("hints.near-title"),
          SystemRecoveryText.key("hints.steps." + step.hintKey() + ".near")),
      new Hint(
          SystemRecoveryText.key("hints.solution-title"),
          SystemRecoveryText.key("hints.steps." + step.hintKey() + ".solution"),
          true)
    };
  }

  /**
   * Returns the stable tracking identifier for an exact hint offered by this step.
   *
   * @param step learning step owning the offer
   * @param acceptedHint hint accepted by the shared HintSystem
   * @return {@code <step>:<stage>} tracking ID
   * @throws IllegalArgumentException if the hint does not belong to the step
   */
  public static String hintId(SystemRecoveryLearningStep step, Hint acceptedHint) {
    if (step == null || !step.isLearningStep() || acceptedHint == null) {
      throw new IllegalArgumentException(
          "A hint ID requires a learning step and its accepted hint.");
    }

    Hint[] stepHints = hints(step);
    for (int index = 0; index < stepHints.length; index++) {
      if (stepHints[index].equals(acceptedHint)) {
        return step.hintKey() + ":" + STAGES[index];
      }
    }
    throw new IllegalArgumentException("The accepted hint does not belong to " + step.hintKey());
  }
}
