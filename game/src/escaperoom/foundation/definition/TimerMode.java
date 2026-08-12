package escaperoom.foundation.definition;

/** Foundation time-limit behavior. */
public enum TimerMode {
  /** Expiry terminates the shared session as failed. */
  HARD,
  /** Expiry marks overtime while the shared session continues. */
  SOFT
}
