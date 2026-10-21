package escaperoom.foundation.runtime;

import java.util.Objects;

/**
 * Stable input attempt result that never returns the authored answer.
 *
 * @param operation command classification and reason
 * @param outcome correct, incorrect, or not evaluated
 */
public record AttemptResult(OperationResult operation, AttemptOutcome outcome) {
  /** Creates an attempt result. */
  public AttemptResult {
    Objects.requireNonNull(operation, "operation");
    Objects.requireNonNull(outcome, "outcome");
    if ((operation.status() == OperationStatus.APPLIED)
        == (outcome == AttemptOutcome.NOT_EVALUATED)) {
      throw new IllegalArgumentException("applied attempts must have a correctness outcome");
    }
  }
}
