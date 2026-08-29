package engine.tracking;

import engine.Game;
import engine.game.ServerProcess;
import engine.utils.logging.DungeonLogger;
import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import tracking.core.TrackingEvent;
import tracking.core.TrackingSessionStatus;

/**
 * Public tracking API for generated and hand-written rooms.
 *
 * <p>Configure tracking before {@link Game#run()}. Only the authoritative server or singleplayer
 * process starts a session. Room code submits facts through this class; the engine assigns
 * sequence, event ID, wall-clock time, and monotonic elapsed time.
 */
public final class Tracking {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(Tracking.class);
  private static final Object LOCK = new Object();

  private static TrackingConfig explicitConfig;
  private static TrackingSession session;
  private static String clientRoomId;
  private static PersistenceFailure persistenceFailure;

  private Tracking() {}

  /**
   * Sets explicit configuration. Call this before {@link Game#run()}.
   *
   * @param config complete room and deployment configuration
   */
  public static void configure(TrackingConfig config) {
    synchronized (LOCK) {
      if (session != null && !session.finished()) {
        throw new IllegalStateException("Tracking is already running");
      }
      session = null;
      persistenceFailure = null;
      explicitConfig = Objects.requireNonNull(config, "config");
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
      return session != null && !session.finished();
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
   * Records the start of a puzzle and makes it the current puzzle for abort reporting.
   *
   * @param puzzleId stable room-local puzzle identifier
   * @return newly recorded event, or empty when inactive or already recorded
   */
  public static Optional<TrackingEvent> puzzleStarted(String puzzleId) {
    synchronized (LOCK) {
      if (session == null || session.finished()) {
        return Optional.empty();
      }
      try {
        return session.puzzleStarted(puzzleId);
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
   * @param participantId session-scoped anonymous participant
   * @return newly recorded event, or empty when inactive
   */
  public static Optional<TrackingEvent> attempt(
      String puzzleId,
      String objectId,
      String answerKind,
      String rawAnswer,
      boolean correct,
      UUID participantId) {
    synchronized (LOCK) {
      if (session == null || session.finished()) {
        return Optional.empty();
      }
      try {
        return Optional.of(
            session.attempt(puzzleId, objectId, answerKind, rawAnswer, correct, participantId));
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
   * @param participantId session-scoped anonymous participant
   * @return newly recorded event, or empty when inactive or already recorded
   */
  public static Optional<TrackingEvent> hintUsed(
      String puzzleId, String hintId, UUID participantId) {
    synchronized (LOCK) {
      if (session == null || session.finished()) {
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
      if (session == null || session.finished()) {
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
   * Returns the anonymous participant mapped to a runtime network client.
   *
   * @param clientId transient network client ID
   * @return session-scoped anonymous participant
   */
  public static Optional<UUID> participantForClient(short clientId) {
    synchronized (LOCK) {
      return session == null ? Optional.empty() : session.participantForClient(clientId);
    }
  }

  /**
   * Returns the anonymous participant mapped to a runtime player entity.
   *
   * @param entityId transient server entity ID
   * @return session-scoped anonymous participant
   */
  public static Optional<UUID> participantForEntity(int entityId) {
    synchronized (LOCK) {
      return session == null ? Optional.empty() : session.participantForEntity(entityId);
    }
  }

  /** Ends the session normally. Repeated calls do nothing. */
  public static void completed() {
    finish(TrackingSessionStatus.COMPLETED, Optional.empty());
  }

  static void startAuthoritativeSession() {
    synchronized (LOCK) {
      if ((session != null && !session.finished())
          || (!Game.isSingleplayer() && !Game.network().isServer())) {
        return;
      }
      session = null;
      config()
          .ifPresent(
              configured -> {
                try {
                  session = new TrackingSession(configured);
                  if (Game.isSingleplayer()) {
                    boolean playedBefore = RoomHistory.playedBefore(configured.roomId());
                    UUID participant =
                        session.participantJoined((short) 0, playedBefore).orElseThrow();
                    Game.player()
                        .ifPresent(player -> session.associateEntity((short) 0, player.id()));
                    RoomHistory.markPlayed(configured.roomId());
                    LOGGER.info("Started local tracking participant {}", participant);
                  }
                } catch (TrackingPersistenceException exception) {
                  recordPersistenceFailure(exception, configured.operatorContact());
                } catch (RuntimeException exception) {
                  LOGGER.error("Could not start tracking", exception);
                }
              });
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
      if (session == null || session.finished()) {
        return Optional.empty();
      }
      try {
        return session.participantJoined(clientId, roomPlayedBefore);
      } catch (TrackingPersistenceException exception) {
        recordPersistenceFailure(exception);
        return Optional.empty();
      }
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

  static void associateEntity(short clientId, int entityId) {
    synchronized (LOCK) {
      if (session != null && !session.finished()) {
        session.associateEntity(clientId, entityId);
      }
    }
  }

  /** Ends a running session as aborted and waits briefly for ordered local persistence. */
  public static void abort() {
    abortAtCurrentPuzzle();
  }

  static void abortAtCurrentPuzzle() {
    synchronized (LOCK) {
      if (session != null) {
        try {
          session.finish(TrackingSessionStatus.ABORTED, session.currentPuzzleId());
        } catch (TrackingPersistenceException exception) {
          recordPersistenceFailure(exception);
        }
      }
    }
  }

  static boolean remotePending() {
    synchronized (LOCK) {
      return persistenceFailure != null || (session != null && session.remotePending());
    }
  }

  static Optional<String> operatorContact() {
    synchronized (LOCK) {
      if (session != null) {
        return session.operatorContact();
      }
      return Optional.ofNullable(persistenceFailure).flatMap(PersistenceFailure::operatorContact);
    }
  }

  static void warnIfRemotePending() {
    synchronized (LOCK) {
      if (persistenceFailure == null && (session == null || !session.remotePending())) {
        return;
      }
      Path path = persistenceFailure != null ? persistenceFailure.path() : session.outboxPath();
      Optional<String> operatorContact =
          session != null ? session.operatorContact() : persistenceFailure.operatorContact();
      String contact = operatorContact.map(value -> " Send it to " + value + ".").orElse("");
      LOGGER.warn(
          "Tracking persistence is not confirmed. Keep the local data at {}.{}", path, contact);
      if (ServerProcess.reportTrackingUploadPending(path, operatorContact)) {
        return;
      }
      if (GraphicsEnvironment.isHeadless()) {
        return;
      }
      String message =
          "Tracking persistence is not confirmed.\nKeep the local data at:\n"
              + path
              + operatorContact.map(value -> "\n\nOperator contact: " + value).orElse("");
      try {
        SwingUtilities.invokeLater(
            () -> {
              try {
                JOptionPane.showMessageDialog(
                    null, message, "Tracking upload pending", JOptionPane.WARNING_MESSAGE);
              } catch (RuntimeException exception) {
                LOGGER.warn("Could not display tracking upload warning for {}", path, exception);
              }
            });
      } catch (RuntimeException exception) {
        LOGGER.warn("Could not schedule tracking upload warning for {}", path, exception);
      }
    }
  }

  private static void finish(TrackingSessionStatus status, Optional<String> puzzleId) {
    synchronized (LOCK) {
      if (session != null) {
        try {
          session.finish(status, puzzleId);
        } catch (TrackingPersistenceException exception) {
          recordPersistenceFailure(exception);
        }
      }
    }
  }

  private static void recordPersistenceFailure(TrackingPersistenceException exception) {
    Optional<String> contact = session == null ? Optional.empty() : session.operatorContact();
    recordPersistenceFailure(exception, contact);
  }

  private static void recordPersistenceFailure(
      TrackingPersistenceException exception, Optional<String> operatorContact) {
    persistenceFailure = new PersistenceFailure(exception.outboxPath(), operatorContact);
    LOGGER.error(
        "Local tracking persistence failed at {}. Gameplay will continue; tracking data may be incomplete.",
        exception.path(),
        exception);
  }

  private static Optional<TrackingConfig> config() {
    return Optional.ofNullable(explicitConfig).or(TrackingConfig::fromEnvironment);
  }

  private record PersistenceFailure(Path path, Optional<String> operatorContact) {
    private PersistenceFailure {
      Objects.requireNonNull(path, "path");
      Objects.requireNonNull(operatorContact, "operatorContact");
    }
  }
}
