package tracking.core;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable facts declared when the authoritative game server starts a session.
 *
 * @param schemaVersion tracking schema version
 * @param sessionId server-generated session identifier
 * @param roomId stable room identifier
 * @param startedAt UTC session start
 * @param runId stable playthrough identifier shared by resumed sessions
 * @param resumedAtActiveMs saved play-clock value when this session starts
 */
public record TrackingSessionDescriptor(
    int schemaVersion,
    UUID sessionId,
    String roomId,
    Instant startedAt,
    UUID runId,
    long resumedAtActiveMs) {
  /** Validates immutable session facts. */
  public TrackingSessionDescriptor {
    schemaVersion = TrackingChecks.schemaVersion(schemaVersion);
    sessionId = TrackingChecks.uuid(sessionId, "sessionId");
    roomId = TrackingChecks.text(roomId, "roomId");
    startedAt = TrackingChecks.utc(startedAt, "startedAt");
    runId = TrackingChecks.uuid(runId, "runId");
    resumedAtActiveMs = TrackingChecks.nonNegative(resumedAtActiveMs, "resumedAtActiveMs");
  }
}
