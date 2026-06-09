package core.network;

/** Reason why the server sent a reliable full snapshot to a specific client. */
public enum FullSnapshotSendReason {
  /** The server has not received any snapshot acknowledgement from the client yet. */
  NO_ACK,

  /** The configured periodic baseline interval elapsed for the client. */
  PERIODIC_BASELINE,

  /** The client's acknowledged delta baseline is no longer retained in server history. */
  MISSING_BASELINE_HISTORY,

  /** The current level changed and existing snapshot baselines were invalidated. */
  LEVEL_CHANGE,

  /** The client reconnected and needs a fresh snapshot baseline. */
  RECONNECT,

  /** The client explicitly requested a snapshot resynchronization. */
  CLIENT_RESYNC_REQUEST,

  /** The server explicitly forced a snapshot resynchronization. */
  SERVER_FORCED_RESYNC
}
