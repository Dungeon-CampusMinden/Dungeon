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
    if (!events.isEmpty()) {
      if (events.getFirst().eventType() != TrackingEventType.SESSION_STARTED) {
        throw new IllegalArgumentException("Outbox events must start with SESSION_STARTED");
      }
      if (events.getFirst().occurredAt().isBefore(session.startedAt())) {
        throw new IllegalArgumentException("SESSION_STARTED event must not precede its descriptor");
      }
    }
    long expectedSequence = 1;
    long previousElapsedMs = -1;
    for (TrackingEvent event : events) {
      if (!event.sessionId().equals(session.sessionId())
          || !event.roomId().equals(session.roomId())
          || event.schemaVersion() != session.schemaVersion()) {
        throw new IllegalArgumentException("Outbox event does not match its session descriptor");
      }
      if (event.sessionSequence() != expectedSequence) {
        throw new IllegalArgumentException("Outbox event sequence must start at one without gaps");
      }
      if (expectedSequence > 1 && event.eventType() == TrackingEventType.SESSION_STARTED) {
        throw new IllegalArgumentException("Outbox must contain only one SESSION_STARTED event");
      }
      if (event.elapsedMonotonicMs() < previousElapsedMs) {
        throw new IllegalArgumentException("Outbox event elapsed time must not regress");
      }
      previousElapsedMs = event.elapsedMonotonicMs();
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
      if (value.elapsedMonotonicMs() < previousElapsedMs) {
        throw new IllegalArgumentException("Outbox finish elapsed time precedes its final event");
      }
    }
  }
}
