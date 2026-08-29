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
 * @param status completed or aborted
 * @param endedAt UTC end instant
 * @param elapsedMonotonicMs authoritative total duration
 * @param abortedAtPuzzleId optional puzzle active when the session was aborted
 */
public record TrackingSessionFinish(
    int schemaVersion,
    UUID sessionId,
    long finalSequence,
    TrackingSessionStatus status,
    Instant endedAt,
    long elapsedMonotonicMs,
    Optional<String> abortedAtPuzzleId) {
  /** Validates terminal status and time values. */
  public TrackingSessionFinish {
    schemaVersion = TrackingChecks.schemaVersion(schemaVersion);
    sessionId = TrackingChecks.uuid(sessionId, "sessionId");
    finalSequence = TrackingChecks.nonNegative(finalSequence, "finalSequence");
    status = Objects.requireNonNull(status, "status");
    endedAt = TrackingChecks.utc(endedAt, "endedAt");
    elapsedMonotonicMs = TrackingChecks.nonNegative(elapsedMonotonicMs, "elapsedMonotonicMs");
    abortedAtPuzzleId =
        Objects.requireNonNull(abortedAtPuzzleId, "abortedAtPuzzleId")
            .map(value -> TrackingChecks.text(value, "abortedAtPuzzleId"));
    if (status != TrackingSessionStatus.ABORTED && abortedAtPuzzleId.isPresent()) {
      throw new IllegalArgumentException("abortedAtPuzzleId is only valid for aborted sessions");
    }
  }
}
