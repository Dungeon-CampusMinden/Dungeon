package foundation.runtime;

import java.util.Objects;
import java.util.Optional;

/**
 * Result of asking authority to release the next authored hint.
 *
 * @param operation command classification and reason
 * @param hint newly released hint only when the command was applied
 */
public record HintRevealResult(OperationResult operation, Optional<ReleasedHint> hint) {
  /** Creates an immutable hint result. */
  public HintRevealResult {
    Objects.requireNonNull(operation, "operation");
    hint = Objects.requireNonNull(hint, "hint");
    if ((operation.status() == OperationStatus.APPLIED) != hint.isPresent()) {
      throw new IllegalArgumentException("only applied hint commands contain a released hint");
    }
  }
}
