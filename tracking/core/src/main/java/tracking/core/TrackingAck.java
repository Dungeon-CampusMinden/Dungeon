package tracking.core;

import java.util.UUID;

/**
 * Backend acknowledgement for an idempotent upload.
 *
 * @param schemaVersion tracking schema version
 * @param sessionId acknowledged session
 * @param lastPersistedSequence highest stored sequence, or zero when no event exists
 * @param acceptedEventCount events newly inserted by this request
 */
public record TrackingAck(
    int schemaVersion, UUID sessionId, long lastPersistedSequence, int acceptedEventCount) {
  /** Validates acknowledgement values. */
  public TrackingAck {
    schemaVersion = TrackingChecks.schemaVersion(schemaVersion);
    sessionId = TrackingChecks.uuid(sessionId, "sessionId");
    lastPersistedSequence =
        TrackingChecks.nonNegative(lastPersistedSequence, "lastPersistedSequence");
    if (acceptedEventCount < 0) {
      throw new IllegalArgumentException("acceptedEventCount must not be negative");
    }
  }
}
