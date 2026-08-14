package escaperoom.foundation.runtime;

import java.util.Objects;

/**
 * Stable non-secret result of one Foundation authority command.
 *
 * @param status applied, idempotent, or rejected classification
 * @param reason stable reason without authored secrets
 */
public record OperationResult(OperationStatus status, OperationReason reason) {
  /** Creates a closed operation result. */
  public OperationResult {
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(reason, "reason");
    if ((status == OperationStatus.APPLIED) != (reason == OperationReason.NONE)) {
      throw new IllegalArgumentException("only applied operations use the NONE reason");
    }
  }

  /**
   * Creates a successfully applied result.
   *
   * @return applied result
   */
  public static OperationResult applied() {
    return new OperationResult(OperationStatus.APPLIED, OperationReason.NONE);
  }

  /**
   * Creates an idempotent result.
   *
   * @param reason stable idempotence reason
   * @return idempotent result
   */
  public static OperationResult idempotent(final OperationReason reason) {
    return new OperationResult(OperationStatus.IDEMPOTENT, reason);
  }

  /**
   * Creates a rejected result.
   *
   * @param reason stable rejection reason
   * @return rejected result
   */
  public static OperationResult rejected(final OperationReason reason) {
    return new OperationResult(OperationStatus.REJECTED, reason);
  }
}
