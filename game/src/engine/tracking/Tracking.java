package engine.tracking;

import engine.Game;
import engine.game.ManagedServerStatus;
import engine.time.PlayClock;
import engine.utils.logging.DungeonLogger;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import tracking.core.TrackingEvent;
import tracking.core.TrackingEventType;
import tracking.core.TrackingInteractionStatus;
import tracking.core.TrackingJson;
import tracking.core.TrackingSessionStatus;

/**
 * Public tracking API for generated and hand-written rooms.
 *
 * <p>Configure tracking before {@link Game#run()}. Only the authoritative server or singleplayer
 * process starts a session. Room code submits facts through this class; the engine assigns
 * sequence, UTC wall-clock time, and shared active play time.
 */
public final class Tracking {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(Tracking.class);
  private static final Object LOCK = new Object();
  private static final String PERSISTENCE_PENDING_STATUS = "tracking-persistence-pending";
  private static final Set<String> PENDING_PUZZLE_STARTS = new LinkedHashSet<>();

  private static TrackingConfig explicitConfig;
  private static TrackingSession session;
  private static boolean sessionStartAttempted;
  private static boolean trackingAllowed = true;
  private static Optional<PlayClock.Outcome> outcome = Optional.empty();
  private static String clientRoomId;
  private static PersistenceFailure persistenceFailure;

  private Tracking() {}

  private static void configure(TrackingConfig config) {
    configure(config, true);
  }

  private static void configure(TrackingConfig config, boolean trackingAllowed) {
    synchronized (LOCK) {
      if (sessionStartAttempted) {
        throw new IllegalStateException("Tracking session start is already closed");
      }
      session = null;
      outcome = Optional.empty();
      PENDING_PUZZLE_STARTS.clear();
      persistenceFailure = null;
      explicitConfig = Objects.requireNonNull(config, "config");
      Tracking.trackingAllowed = trackingAllowed;
    }
  }

  /**
   * Configures a room while retaining deployment endpoint, credentials, and outbox settings.
   *
   * @param roomId stable room identifier
   */
  public static void configureRoom(String roomId) {
    configure(TrackingConfig.forRoom(roomId));
  }

  /**
   * Configures a room with an optional stable playthrough identifier.
   *
   * @param roomId stable room identifier
   * @param runId optional identifier shared by sessions of one playthrough
   */
  public static void configureRoom(String roomId, Optional<UUID> runId) {
    configure(TrackingConfig.forRoom(roomId, runId));
  }

  /**
   * Configures a room and explicitly enables or disables tracking for the current run.
   *
   * <p>A disabled run does not create a tracking session, local JSONL outbox, or remote upload.
   * Existing overloads remain enabled by default for rooms that do not use a consent flow.
   *
   * @param roomId stable room identifier
   * @param runId optional stable playthrough identifier
   * @param trackingAllowed whether the player consented to tracking
   */
  public static void configureRoom(String roomId, Optional<UUID> runId, boolean trackingAllowed) {
    configure(TrackingConfig.forRoom(roomId, runId), trackingAllowed);
  }

  /**
   * Configures a consent-gated room with an explicit operator contact.
   *
   * @param roomId stable room identifier
   * @param operatorEmail contact shown when local tracking persistence needs attention
   * @param runId optional stable playthrough identifier
   * @param trackingAllowed whether the player consented to tracking
   */
  public static void configureRoom(
      String roomId, String operatorEmail, Optional<UUID> runId, boolean trackingAllowed) {
    configure(TrackingConfig.forRoom(roomId, operatorEmail, runId), trackingAllowed);
  }

  /**
   * Configures a room with an operator email while retaining other deployment settings.
   *
   * @param roomId stable room identifier
   * @param operatorEmail operator email shown when tracking persistence remains pending
   */
  public static void configureRoom(String roomId, String operatorEmail) {
    configure(TrackingConfig.forRoom(roomId, operatorEmail));
  }

  /**
   * Configures a room with operator email and an optional playthrough identifier.
   *
   * @param roomId stable room identifier
   * @param operatorEmail operator email shown when tracking persistence remains pending
   * @param runId optional identifier shared by sessions of one playthrough
   */
  public static void configureRoom(String roomId, String operatorEmail, Optional<UUID> runId) {
    configure(TrackingConfig.forRoom(roomId, operatorEmail, runId));
  }

  /**
   * Returns the configured room ID, if tracking is configured.
   *
   * @return configured room ID
   */
  public static Optional<String> roomId() {
    synchronized (LOCK) {
      if (session != null) {
        return Optional.of(session.roomId());
      }
      return config().map(TrackingConfig::roomId);
    }
  }

  /**
   * Returns whether an authoritative tracking session is running.
   *
   * @return whether a session is active
   */
  public static boolean active() {
    synchronized (LOCK) {
      return trackingAllowed && session != null && !session.finished();
    }
  }

  /**
   * Returns whether the configured deployment stores tracking data through the HTTP backend.
   *
   * <p>Rooms use this value to select privacy text. The default build keeps the backend disabled
   * and therefore continues to describe the local JSONL outbox truthfully.
   *
   * @return {@code true} when the central tracking backend is enabled
   */
  public static boolean remoteStorageEnabled() {
    synchronized (LOCK) {
      return TrackingConfig.TRACKING_ENABLED && config().isPresent();
    }
  }

  /**
   * Returns the append-only local log of the current session.
   *
   * @return current session outbox path
   */
  public static Optional<Path> outboxPath() {
    synchronized (LOCK) {
      if (session != null) {
        return Optional.of(session.outboxPath());
      }
      return Optional.ofNullable(persistenceFailure).map(PersistenceFailure::path);
    }
  }

  /**
   * Deletes all local JSONL tracking files for the configured outbox.
   *
   * <p>This is the local deletion path exposed by a room's privacy settings. It deliberately only
   * removes regular {@code .jsonl} files and leaves savegames, configuration and unrelated files
   * untouched.
   *
   * @return {@code true} when all local tracking files were removed or none existed
   */
  public static boolean deleteLocalData() {
    synchronized (LOCK) {
      trackingAllowed = false;
      PENDING_PUZZLE_STARTS.clear();
      if (session != null && !session.finished()) {
        try {
          finishWithOutcome();
        } catch (TrackingPersistenceException exception) {
          recordPersistenceFailure(exception);
          return false;
        }
        sessionStartAttempted = true;
      }
      session = null;

      Path directory =
          config().map(TrackingConfig::outboxDirectory).orElse(Path.of("tracking-outbox"));
      if (!Files.isDirectory(directory)) return true;
      try (var files = Files.list(directory)) {
        for (Path file : files.filter(Tracking::isTrackingFile).toList()) {
          Files.deleteIfExists(file);
        }
        return true;
      } catch (IOException | RuntimeException exception) {
        LOGGER.warn("Could not delete local tracking data in {}", directory, exception);
        return false;
      }
    }
  }

  private static boolean isTrackingFile(Path file) {
    return Files.isRegularFile(file) && file.getFileName().toString().endsWith(".jsonl");
  }

  /**
   * Records when a puzzle becomes accessible or unlocked.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @return newly recorded event, or empty when inactive or already recorded
   */
  public static Optional<TrackingEvent> puzzleStarted(String puzzleId) {
    synchronized (LOCK) {
      String startedPuzzle = requirePuzzleId(puzzleId);
      if (!trackingAllowed || session == null || session.finished()) {
        if (!sessionStartAttempted && !Game.isMultiplayerClient() && startConfig().isPresent()) {
          PENDING_PUZZLE_STARTS.add(startedPuzzle);
        }
        return Optional.empty();
      }
      try {
        return session.puzzleStarted(startedPuzzle);
      } catch (TrackingPersistenceException exception) {
        recordPersistenceFailure(exception);
        return Optional.empty();
      }
    }
  }

  /**
   * Records one answer attempt, including the raw answer.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @param objectId stable answered-object identifier
   * @param answerKind answer representation
   * @param rawAnswer complete submitted answer
   * @param correct server-evaluated correctness
   * @param participantId session-scoped participant identifier
   * @return newly recorded event, or empty when inactive
   */
  public static Optional<TrackingEvent> attempt(
      String puzzleId,
      String objectId,
      String answerKind,
      String rawAnswer,
      boolean correct,
      UUID participantId) {
    return attempt(
        puzzleId, objectId, answerKind, rawAnswer, correct, participantId, Optional.empty());
  }

  /**
   * Records an answer with the help state at submission and concrete outcome details.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @param objectId stable interacted-object identifier
   * @param answerKind answer representation
   * @param rawAnswer complete submitted answer
   * @param participantId session-scoped anonymous participant
   * @param details help state and final failure reasons
   * @return newly recorded event, or empty when tracking is inactive or recording fails
   */
  public static Optional<TrackingEvent> attempt(
      String puzzleId,
      String objectId,
      String answerKind,
      String rawAnswer,
      UUID participantId,
      AttemptDetails details) {
    return attempt(
        puzzleId,
        objectId,
        answerKind,
        rawAnswer,
        details.failureReasons().isEmpty(),
        participantId,
        Optional.of(details));
  }

  private static Optional<TrackingEvent> attempt(
      String puzzleId,
      String objectId,
      String answerKind,
      String rawAnswer,
      boolean correct,
      UUID participantId,
      Optional<AttemptDetails> details) {
    synchronized (LOCK) {
      if (!trackingAllowed
          || session == null
          || session.finished()
          || !session.participantKnown(participantId)) {
        return Optional.empty();
      }
      try {
        return Optional.of(
            session.attempt(
                puzzleId, objectId, answerKind, rawAnswer, correct, participantId, details));
      } catch (TrackingPersistenceException exception) {
        recordPersistenceFailure(exception);
        return Optional.empty();
      }
    }
  }

  /**
   * Records a completed player interaction that belongs to no single puzzle, such as discovering an
   * object, opening the quest log, or requesting help.
   *
   * @param objectId stable room-local object identifier
   * @param action stable action identifier, never display text or mouse coordinates
   * @param participantId session-scoped anonymous participant
   * @return newly recorded event, or empty when inactive
   */
  public static Optional<TrackingEvent> interaction(
      String objectId, String action, UUID participantId) {
    synchronized (LOCK) {
      if (!trackingAllowed
          || session == null
          || session.finished()
          || !session.participantActive(participantId)) {
        return Optional.empty();
      }
      try {
        return Optional.of(session.interaction(objectId, action, participantId));
      } catch (TrackingPersistenceException exception) {
        recordPersistenceFailure(exception);
        return Optional.empty();
      }
    }
  }

  /**
   * Records a completed interaction with one puzzle, such as editing or running it, without
   * treating it as a submitted answer. Its first such event marks the first contact with the
   * puzzle.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @param objectId stable interacted object identifier
   * @param action stable action identifier
   * @param participantId session-scoped participant identifier
   * @return recorded event, or empty when tracking or the participant is inactive
   */
  public static Optional<TrackingEvent> interaction(
      String puzzleId, String objectId, String action, UUID participantId) {
    synchronized (LOCK) {
      if (!trackingAllowed
          || session == null
          || session.finished()
          || !session.participantActive(participantId)) {
        return Optional.empty();
      }
      try {
        return Optional.of(session.interaction(puzzleId, objectId, action, participantId));
      } catch (TrackingPersistenceException exception) {
        recordPersistenceFailure(exception);
        return Optional.empty();
      }
    }
  }

  /**
   * Records a puzzle interaction with an explicit result, without treating it as a submitted answer
   * or a puzzle start.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @param objectId stable interacted object identifier
   * @param action stable action identifier
   * @param status authoritative interaction result
   * @param reason stable reason for the result
   * @param participantId session-scoped participant identifier
   * @return recorded event, or empty when tracking or the participant is inactive
   */
  public static Optional<TrackingEvent> interaction(
      String puzzleId,
      String objectId,
      String action,
      TrackingInteractionStatus status,
      String reason,
      UUID participantId) {
    synchronized (LOCK) {
      if (!trackingAllowed
          || session == null
          || session.finished()
          || !session.participantActive(participantId)) {
        return Optional.empty();
      }
      try {
        return Optional.of(
            session.interaction(puzzleId, objectId, action, status, reason, participantId));
      } catch (TrackingPersistenceException exception) {
        recordPersistenceFailure(exception);
        return Optional.empty();
      }
    }
  }

  /**
   * Records use of one hint.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @param hintId stable room-local hint identifier
   * @param participantId session-scoped participant identifier
   * @return newly recorded event, or empty when inactive or already recorded
   */
  public static Optional<TrackingEvent> hintUsed(
      String puzzleId, String hintId, UUID participantId) {
    synchronized (LOCK) {
      if (!trackingAllowed
          || session == null
          || session.finished()
          || !session.participantActive(participantId)) {
        return Optional.empty();
      }
      try {
        return session.hintUsed(puzzleId, hintId, participantId);
      } catch (TrackingPersistenceException exception) {
        recordPersistenceFailure(exception);
        return Optional.empty();
      }
    }
  }

  /**
   * Records successful completion of one puzzle.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @return newly recorded event, or empty when inactive or already recorded
   */
  public static Optional<TrackingEvent> puzzleSolved(String puzzleId) {
    synchronized (LOCK) {
      if (!trackingAllowed || session == null || session.finished()) {
        return Optional.empty();
      }
      try {
        return session.puzzleSolved(puzzleId);
      } catch (TrackingPersistenceException exception) {
        recordPersistenceFailure(exception);
        return Optional.empty();
      }
    }
  }

  /**
   * Returns the session-scoped participant identifier mapped to a runtime network client.
   *
   * @param clientId transient network client ID
   * @return session-scoped participant identifier
   */
  public static Optional<UUID> participantForClient(short clientId) {
    synchronized (LOCK) {
      return session == null ? Optional.empty() : session.participantForClient(clientId);
    }
  }

  /**
   * Returns the session-scoped participant identifier mapped to a runtime player entity.
   *
   * @param entityId transient server entity ID
   * @return session-scoped participant identifier
   */
  public static Optional<UUID> participantForEntity(int entityId) {
    synchronized (LOCK) {
      return session == null ? Optional.empty() : session.participantForEntity(entityId);
    }
  }

  /**
   * Records a survey answer before play starts or after the outcome while the session remains open,
   * without changing play time or puzzle state.
   *
   * @param questionnaireId stable questionnaire identifier
   * @param questionId stable question identifier
   * @param answer full answer as text or structured JSON value
   * @param participantId session-scoped participant identifier
   * @return recorded event, or empty when tracking or the participant is inactive
   */
  public static Optional<TrackingEvent> surveyAnswered(
      String questionnaireId, String questionId, JsonNode answer, UUID participantId) {
    synchronized (LOCK) {
      if (!trackingAllowed
          || session == null
          || session.finished()
          || !session.participantKnown(participantId)) {
        return Optional.empty();
      }
      ObjectNode payload =
          TrackingJson.object()
              .put("questionnaireId", questionnaireId)
              .put("questionId", questionId);
      payload.set("answer", Objects.requireNonNull(answer, "answer"));
      try {
        return Optional.of(
            session.event(
                TrackingEventType.SURVEY_ANSWERED,
                Optional.of(participantId),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                payload));
      } catch (TrackingPersistenceException exception) {
        recordPersistenceFailure(exception);
        return Optional.empty();
      }
    }
  }

  static void playClockChanged(PlayClock.Transition transition) {
    synchronized (LOCK) {
      // The room's result decides the finish status; the session stays open for surveys.
      transition.outcome().ifPresent(result -> outcome = Optional.of(result));
      if (!trackingAllowed || session == null || session.finished()) return;
      ObjectNode payload = TrackingJson.object();
      transition.reason().ifPresent(reason -> payload.put("reason", reason.name()));
      transition.outcome().ifPresent(result -> payload.put("outcome", result.name()));
      TrackingEventType type =
          switch (transition.event()) {
            case STARTED -> TrackingEventType.PLAY_STARTED;
            case PAUSED -> TrackingEventType.PLAY_PAUSED;
            case RESUMED -> TrackingEventType.PLAY_RESUMED;
            case ENDED -> TrackingEventType.PLAY_ENDED;
          };
      try {
        session.event(
            type, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), payload);
      } catch (TrackingPersistenceException exception) {
        recordPersistenceFailure(exception);
      }
    }
  }

  static void startSingleplayerSession(int entityId) {
    synchronized (LOCK) {
      if (!Game.isSingleplayer() || sessionStartAttempted) {
        return;
      }
      sessionStartAttempted = true;
      Optional<TrackingConfig> configured = startConfig();
      if (configured.isPresent()) {
        startSingleplayerSession(configured.orElseThrow(), entityId);
      } else {
        PENDING_PUZZLE_STARTS.clear();
      }
    }
  }

  private static void startSingleplayerSession(TrackingConfig configured, int entityId) {
    try {
      session = new TrackingSession(configured);
      boolean playedBefore = RoomHistory.playedBefore(configured.roomId());
      UUID participant = session.participantJoined((short) 0, playedBefore).orElseThrow();
      session.associateEntity((short) 0, entityId);
      emitPendingPuzzleStarts();
      RoomHistory.markPlayed(configured.roomId());
      LOGGER.info("Started local tracking participant {}", participant);
    } catch (TrackingPersistenceException exception) {
      recordPersistenceFailure(exception, configured.operatorEmail());
    } catch (RuntimeException exception) {
      LOGGER.error("Could not start tracking", exception);
    } finally {
      PENDING_PUZZLE_STARTS.clear();
    }
  }

  static void clientRoomContext(String roomId) {
    synchronized (LOCK) {
      clientRoomId = roomId == null || roomId.isBlank() ? null : roomId.strip();
    }
  }

  static boolean clientRoomPlayedBefore() {
    synchronized (LOCK) {
      return clientRoomId != null && RoomHistory.playedBefore(clientRoomId);
    }
  }

  static void markClientRoomPlayed() {
    synchronized (LOCK) {
      if (clientRoomId != null) {
        RoomHistory.markPlayed(clientRoomId);
      }
    }
  }

  static Optional<UUID> participantJoined(short clientId, boolean roomPlayedBefore) {
    synchronized (LOCK) {
      if (!trackingAllowed || Game.isSingleplayer() || !Game.network().isServer()) {
        return Optional.empty();
      }
      boolean startingSession = (session == null || session.finished()) && !sessionStartAttempted;
      if (startingSession) {
        sessionStartAttempted = true;
        startConfig().ifPresent(Tracking::startMultiplayerSession);
      }
      if (session == null || session.finished()) {
        if (startingSession) {
          PENDING_PUZZLE_STARTS.clear();
        }
        return Optional.empty();
      }
      try {
        Optional<UUID> participant = session.participantJoined(clientId, roomPlayedBefore);
        if (startingSession && participant.isPresent()) {
          try {
            emitPendingPuzzleStarts();
          } catch (TrackingPersistenceException exception) {
            recordPersistenceFailure(exception);
          }
        }
        return participant;
      } catch (TrackingPersistenceException exception) {
        recordPersistenceFailure(exception);
        return Optional.empty();
      } finally {
        if (startingSession) {
          PENDING_PUZZLE_STARTS.clear();
        }
      }
    }
  }

  private static void startMultiplayerSession(TrackingConfig configured) {
    try {
      TrackingSession previous = session;
      session = new TrackingSession(configured);
      if (previous != null) session.continuePuzzleState(previous);
    } catch (TrackingPersistenceException exception) {
      recordPersistenceFailure(exception, configured.operatorEmail());
    } catch (RuntimeException exception) {
      LOGGER.error("Could not start tracking", exception);
    }
  }

  static void participantLeft(short clientId) {
    synchronized (LOCK) {
      if (session != null && !session.finished()) {
        try {
          session.participantLeft(clientId);
        } catch (TrackingPersistenceException exception) {
          recordPersistenceFailure(exception);
        }
      }
    }
  }

  /**
   * Stops all future tracking events for the current authoritative run.
   *
   * <p>This is used when a multiplayer participant refuses consent. Events already written before
   * the refusal are not retroactively altered. The session closes with its outcome or interruption,
   * and its final batch is still uploaded; no participant or later event is recorded.
   */
  static void disableTrackingForRun() {
    synchronized (LOCK) {
      if (session != null && !session.finished()) {
        try {
          finishWithOutcome();
        } catch (TrackingPersistenceException exception) {
          recordPersistenceFailure(exception);
        }
      }
      trackingAllowed = false;
      PENDING_PUZZLE_STARTS.clear();
      LOGGER.info("Tracking disabled because a multiplayer participant refused consent.");
    }
  }

  static void associateEntity(short clientId, int entityId) {
    synchronized (LOCK) {
      if (session != null && !session.finished()) {
        session.associateEntity(clientId, entityId);
      }
    }
  }

  /** Records the session's final facts after all post-outcome activity. */
  static void finishSession() {
    synchronized (LOCK) {
      finishOpenSession();
    }
  }

  /** Closes an empty session while retaining the paused room for a later reconnect. */
  static void noParticipants() {
    synchronized (LOCK) {
      finishOpenSession();
      if (outcome.isEmpty() && trackingAllowed) sessionStartAttempted = false;
    }
  }

  private static void finishOpenSession() {
    sessionStartAttempted = true;
    PENDING_PUZZLE_STARTS.clear();
    if (session != null) {
      try {
        finishWithOutcome();
      } catch (TrackingPersistenceException exception) {
        recordPersistenceFailure(exception);
      }
    }
  }

  static boolean remotePending() {
    synchronized (LOCK) {
      return persistenceFailure != null || (session != null && session.remotePending());
    }
  }

  static Optional<CompletableFuture<Boolean>> backendHealth() {
    synchronized (LOCK) {
      if (!TrackingConfig.TRACKING_ENABLED) {
        return Optional.empty();
      }
      return startConfig().map(config -> TrackingBackendHealth.check(config.endpoint()));
    }
  }

  static void warnIfRemotePending() {
    synchronized (LOCK) {
      if (persistenceFailure == null && (session == null || !session.remotePending())) {
        return;
      }
      Path path = persistenceFailure != null ? persistenceFailure.path() : session.outboxPath();
      String operatorEmail =
          session != null ? session.operatorEmail() : persistenceFailure.operatorEmail();
      LOGGER.warn(
          "Tracking persistence is not confirmed. Send the JSONL file at {} to {}.",
          path,
          operatorEmail);
      if (ManagedServerStatus.send(persistencePendingStatus(path, operatorEmail))) {
        return;
      }
      if (GraphicsEnvironment.isHeadless()) {
        return;
      }
      showPersistencePending(path, operatorEmail);
    }
  }

  static void handleManagedServerStatus(String status) {
    try {
      var payload = TrackingJson.object(status);
      var type = payload.get("type");
      var outbox = payload.get("outboxPath");
      if (type == null
          || !type.isString()
          || !PERSISTENCE_PENDING_STATUS.equals(type.stringValue())
          || outbox == null
          || !outbox.isString()) {
        LOGGER.warn("Managed server sent an unknown tracking status.");
        return;
      }
      var email = payload.get("operatorEmail");
      if (email == null || !email.isString()) {
        LOGGER.warn("Managed server sent malformed tracking operator email.");
        return;
      }
      String operatorEmail = email.stringValue();
      Path outboxPath = Path.of(outbox.stringValue()).toAbsolutePath();
      LOGGER.warn(
          "Tracking persistence is not confirmed. Send the JSONL file at {} to {}.",
          outboxPath,
          operatorEmail);
      if (!GraphicsEnvironment.isHeadless()) {
        showPersistencePending(outboxPath, operatorEmail);
      }
    } catch (RuntimeException exception) {
      LOGGER.warn("Cannot interpret managed-server tracking status.", exception);
    }
  }

  private static String persistencePendingStatus(Path outboxPath, String operatorEmail) {
    var payload = TrackingJson.object();
    payload.put("type", PERSISTENCE_PENDING_STATUS);
    payload.put("outboxPath", outboxPath.toAbsolutePath().toString());
    payload.put("operatorEmail", operatorEmail);
    return payload.toString();
  }

  private static void showPersistencePending(Path outboxPath, String operatorEmail) {
    String message =
        "Tracking persistence is not confirmed.\nSend the JSONL file at:\n"
            + outboxPath
            + "\n\nto: "
            + operatorEmail;
    try {
      SwingUtilities.invokeLater(
          () -> {
            try {
              JOptionPane.showMessageDialog(
                  null, message, "Tracking upload pending", JOptionPane.WARNING_MESSAGE);
            } catch (RuntimeException exception) {
              LOGGER.warn(
                  "Could not display tracking upload warning for {}", outboxPath, exception);
            }
          });
    } catch (RuntimeException exception) {
      LOGGER.warn("Could not schedule tracking upload warning for {}", outboxPath, exception);
    }
  }

  /** Finishes the open session as completed after the outcome, otherwise as interrupted. */
  private static void finishWithOutcome() {
    session.finish(
        outcome
            .map(
                result ->
                    result == PlayClock.Outcome.SUCCESS
                        ? TrackingSessionStatus.COMPLETED
                        : TrackingSessionStatus.FAILED)
            .orElse(TrackingSessionStatus.INTERRUPTED),
        outcome.isPresent() ? Optional.empty() : session.currentPuzzleId());
  }

  private static void recordPersistenceFailure(TrackingPersistenceException exception) {
    recordPersistenceFailure(exception, session.operatorEmail());
  }

  private static void recordPersistenceFailure(
      TrackingPersistenceException exception, String operatorEmail) {
    persistenceFailure = new PersistenceFailure(exception.outboxPath(), operatorEmail);
    LOGGER.error(
        "Local tracking persistence failed at {}. Gameplay will continue; tracking data may be incomplete.",
        exception.outboxPath(),
        exception);
  }

  private static Optional<TrackingConfig> config() {
    return Optional.ofNullable(explicitConfig).or(TrackingConfig::fromEnvironment);
  }

  private static Optional<TrackingConfig> startConfig() {
    if (!trackingAllowed) return Optional.empty();
    try {
      return config();
    } catch (RuntimeException exception) {
      LOGGER.error("Could not read tracking configuration", exception);
      return Optional.empty();
    }
  }

  private static void emitPendingPuzzleStarts() {
    for (String puzzleId : PENDING_PUZZLE_STARTS) {
      session.puzzleStarted(puzzleId);
    }
  }

  private static String requirePuzzleId(String puzzleId) {
    if (puzzleId == null || puzzleId.isBlank()) {
      throw new IllegalArgumentException("puzzleId must not be blank");
    }
    return puzzleId.strip();
  }

  private record PersistenceFailure(Path path, String operatorEmail) {
    private PersistenceFailure {
      Objects.requireNonNull(path, "path");
      Objects.requireNonNull(operatorEmail, "operatorEmail");
    }
  }
}
