package tracking.core;

/** Terminal state of a tracking session. */
public enum TrackingSessionStatus {
  /** The room was solved. */
  COMPLETED,
  /** The room was lost for good, for example by a hard time limit. */
  FAILED,
  INTERRUPTED
}
