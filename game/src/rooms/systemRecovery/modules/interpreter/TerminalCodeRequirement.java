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
 */
public record TerminalCodeRequirement(
    CodeLine[] codeLines,
    boolean requiresOrder,
    Consumer<TerminalAttempt> onSuccess,
    Consumer<TerminalAttempt> onFailure) {

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
        });
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
