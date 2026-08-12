package foundation.runtime;

/** Stable classification of one Foundation authority command. */
public enum OperationStatus {
  /** The command was accepted and changed authoritative state or processed an attempt. */
  APPLIED,
  /** The requested outcome was already present and no state changed. */
  IDEMPOTENT,
  /** The command was invalid for the current authority state. */
  REJECTED
}
