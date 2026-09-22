package tracking.core;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Immutable facts declared when the authoritative game server starts a session.
 *
 * @param schemaVersion tracking schema version
 * @param sessionId server-generated session identifier
 * @param roomId stable room identifier
 * @param startedAt UTC session start
 * @param runId optional stable playthrough identifier shared by resumed sessions
 */
public record TrackingSessionDescriptor(
    int schemaVersion,
    UUID sessionId,
    String roomId,
    Instant startedAt,
    Optional<UUID> runId) {
  /** Creates a descriptor without a playthrough identifier. */
  public TrackingSessionDescriptor(
      int schemaVersion, UUID sessionId, String roomId, Instant startedAt) {
    this(schemaVersion, sessionId, roomId, startedAt, Optional.empty());
  }

  /** Validates immutable session facts. */
  public TrackingSessionDescriptor {
    schemaVersion = TrackingChecks.schemaVersion(schemaVersion);
    sessionId = TrackingChecks.uuid(sessionId, "sessionId");
    roomId = TrackingChecks.text(roomId, "roomId");
    startedAt = TrackingChecks.utc(startedAt, "startedAt");
    runId = Objects.requireNonNull(runId, "runId");
  }
}
