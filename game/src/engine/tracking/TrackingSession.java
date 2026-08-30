package engine.tracking;

import engine.utils.logging.DungeonLogger;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import tracking.core.TrackingBatch;
import tracking.core.TrackingEvent;
import tracking.core.TrackingEventType;
import tracking.core.TrackingJson;
import tracking.core.TrackingOutcome;
import tracking.core.TrackingParticipant;
import tracking.core.TrackingSessionDescriptor;
import tracking.core.TrackingSessionFinish;
import tracking.core.TrackingSessionStatus;

/** Mutable state of one authoritative tracking session. Guarded by the facade lock. */
final class TrackingSession {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(TrackingSession.class);
  private static final int SCHEMA_VERSION = 1;
  private static final Duration FINAL_UPLOAD_TIMEOUT = Duration.ofMillis(2500);

  private final TrackingConfig config;
  private final TrackingSessionDescriptor descriptor;
  private final long startedNanos = System.nanoTime();
  private final TrackingOutbox outbox;
  private final TrackingUploader uploader;
  private final Map<Short, ParticipantState> participantsByClient = new LinkedHashMap<>();
  private final Map<Integer, UUID> participantsByEntity = new HashMap<>();
  private final Set<String> startedPuzzles = new HashSet<>();
  private final Set<String> solvedPuzzles = new HashSet<>();
  private final Set<String> usedHints = new HashSet<>();
  private final Set<String> activePuzzles = new LinkedHashSet<>();
  private final Map<String, Integer> attemptsByPuzzle = new HashMap<>();

  private long sequence;
  private boolean finished;

  TrackingSession(TrackingConfig config) {
    this.config = config;
    UUID sessionId = UUID.randomUUID();
    this.descriptor =
        new TrackingSessionDescriptor(SCHEMA_VERSION, sessionId, config.roomId(), Instant.now());
    this.outbox = TrackingOutbox.create(config.outboxDirectory(), descriptor);
    this.uploader =
        config.endpoint().map(endpoint -> new TrackingUploader(config, descriptor)).orElse(null);
    event(
        TrackingEventType.SESSION_STARTED,
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        TrackingJson.object());
    LOGGER.info("Tracking session {} writes to {}", sessionId, outbox.path());
  }

  String roomId() {
    return descriptor.roomId();
  }

  Path outboxPath() {
    return outbox.path();
  }

  Optional<String> operatorContact() {
    return config.operatorContact();
  }

  boolean finished() {
    return finished;
  }

  Optional<String> currentPuzzleId() {
    return activePuzzles.stream().reduce((ignored, latest) -> latest);
  }

  Optional<TrackingEvent> puzzleStarted(String puzzleId) {
    String startedPuzzle = requireText(puzzleId, "puzzleId");
    if (solvedPuzzles.contains(startedPuzzle)) {
      return Optional.empty();
    }
    if (startedPuzzles.contains(startedPuzzle)) {
      return Optional.empty();
    }
    TrackingEvent startedEvent =
        event(
            TrackingEventType.PUZZLE_STARTED,
            Optional.empty(),
            Optional.of(startedPuzzle),
            Optional.empty(),
            Optional.empty(),
            TrackingJson.object());
    startedPuzzles.add(startedPuzzle);
    touchActivePuzzle(startedPuzzle);
    return Optional.of(startedEvent);
  }

  TrackingEvent attempt(
      String puzzleId,
      String objectId,
      String answerKind,
      String rawAnswer,
      boolean correct,
      UUID participantId) {
    String attemptedPuzzle = requireText(puzzleId, "puzzleId");
    int attemptNumber = attemptsByPuzzle.getOrDefault(attemptedPuzzle, 0) + 1;
    ObjectNode payload =
        TrackingJson.object()
            .put("answerKind", requireText(answerKind, "answerKind"))
            .put("attemptNumber", attemptNumber)
            .put("answer", java.util.Objects.requireNonNull(rawAnswer, "rawAnswer"));
    TrackingEvent attemptEvent =
        event(
            TrackingEventType.ANSWER_SUBMITTED,
            Optional.of(participantId),
            Optional.of(attemptedPuzzle),
            Optional.of(requireText(objectId, "objectId")),
            Optional.of(correct ? TrackingOutcome.CORRECT : TrackingOutcome.INCORRECT),
            payload);
    attemptsByPuzzle.put(attemptedPuzzle, attemptNumber);
    touchActivePuzzle(attemptedPuzzle);
    return attemptEvent;
  }

  Optional<TrackingEvent> hintUsed(String puzzleId, String hintId, UUID participantId) {
    String trackedPuzzle = requireText(puzzleId, "puzzleId");
    String trackedHint = requireText(hintId, "hintId");
    String hintKey = trackedPuzzle + "\u0000" + trackedHint;
    if (solvedPuzzles.contains(trackedPuzzle) || usedHints.contains(hintKey)) {
      return Optional.empty();
    }
    TrackingEvent hintEvent =
        event(
            TrackingEventType.HINT_USED,
            Optional.of(participantId),
            Optional.of(trackedPuzzle),
            Optional.of(trackedHint),
            Optional.empty(),
            TrackingJson.object().put("hintId", trackedHint));
    usedHints.add(hintKey);
    touchActivePuzzle(trackedPuzzle);
    return Optional.of(hintEvent);
  }

  Optional<TrackingEvent> puzzleSolved(String puzzleId) {
    String solvedPuzzle = requireText(puzzleId, "puzzleId");
    if (solvedPuzzles.contains(solvedPuzzle)) {
      activePuzzles.remove(solvedPuzzle);
      return Optional.empty();
    }
    TrackingEvent event =
        event(
            TrackingEventType.PUZZLE_SOLVED,
            Optional.empty(),
            Optional.of(solvedPuzzle),
            Optional.empty(),
            Optional.empty(),
            TrackingJson.object());
    solvedPuzzles.add(solvedPuzzle);
    activePuzzles.remove(solvedPuzzle);
    return Optional.of(event);
  }

  TrackingEvent event(
      TrackingEventType eventType,
      Optional<UUID> participantId,
      Optional<String> puzzleId,
      Optional<String> objectId,
      Optional<TrackingOutcome> outcome,
      JsonNode payload) {
    return event(eventType, participantId, puzzleId, objectId, outcome, payload, Instant.now());
  }

  private TrackingEvent event(
      TrackingEventType eventType,
      Optional<UUID> participantId,
      Optional<String> puzzleId,
      Optional<String> objectId,
      Optional<TrackingOutcome> outcome,
      JsonNode payload,
      Instant occurredAt) {
    if (finished) {
      throw new IllegalStateException("Tracking session has finished");
    }
    long nextSequence = sequence + 1;
    TrackingEvent event =
        new TrackingEvent(
            SCHEMA_VERSION,
            descriptor.sessionId(),
            nextSequence,
            TrackingEvent.eventId(descriptor.sessionId(), nextSequence),
            participantId,
            descriptor.roomId(),
            eventType,
            puzzleId,
            objectId,
            outcome,
            elapsedMs(),
            occurredAt,
            payload);
    outbox.append(event);
    sequence = nextSequence;
    if (uploader != null) {
      try {
        uploader.offer(
            new TrackingBatch(SCHEMA_VERSION, descriptor, participants(), List.of(event)));
      } catch (RuntimeException exception) {
        LOGGER.warn("Could not queue tracking event {} for upload", event.eventId(), exception);
      }
    }
    return event;
  }

  Optional<UUID> participantJoined(short clientId, boolean roomPlayedBefore) {
    if (finished) {
      return Optional.empty();
    }
    ParticipantState existing = participantsByClient.get(clientId);
    if (existing != null) {
      TrackingParticipant previousParticipant = existing.participant;
      if (previousParticipant.leftAt().isEmpty()) {
        return Optional.of(previousParticipant.participantId());
      }
      TrackingParticipant rejoinedParticipant =
          new TrackingParticipant(
              descriptor.sessionId(),
              previousParticipant.participantId(),
              previousParticipant.roomPlayedBefore(),
              previousParticipant.joinedAt(),
              Optional.empty());
      existing.participant = rejoinedParticipant;
      try {
        participantEvent(rejoinedParticipant, TrackingEventType.PARTICIPANT_JOINED, Instant.now());
      } catch (RuntimeException exception) {
        existing.participant = previousParticipant;
        throw exception;
      }
      return Optional.of(rejoinedParticipant.participantId());
    }
    Instant joinedAt = Instant.now();
    TrackingParticipant participant =
        new TrackingParticipant(
            descriptor.sessionId(),
            UUID.randomUUID(),
            roomPlayedBefore,
            joinedAt,
            Optional.empty());
    participantsByClient.put(clientId, new ParticipantState(participant));
    try {
      participantEvent(participant, TrackingEventType.PARTICIPANT_JOINED, joinedAt);
    } catch (RuntimeException exception) {
      participantsByClient.remove(clientId);
      throw exception;
    }
    return Optional.of(participant.participantId());
  }

  void participantLeft(short clientId) {
    if (finished) {
      return;
    }
    ParticipantState state = participantsByClient.get(clientId);
    if (state == null || state.participant.leftAt().isPresent()) {
      return;
    }
    TrackingParticipant previousParticipant = state.participant;
    Instant leftAt = Instant.now();
    TrackingParticipant leftParticipant =
        new TrackingParticipant(
            descriptor.sessionId(),
            previousParticipant.participantId(),
            previousParticipant.roomPlayedBefore(),
            previousParticipant.joinedAt(),
            Optional.of(leftAt));
    state.participant = leftParticipant;
    try {
      participantEvent(leftParticipant, TrackingEventType.PARTICIPANT_LEFT, leftAt);
      participantsByEntity.values().removeIf(leftParticipant.participantId()::equals);
    } catch (RuntimeException exception) {
      state.participant = previousParticipant;
      throw exception;
    }
  }

  void associateEntity(short clientId, int entityId) {
    if (finished) {
      return;
    }
    participantForClient(clientId)
        .ifPresent(participant -> participantsByEntity.put(entityId, participant));
  }

  Optional<UUID> participantForClient(short clientId) {
    return Optional.ofNullable(participantsByClient.get(clientId))
        .filter(state -> state.participant.leftAt().isEmpty())
        .map(state -> state.participant.participantId());
  }

  Optional<UUID> participantForEntity(int entityId) {
    return Optional.ofNullable(participantsByEntity.get(entityId));
  }

  boolean participantActive(UUID participantId) {
    return participantsByClient.values().stream()
        .map(state -> state.participant)
        .anyMatch(
            participant ->
                participant.participantId().equals(participantId)
                    && participant.leftAt().isEmpty());
  }

  void finish(TrackingSessionStatus status, Optional<String> abortedAtPuzzleId) {
    if (finished) {
      return;
    }
    TrackingSessionFinish finish =
        new TrackingSessionFinish(
            SCHEMA_VERSION,
            descriptor.sessionId(),
            sequence,
            status,
            Instant.now(),
            elapsedMs(),
            abortedAtPuzzleId);
    TrackingPersistenceException persistenceFailure = null;
    try {
      outbox.append(finish);
    } catch (TrackingPersistenceException exception) {
      persistenceFailure = exception;
    }
    finished = true;
    if (uploader != null) {
      uploader.finishAndFlush(finish, sequence, FINAL_UPLOAD_TIMEOUT);
    }
    if (persistenceFailure != null) {
      throw persistenceFailure;
    }
  }

  boolean remotePending() {
    return outbox.failed() || (uploader != null && uploader.pending(sequence, finished));
  }

  private void participantEvent(
      TrackingParticipant participant, TrackingEventType eventType, Instant occurredAt) {
    ObjectNode payload =
        TrackingJson.object().put("roomPlayedBefore", participant.roomPlayedBefore());
    event(
        eventType,
        Optional.of(participant.participantId()),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        payload,
        occurredAt);
  }

  private List<TrackingParticipant> participants() {
    return participantsByClient.values().stream().map(state -> state.participant).toList();
  }

  private long elapsedMs() {
    return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedNanos);
  }

  private void touchActivePuzzle(String puzzleId) {
    if (solvedPuzzles.contains(puzzleId)) {
      return;
    }
    activePuzzles.remove(puzzleId);
    activePuzzles.add(puzzleId);
  }

  private static String requireText(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(name + " must not be blank");
    }
    return value.strip();
  }

  private static final class ParticipantState {
    private TrackingParticipant participant;

    private ParticipantState(TrackingParticipant participant) {
      this.participant = participant;
    }
  }
}
