package engine.tracking;

import engine.utils.logging.DungeonLogger;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
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
  private final Path outboxPath;
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
  private boolean persistenceFailed;

  TrackingSession(TrackingConfig config) {
    this.config = config;
    UUID sessionId = UUID.randomUUID();
    this.descriptor =
        new TrackingSessionDescriptor(SCHEMA_VERSION, sessionId, config.roomId(), Instant.now());
    this.outboxPath = createOutbox(config.outboxDirectory(), sessionId);
    writeSessionDescriptor();
    this.uploader =
        config.endpoint().map(endpoint -> new TrackingUploader(config, descriptor)).orElse(null);
    event(
        TrackingEventType.SESSION_STARTED,
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        TrackingJson.object());
    LOGGER.info("Tracking session {} writes to {}", sessionId, outboxPath);
  }

  String roomId() {
    return descriptor.roomId();
  }

  Path outboxPath() {
    return outboxPath;
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
            Instant.now(),
            payload);
    appendEvent(event);
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
      TrackingParticipant rejoinedParticipant =
          new TrackingParticipant(
              descriptor.sessionId(),
              previousParticipant.participantId(),
              previousParticipant.roomPlayedBefore(),
              previousParticipant.joinedAt(),
              Optional.empty());
      writeParticipant(rejoinedParticipant);
      existing.participant = rejoinedParticipant;
      try {
        participantEvent(rejoinedParticipant, TrackingEventType.PARTICIPANT_JOINED);
      } catch (RuntimeException exception) {
        existing.participant = previousParticipant;
        restoreParticipant(previousParticipant, exception);
        throw exception;
      }
      return Optional.of(rejoinedParticipant.participantId());
    }
    TrackingParticipant participant =
        new TrackingParticipant(
            descriptor.sessionId(),
            UUID.randomUUID(),
            roomPlayedBefore,
            Instant.now(),
            Optional.empty());
    writeParticipant(participant);
    participantsByClient.put(clientId, new ParticipantState(participant));
    try {
      participantEvent(participant, TrackingEventType.PARTICIPANT_JOINED);
    } catch (RuntimeException exception) {
      participantsByClient.remove(clientId);
      deleteParticipant(participant, exception);
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
    TrackingParticipant leftParticipant =
        new TrackingParticipant(
            descriptor.sessionId(),
            previousParticipant.participantId(),
            previousParticipant.roomPlayedBefore(),
            previousParticipant.joinedAt(),
            Optional.of(Instant.now()));
    writeParticipant(leftParticipant);
    state.participant = leftParticipant;
    try {
      participantEvent(leftParticipant, TrackingEventType.PARTICIPANT_LEFT);
    } catch (RuntimeException exception) {
      state.participant = previousParticipant;
      restoreParticipant(previousParticipant, exception);
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
        .map(state -> state.participant.participantId());
  }

  Optional<UUID> participantForEntity(int entityId) {
    return Optional.ofNullable(participantsByEntity.get(entityId));
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
      writeFinish(finish);
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
    return persistenceFailed || (uploader != null && uploader.pending(sequence, finished));
  }

  private void participantEvent(TrackingParticipant participant, TrackingEventType eventType) {
    ObjectNode payload =
        TrackingJson.object().put("roomPlayedBefore", participant.roomPlayedBefore());
    event(
        eventType,
        Optional.of(participant.participantId()),
        Optional.empty(),
        Optional.empty(),
        Optional.empty(),
        payload);
  }

  private List<TrackingParticipant> participants() {
    return participantsByClient.values().stream().map(state -> state.participant).toList();
  }

  private long elapsedMs() {
    return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedNanos);
  }

  private static Path createOutbox(Path directory, UUID sessionId) {
    Path path = directory.resolve(sessionId + ".jsonl").toAbsolutePath();
    try {
      Files.createDirectories(path.getParent());
      return Files.createFile(path);
    } catch (IOException | RuntimeException exception) {
      throw new TrackingPersistenceException(
          "Could not create new tracking outbox " + path, path, path, exception);
    }
  }

  private void writeSessionDescriptor() {
    Path path = sidecar("session.json");
    try {
      writeNewSidecar(path, TrackingJson.write(descriptor));
    } catch (IOException | RuntimeException exception) {
      throw persistenceFailure(
          "Could not create tracking session descriptor " + path, path, exception);
    }
  }

  private void writeFinish(TrackingSessionFinish finish) {
    Path path = sidecar("finish.json");
    try {
      writeNewSidecar(path, TrackingJson.write(finish));
    } catch (IOException | RuntimeException exception) {
      throw persistenceFailure("Could not write tracking finish " + path, path, exception);
    }
  }

  private void writeParticipant(TrackingParticipant participant) {
    Path target = participantPath(participant);
    Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
    try {
      Files.writeString(
          temporary,
          TrackingJson.write(participant),
          StandardCharsets.UTF_8,
          StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING,
          StandardOpenOption.WRITE);
      forceFile(temporary);
      try {
        Files.move(
            temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException unsupportedAtomicMove) {
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException | RuntimeException exception) {
      throw persistenceFailure(
          "Could not write anonymous tracking participant " + target, target, exception);
    }
  }

  private void restoreParticipant(TrackingParticipant participant, RuntimeException failure) {
    try {
      writeParticipant(participant);
    } catch (RuntimeException restoreFailure) {
      failure.addSuppressed(restoreFailure);
    }
  }

  private void deleteParticipant(TrackingParticipant participant, RuntimeException failure) {
    try {
      Files.deleteIfExists(participantPath(participant));
    } catch (IOException | RuntimeException deleteFailure) {
      failure.addSuppressed(
          persistenceFailure(
              "Could not remove anonymous tracking participant " + participantPath(participant),
              participantPath(participant),
              deleteFailure));
    }
  }

  private Path participantPath(TrackingParticipant participant) {
    return outboxPath.resolveSibling(
        descriptor.sessionId() + ".participant-" + participant.participantId() + ".json");
  }

  private Path sidecar(String suffix) {
    String filename = outboxPath.getFileName().toString().replace(".jsonl", "." + suffix);
    return outboxPath.resolveSibling(filename);
  }

  private void appendEvent(TrackingEvent event) {
    long originalSize;
    try {
      originalSize = Files.size(outboxPath);
    } catch (IOException | RuntimeException exception) {
      throw persistenceFailure(
          "Could not inspect tracking outbox " + outboxPath, outboxPath, exception);
    }
    try {
      TrackingJson.appendEventsJsonl(outboxPath, List.of(event));
      try (FileChannel channel = FileChannel.open(outboxPath, StandardOpenOption.WRITE)) {
        channel.force(true);
      }
    } catch (IOException | RuntimeException exception) {
      rollbackAppend(originalSize, exception);
      throw persistenceFailure(
          "Could not append tracking event " + event.eventId() + " to " + outboxPath,
          outboxPath,
          exception);
    }
  }

  private void writeNewSidecar(Path target, String content) throws IOException {
    Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
    Files.writeString(
        temporary,
        content,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING,
        StandardOpenOption.WRITE);
    forceFile(temporary);
    try {
      Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
    } catch (AtomicMoveNotSupportedException unsupportedAtomicMove) {
      Files.move(temporary, target);
    }
  }

  private void forceFile(Path path) throws IOException {
    try (FileChannel channel = FileChannel.open(path, StandardOpenOption.WRITE)) {
      channel.force(true);
    }
  }

  private void rollbackAppend(long originalSize, RuntimeException appendFailure) {
    rollbackAppend(originalSize, (Throwable) appendFailure);
  }

  private void rollbackAppend(long originalSize, IOException appendFailure) {
    rollbackAppend(originalSize, (Throwable) appendFailure);
  }

  private void rollbackAppend(long originalSize, Throwable appendFailure) {
    try (FileChannel channel = FileChannel.open(outboxPath, StandardOpenOption.WRITE)) {
      channel.truncate(originalSize);
      channel.force(true);
    } catch (IOException | RuntimeException rollbackFailure) {
      appendFailure.addSuppressed(rollbackFailure);
      LOGGER.error("Could not roll back failed tracking append at {}", outboxPath, rollbackFailure);
    }
  }

  private TrackingPersistenceException persistenceFailure(
      String message, Path path, Throwable cause) {
    persistenceFailed = true;
    return new TrackingPersistenceException(message, path, outboxPath, cause);
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
