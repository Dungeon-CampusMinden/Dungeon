package tracking.core;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Idempotently uploadable session facts and a contiguous event range.
 *
 * @param schemaVersion tracking schema version
 * @param session immutable session descriptor
 * @param participants current session-scoped participants
 * @param events contiguous ascending event range, or an empty list
 */
public record TrackingBatch(
    int schemaVersion,
    TrackingSessionDescriptor session,
    List<TrackingParticipant> participants,
    List<TrackingEvent> events) {
  /** Validates that every batch item belongs to the declared session. */
  public TrackingBatch {
    schemaVersion = TrackingChecks.schemaVersion(schemaVersion);
    session = Objects.requireNonNull(session, "session");
    participants = List.copyOf(Objects.requireNonNull(participants, "participants"));
    events = List.copyOf(Objects.requireNonNull(events, "events"));
    if (schemaVersion != session.schemaVersion()) {
      throw new IllegalArgumentException("batch and session schemaVersion must match");
    }
    Set<UUID> participantIds = validateParticipants(session.sessionId(), participants);
    validateEvents(session, participantIds, events);
  }

  private static Set<UUID> validateParticipants(
      final UUID sessionId, final List<TrackingParticipant> participants) {
    Set<UUID> ids = new HashSet<>();
    for (TrackingParticipant participant : participants) {
      if (!participant.sessionId().equals(sessionId)) {
        throw new IllegalArgumentException("participant belongs to a different session");
      }
      if (!ids.add(participant.participantId())) {
        throw new IllegalArgumentException("participant IDs must be unique in a batch");
      }
    }
    return ids;
  }

  private static void validateEvents(
      final TrackingSessionDescriptor session,
      final Set<UUID> participantIds,
      final List<TrackingEvent> events) {
    long previous = -1;
    for (TrackingEvent event : events) {
      if (!event.sessionId().equals(session.sessionId())
          || !event.roomId().equals(session.roomId())
          || event.schemaVersion() != session.schemaVersion()) {
        throw new IllegalArgumentException("event does not match its session descriptor");
      }
      if (event.participantId().filter(id -> !participantIds.contains(id)).isPresent()) {
        throw new IllegalArgumentException("event participant must be included in the batch");
      }
      if (previous != -1 && event.sessionSequence() != previous + 1) {
        throw new IllegalArgumentException("events must be a contiguous ascending sequence");
      }
      previous = event.sessionSequence();
    }
  }
}
