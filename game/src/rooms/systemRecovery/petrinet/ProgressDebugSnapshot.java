package rooms.systemRecovery.petrinet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import rooms.systemRecovery.util.SystemRecoveryText;

/**
 * Immutable, translation-key-only inspection of the authoritative progress marking.
 *
 * @param activeStepKey stable key of the active step, or an empty string when inconsistent
 * @param interpreterStep shared terminal interpreter state, or {@code -1} if unavailable
 * @param tokenCounts token counts keyed by stable step identifiers
 * @param totalTokens total number of tokens across all progress places
 * @param hintIndex number of accepted hints for the active step
 * @param hintCount number of available hints for the active step
 * @param lastAcceptedStepKey most recently accepted completion key, or empty
 * @param lastRejectedStepKey most recently rejected completion key, or empty
 * @param rejectionReasonKey stable key describing the most recent rejection, or empty
 * @param steps ordered status rows for every learning step
 * @param consistent whether the marking satisfies the single-token invariant
 * @param inputWeight required completion input-arc weight
 * @param outputWeight completion output-arc weight
 */
public record ProgressDebugSnapshot(
    String activeStepKey,
    int interpreterStep,
    Map<String, Integer> tokenCounts,
    int totalTokens,
    int hintIndex,
    int hintCount,
    String lastAcceptedStepKey,
    String lastRejectedStepKey,
    String rejectionReasonKey,
    List<StepStatus> steps,
    boolean consistent,
    int inputWeight,
    int outputWeight) {

  private static final int COMPLETION_INPUT_WEIGHT = 2;
  private static final int COMPLETION_OUTPUT_WEIGHT = 1;

  /** Defensively freezes all collections so a network response cannot drift after capture. */
  public ProgressDebugSnapshot {
    tokenCounts = Collections.unmodifiableMap(new LinkedHashMap<>(tokenCounts));
    steps = List.copyOf(steps);
  }

  /**
   * Builds status rows from a complete or partial marking. Missing places are treated as empty.
   *
   * @param marking token counts keyed by learning step
   * @param interpreterStep active shared-interpreter state ID, or {@code -1} when unavailable
   * @param hintIndex accepted hints for the active place
   * @param hintCount available hints for the active place
   * @param lastAcceptedStepKey most recently accepted step key, if any
   * @param lastRejectedStepKey most recently rejected request key, if any
   * @param rejectionReasonKey stable diagnostic reason key, if any
   * @return immutable diagnostic snapshot
   */
  public static ProgressDebugSnapshot fromMarking(
      Map<SystemRecoveryLearningStep, Integer> marking,
      int interpreterStep,
      int hintIndex,
      int hintCount,
      String lastAcceptedStepKey,
      String lastRejectedStepKey,
      String rejectionReasonKey) {
    EnumMap<SystemRecoveryLearningStep, Integer> counts =
        new EnumMap<>(SystemRecoveryLearningStep.class);
    int totalTokens = 0;
    boolean validCounts = true;
    for (SystemRecoveryLearningStep step : SystemRecoveryLearningStep.values()) {
      int count = marking.getOrDefault(step, 0);
      counts.put(step, count);
      totalTokens += count;
      validCounts &= count >= 0 && count <= 1;
    }

    boolean consistent = validCounts && totalTokens == 1;
    int activeIndex = -1;
    if (consistent) {
      for (SystemRecoveryLearningStep step : SystemRecoveryLearningStep.values()) {
        if (counts.get(step) == 1) {
          activeIndex = step.ordinal();
          break;
        }
      }
    }

    String activeKey =
        activeIndex < 0 ? "" : SystemRecoveryLearningStep.values()[activeIndex].hintKey();
    Map<String, Integer> keyedCounts = new LinkedHashMap<>();
    List<StepStatus> statuses = new ArrayList<>();
    for (SystemRecoveryLearningStep step : SystemRecoveryLearningStep.values()) {
      int count = counts.get(step);
      String status = statusFor(step, count, activeIndex, consistent);
      keyedCounts.put(step.hintKey(), count);
      statuses.add(new StepStatus(step.hintKey(), status, count));
    }

    return new ProgressDebugSnapshot(
        activeKey,
        interpreterStep,
        keyedCounts,
        totalTokens,
        hintIndex,
        hintCount,
        valueOrEmpty(lastAcceptedStepKey),
        valueOrEmpty(lastRejectedStepKey),
        valueOrEmpty(rejectionReasonKey),
        statuses,
        consistent,
        COMPLETION_INPUT_WEIGHT,
        COMPLETION_OUTPUT_WEIGHT);
  }

  /**
   * Formats this compact snapshot as a reliable, localized debug-dialog payload.
   *
   * @return translation keys with primitive arguments; localization occurs on the receiving client
   */
  public String dialogPayload() {
    String activeLabel =
        activeStepKey.isEmpty()
            ? SystemRecoveryText.key("computer.debug-petri-active-none")
            : stepLabelKey(activeStepKey);
    String consistencyLabel =
        SystemRecoveryText.key(
            consistent ? "computer.debug-petri-consistent" : "computer.debug-petri-inconsistent");
    StringBuilder payload = new StringBuilder();
    payload.append(SystemRecoveryText.key("computer.debug-petri-net-header"));
    payload
        .append("\n\n")
        .append(
            SystemRecoveryText.key(
                "computer.debug-petri-status",
                activeLabel,
                interpreterStep,
                totalTokens,
                hintIndex,
                hintCount,
                consistencyLabel));
    payload
        .append("\n\n")
        .append(
            SystemRecoveryText.key(
                "computer.debug-petri-diagnostics",
                labelOrNone(lastAcceptedStepKey),
                labelOrNone(lastRejectedStepKey),
                reasonLabelOrNone(rejectionReasonKey)));
    payload.append("\n\n").append(SystemRecoveryText.key("computer.debug-petri-steps"));
    for (StepStatus step : steps) {
      String hintProgress = step.statusKey().equals("active") ? hintIndex + "/" + hintCount : "";
      payload
          .append("\n")
          .append(
              SystemRecoveryText.key(
                  "computer.debug-petri-row",
                  SystemRecoveryText.key("computer.debug-petri-state." + step.statusKey()),
                  stepLabelKey(step.stepKey()),
                  step.tokenCount(),
                  hintProgress));
    }
    payload
        .append("\n\n")
        .append(
            SystemRecoveryText.key(
                "computer.debug-petri-technical",
                totalTokens,
                inputWeight,
                outputWeight,
                steps.size()));
    return payload.toString();
  }

  private static String statusFor(
      SystemRecoveryLearningStep step, int count, int activeIndex, boolean consistent) {
    if (!consistent && count != 0) return "invalid";
    if (!consistent) return "locked";
    if (step.ordinal() < activeIndex) return "completed";
    if (step.ordinal() == activeIndex) return "active";
    return "locked";
  }

  private static String stepLabelKey(String stepKey) {
    return SystemRecoveryText.key("computer.debug-petri-step." + stepKey);
  }

  private static String labelOrNone(String stepKey) {
    return stepKey.isEmpty()
        ? SystemRecoveryText.key("computer.debug-petri-active-none")
        : stepLabelKey(stepKey);
  }

  private static String reasonLabelOrNone(String reasonKey) {
    return reasonKey.isEmpty()
        ? SystemRecoveryText.key("computer.debug-petri-active-none")
        : SystemRecoveryText.key("computer.debug-petri-reason." + reasonKey);
  }

  private static String valueOrEmpty(String value) {
    return value == null ? "" : value;
  }

  /**
   * One stable row in the ordered learning-place list.
   *
   * @param stepKey stable identifier for the learning step
   * @param statusKey stable status identifier such as active or completed
   * @param tokenCount current token count for that place
   */
  public record StepStatus(String stepKey, String statusKey, int tokenCount) {}
}
