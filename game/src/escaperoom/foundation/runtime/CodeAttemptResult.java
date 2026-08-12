package escaperoom.foundation.runtime;

import java.util.Objects;

/**
 * Stable numeric attempt result that never returns the authored answer.
 *
 * @param operation command classification and reason
 * @param outcome correct, incorrect, or not evaluated
 */
public record CodeAttemptResult(OperationResult operation, CodeOutcome outcome) {
  /** Creates a code-attempt result. */
  public CodeAttemptResult {
    Objects.requireNonNull(operation, "operation");
    Objects.requireNonNull(outcome, "outcome");
    if ((operation.status() == OperationStatus.APPLIED) == (outcome == CodeOutcome.NOT_EVALUATED)) {
      throw new IllegalArgumentException("applied code attempts must have a correctness outcome");
    }
  }
}
