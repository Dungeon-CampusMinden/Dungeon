package foundation.runtime;

/** Stable non-secret reason accompanying an authority command result. */
public enum OperationReason {
  /** No exceptional reason applies. */
  NONE,
  /** The room capacity does not contain the slot. */
  UNKNOWN_SLOT,
  /** The slot is already connected. */
  ALREADY_CONNECTED,
  /** The slot is already disconnected. */
  ALREADY_DISCONNECTED,
  /** The slot must be connected for the operation. */
  SLOT_DISCONNECTED,
  /** Spawn readiness was already retained. */
  ALREADY_SPAWNED,
  /** The shared session has not reached running readiness. */
  SESSION_NOT_RUNNING,
  /** The shared session already has an immutable terminal result. */
  SESSION_TERMINAL,
  /** No riddle has the supplied identifier. */
  UNKNOWN_RIDDLE,
  /** The riddle is not active yet. */
  RIDDLE_LOCKED,
  /** No input has the supplied identifier in the riddle. */
  UNKNOWN_INPUT,
  /** No information source has the supplied identifier in the riddle. */
  UNKNOWN_INFORMATION_SOURCE,
  /** The input was already satisfied. */
  INPUT_ALREADY_SATISFIED,
  /** The readable source has no associated collection input. */
  INFORMATION_SOURCE_ONLY,
  /** The riddle was already completed. */
  RIDDLE_ALREADY_COMPLETED,
  /** Every authored hint has already been released. */
  HINTS_EXHAUSTED,
  /** The common exit door is still closed. */
  DOOR_CLOSED,
  /** The roster slot is already present at the common exit. */
  ALREADY_IN_EXIT,
  /** The roster slot is not present at the common exit. */
  NOT_IN_EXIT,
  /** A zero duration produced no timer change. */
  ZERO_DURATION,
  /** A negative duration cannot advance authority time. */
  NEGATIVE_DURATION
}
