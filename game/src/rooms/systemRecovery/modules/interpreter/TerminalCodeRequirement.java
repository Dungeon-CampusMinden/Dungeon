package rooms.systemRecovery.modules.interpreter;

import java.util.Arrays;

/**
 * Defines the code required for one interpreter state and its callbacks.
 *
 * @param codeLines code lines that must all be present
 * @param onSuccess callback for a successful interpretation
 * @param onFailure callback for an unsuccessful interpretation
 */
public record TerminalCodeRequirement(
    CodeLine[] codeLines, Runnable onSuccess, Runnable onFailure) {

  /**
   * Creates an immutable terminal puzzle step.
   *
   * @param codeLines code lines that must all be present
   * @param onSuccess callback for a successful interpretation
   * @param onFailure callback for an unsuccessful interpretation
   */
  public TerminalCodeRequirement {
    codeLines = Arrays.copyOf(codeLines, codeLines.length);
    onSuccess = onSuccess == null ? () -> {} : onSuccess;
    onFailure = onFailure == null ? () -> {} : onFailure;
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
