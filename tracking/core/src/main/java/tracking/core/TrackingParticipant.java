package tracking.core;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * A participant identity that is valid only inside one session.
 *
 * @param sessionId owning session
 * @param participantId identifier generated for this session only
 * @param roomPlayedBefore resettable client-local prior-play flag
 * @param joinedAt UTC join instant
 * @param leftAt optional UTC leave instant
 */
public record TrackingParticipant(
    UUID sessionId,
    UUID participantId,
    boolean roomPlayedBefore,
    Instant joinedAt,
    Optional<Instant> leftAt) {
  /** Validates participant identity and lifecycle times. */
  public TrackingParticipant {
    sessionId = TrackingChecks.uuid(sessionId, "sessionId");
    participantId = TrackingChecks.uuid(participantId, "participantId");
    joinedAt = TrackingChecks.utc(joinedAt, "joinedAt");
    leftAt = java.util.Objects.requireNonNull(leftAt, "leftAt");
    if (leftAt.isPresent() && leftAt.orElseThrow().isBefore(joinedAt)) {
      throw new IllegalArgumentException("leftAt must not be before joinedAt");
    }
  }
}
