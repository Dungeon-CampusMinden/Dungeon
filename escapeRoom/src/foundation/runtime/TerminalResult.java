package foundation.runtime;

/** Immutable terminal outcome of one Foundation authority session. */
public enum TerminalResult {
  /** Every currently ready player reached the common exit. */
  SUCCESS,
  /** The hard authoritative deadline was processed. */
  HARD_TIMEOUT,
  /** Authority was explicitly aborted. */
  ABORTED
}
