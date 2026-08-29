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
 */
public record TrackingSessionDescriptor(
    int schemaVersion, UUID sessionId, String roomId, Instant startedAt) {
  /** Validates immutable session facts. */
  public TrackingSessionDescriptor {
    schemaVersion = TrackingChecks.schemaVersion(schemaVersion);
    sessionId = TrackingChecks.uuid(sessionId, "sessionId");
    roomId = TrackingChecks.text(roomId, "roomId");
    startedAt = TrackingChecks.utc(startedAt, "startedAt");
  }
}
