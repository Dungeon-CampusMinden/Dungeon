package tracking.core;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Terminal facts submitted once the game session ends.
 *
 * @param schemaVersion tracking schema version
 * @param sessionId finished session
 * @param finalSequence final event sequence, or zero when the session has no events
 * @param status completed or interrupted
 * @param endedAt UTC end instant
 * @param activeMs authoritative accumulated play time for the run
 * @param interruptedAtPuzzleId optional puzzle active when the session was interrupted
 */
public record TrackingSessionFinish(
    int schemaVersion,
    UUID sessionId,
    long finalSequence,
    TrackingSessionStatus status,
    Instant endedAt,
    long activeMs,
    Optional<String> interruptedAtPuzzleId) {
  /** Validates terminal status and time values. */
  public TrackingSessionFinish {
    schemaVersion = TrackingChecks.schemaVersion(schemaVersion);
    sessionId = TrackingChecks.uuid(sessionId, "sessionId");
    finalSequence = TrackingChecks.nonNegative(finalSequence, "finalSequence");
    status = Objects.requireNonNull(status, "status");
    endedAt = TrackingChecks.utc(endedAt, "endedAt");
    activeMs = TrackingChecks.nonNegative(activeMs, "activeMs");
    interruptedAtPuzzleId =
        Objects.requireNonNull(interruptedAtPuzzleId, "interruptedAtPuzzleId")
            .map(value -> TrackingChecks.text(value, "interruptedAtPuzzleId"));
    if (status != TrackingSessionStatus.INTERRUPTED && interruptedAtPuzzleId.isPresent()) {
      throw new IllegalArgumentException(
          "interruptedAtPuzzleId is only valid for interrupted sessions");
    }
  }
}
