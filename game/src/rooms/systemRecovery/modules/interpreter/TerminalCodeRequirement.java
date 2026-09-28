package rooms.systemRecovery.modules.interpreter;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Defines the code required for one interpreter state and its callbacks.
 *
 * @param codeLines code lines that must all be present
 * @param requiresOrder whether the code lines must appear in the registered order
 * @param onSuccess callback for a successful interpretation
 * @param onFailure callback for an unsuccessful interpretation
 * @param acceptsPartialInput whether correct subsets may be accumulated across submissions
 * @param acceptsFollowingStepInSameSubmission whether this submission may complete the next step
 * @param onPartialInput callback for an accepted but incomplete submission
 */
public record TerminalCodeRequirement(
    CodeLine[] codeLines,
    boolean requiresOrder,
    Consumer<TerminalAttempt> onSuccess,
    Consumer<TerminalAttempt> onFailure,
    boolean acceptsPartialInput,
    boolean acceptsFollowingStepInSameSubmission,
    Consumer<TerminalAttempt> onPartialInput) {

  /**
   * Creates the standard all-or-nothing requirement for one terminal step.
   *
   * @param codeLines code lines that must all be present
   * @param requiresOrder whether the code lines must appear in the registered order
   * @param onSuccess callback for a successful interpretation
   * @param onFailure callback for an unsuccessful interpretation
   */
  public TerminalCodeRequirement(
      CodeLine[] codeLines,
      boolean requiresOrder,
      Consumer<TerminalAttempt> onSuccess,
      Consumer<TerminalAttempt> onFailure) {
    this(codeLines, requiresOrder, onSuccess, onFailure, false, false, null);
  }

  /**
   * Creates an unordered terminal puzzle step.
   *
   * @param codeLines code lines that must all be present
   * @param onSuccess callback for a successful interpretation
   * @param onFailure callback for an unsuccessful interpretation
   */
  public TerminalCodeRequirement(CodeLine[] codeLines, Runnable onSuccess, Runnable onFailure) {
    this(
        codeLines,
        false,
        ignored -> {
          if (onSuccess != null) onSuccess.run();
        },
        ignored -> {
          if (onFailure != null) onFailure.run();
        },
        false,
        false,
        null);
  }

  /**
   * Creates an immutable terminal puzzle step.
   *
   * @param codeLines code lines that must all be present
   * @param requiresOrder whether the code lines must appear in the registered order
   * @param onSuccess callback for a successful interpretation
   * @param onFailure callback for an unsuccessful interpretation
   */
  public TerminalCodeRequirement {
    codeLines = Arrays.copyOf(codeLines, codeLines.length);
    onSuccess = onSuccess == null ? ignored -> {} : onSuccess;
    onFailure = onFailure == null ? ignored -> {} : onFailure;
    onPartialInput = onPartialInput == null ? ignored -> {} : onPartialInput;
  }

  /**
   * Returns a copy that accumulates matched code lines across multiple submissions.
   *
   * @param callback callback for a valid but incomplete submission
   * @return configured requirement
   */
  public TerminalCodeRequirement withPartialSubmissions(
      Consumer<TerminalAttempt> callback) {
    return new TerminalCodeRequirement(
        codeLines,
        requiresOrder,
        onSuccess,
        onFailure,
        true,
        acceptsFollowingStepInSameSubmission,
        callback);
  }

  /**
   * Returns a copy that may also accept statements belonging to the immediately following step.
   *
   * @return configured requirement
   */
  public TerminalCodeRequirement allowingFollowingStepInSameSubmission() {
    return new TerminalCodeRequirement(
        codeLines,
        requiresOrder,
        onSuccess,
        onFailure,
        acceptsPartialInput,
        true,
        onPartialInput);
  }

  /**
   * Returns a copy of the required code lines.
   *
   * @return registered code lines
   */
  @Override
  public CodeLine[] codeLines() {
    return Arrays.copyOf(codeLines, codeLines.length);
  }
}
