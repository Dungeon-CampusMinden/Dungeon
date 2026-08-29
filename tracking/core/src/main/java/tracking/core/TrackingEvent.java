package tracking.core;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * One append-only, server-authoritative event in a session.
 *
 * @param schemaVersion tracking schema version
 * @param sessionId owning session
 * @param sessionSequence gap-free server sequence starting at one
 * @param eventId deterministic {@code sessionId:sessionSequence} identifier
 * @param participantId optional session-scoped participant
 * @param roomId stable room identifier
 * @param eventType precise tracking event type
 * @param puzzleId optional stable puzzle identifier
 * @param objectId optional stable object identifier
 * @param outcome optional attempt result
 * @param elapsedMonotonicMs monotonic milliseconds since the session began
 * @param occurredAt UTC wall-clock instant
 * @param payload structured event-specific data, including full submitted answers
 */
public record TrackingEvent(
    int schemaVersion,
    UUID sessionId,
    long sessionSequence,
    String eventId,
    Optional<UUID> participantId,
    String roomId,
    TrackingEventType eventType,
    Optional<String> puzzleId,
    Optional<String> objectId,
    Optional<TrackingOutcome> outcome,
    long elapsedMonotonicMs,
    Instant occurredAt,
    JsonNode payload) {
  /** Validates event identity, time, and structured data. */
  public TrackingEvent {
    schemaVersion = TrackingChecks.schemaVersion(schemaVersion);
    sessionId = TrackingChecks.uuid(sessionId, "sessionId");
    sessionSequence = TrackingChecks.positive(sessionSequence, "sessionSequence");
    eventId = TrackingChecks.text(eventId, "eventId");
    if (!eventId.equals(eventId(sessionId, sessionSequence))) {
      throw new IllegalArgumentException(
          "eventId must be derived from sessionId and sessionSequence");
    }
    participantId = Objects.requireNonNull(participantId, "participantId");
    roomId = TrackingChecks.text(roomId, "roomId");
    eventType = Objects.requireNonNull(eventType, "eventType");
    puzzleId = optionalText(puzzleId, "puzzleId");
    objectId = optionalText(objectId, "objectId");
    outcome = Objects.requireNonNull(outcome, "outcome");
    elapsedMonotonicMs = TrackingChecks.nonNegative(elapsedMonotonicMs, "elapsedMonotonicMs");
    occurredAt = TrackingChecks.utc(occurredAt, "occurredAt");
    payload = TrackingChecks.object(payload, "payload");
    if ((eventType == TrackingEventType.ANSWER_SUBMITTED) != outcome.isPresent()) {
      throw new IllegalArgumentException("outcome is required only for ANSWER_SUBMITTED");
    }
    boolean puzzleEvent =
        switch (eventType) {
          case PUZZLE_STARTED, ANSWER_SUBMITTED, HINT_USED, PUZZLE_SOLVED -> true;
          default -> false;
        };
    if (puzzleEvent != puzzleId.isPresent()) {
      throw new IllegalArgumentException("puzzleId is required only for puzzle events");
    }
    boolean participantEvent =
        switch (eventType) {
          case PARTICIPANT_JOINED, PARTICIPANT_LEFT, ANSWER_SUBMITTED, HINT_USED -> true;
          default -> false;
        };
    if (participantEvent != participantId.isPresent()) {
      throw new IllegalArgumentException("participantId is required only for participant events");
    }
    if (eventType == TrackingEventType.ANSWER_SUBMITTED
        && (payload.get("answer") == null || payload.get("answer").isNull())) {
      throw new IllegalArgumentException("ANSWER_SUBMITTED payload must contain the full answer");
    }
    if (eventType == TrackingEventType.ANSWER_SUBMITTED) {
      JsonNode answerKind = payload.get("answerKind");
      JsonNode attemptNumber = payload.get("attemptNumber");
      if (objectId.isEmpty()) {
        throw new IllegalArgumentException("ANSWER_SUBMITTED requires objectId");
      }
      if (answerKind == null || !answerKind.isString() || answerKind.stringValue().isBlank()) {
        throw new IllegalArgumentException("ANSWER_SUBMITTED requires a non-blank answerKind");
      }
      if (attemptNumber == null
          || !attemptNumber.isIntegralNumber()
          || !attemptNumber.canConvertToInt()
          || attemptNumber.intValue() < 1) {
        throw new IllegalArgumentException("ANSWER_SUBMITTED requires a positive attemptNumber");
      }
    }
    if (eventType == TrackingEventType.HINT_USED && objectId.isEmpty()) {
      throw new IllegalArgumentException("HINT_USED requires the hint ID as objectId");
    }
  }

  /**
   * Returns the only valid event ID for a session sequence.
   *
   * @param sessionId owning session
   * @param sessionSequence positive session sequence
   * @return deterministic event ID
   */
  public static String eventId(final UUID sessionId, final long sessionSequence) {
    TrackingChecks.uuid(sessionId, "sessionId");
    TrackingChecks.positive(sessionSequence, "sessionSequence");
    return sessionId + ":" + sessionSequence;
  }

  @Override
  public JsonNode payload() {
    return payload.deepCopy();
  }

  private static Optional<String> optionalText(final Optional<String> value, final String name) {
    return Objects.requireNonNull(value, name).map(item -> TrackingChecks.text(item, name));
  }
}
