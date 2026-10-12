package tracking.core;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The complete records recovered from one self-contained tracking outbox.
 *
 * @param session persisted session descriptor
 * @param events complete ordered event records
 * @param finish optional terminal record
 * @param truncatedTailIgnored whether an incomplete final record was ignored
 */
public record TrackingJsonlReadResult(
    TrackingSessionDescriptor session,
    List<TrackingEvent> events,
    Optional<TrackingSessionFinish> finish,
    boolean truncatedTailIgnored) {
  /** Copies collections and validates the session identity and order. */
  public TrackingJsonlReadResult {
    session = Objects.requireNonNull(session, "session");
    events = List.copyOf(Objects.requireNonNull(events, "events"));
    finish = Objects.requireNonNull(finish, "finish");
    if (!events.isEmpty() && events.getFirst().occurredAt().isBefore(session.startedAt())) {
      throw new IllegalArgumentException("First outbox event must not precede its session");
    }
    long expectedSequence = 1;
    long previousActiveMs = session.resumedAtActiveMs();
    for (TrackingEvent event : events) {
      if (!event.sessionId().equals(session.sessionId())
          || !event.roomId().equals(session.roomId())
          || event.schemaVersion() != session.schemaVersion()) {
        throw new IllegalArgumentException("Outbox event does not match its session descriptor");
      }
      if (event.sessionSequence() != expectedSequence) {
        throw new IllegalArgumentException("Outbox event sequence must start at one without gaps");
      }
      if (event.activeMs() < previousActiveMs) {
        throw new IllegalArgumentException("Outbox event active time must not regress");
      }
      previousActiveMs = event.activeMs();
      expectedSequence++;
    }
    if (finish.isPresent()) {
      TrackingSessionFinish value = finish.orElseThrow();
      if (!value.sessionId().equals(session.sessionId())
          || value.schemaVersion() != session.schemaVersion()
          || value.finalSequence() != events.size()) {
        throw new IllegalArgumentException("Outbox finish does not match its complete events");
      }
      if (value.endedAt().isBefore(session.startedAt())) {
        throw new IllegalArgumentException("Outbox finish must not precede its session");
      }
      if (value.activeMs() < previousActiveMs) {
        throw new IllegalArgumentException("Outbox finish active time precedes its final event");
      }
    }
  }
}
