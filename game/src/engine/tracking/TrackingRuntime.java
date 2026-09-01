package engine.tracking;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Engine lifecycle bridge. Room code should use {@link Tracking} instead. */
public final class TrackingRuntime {
  private TrackingRuntime() {}

  /**
   * Starts singleplayer tracking once the initial world and local player are ready.
   *
   * @param entityId local player entity to associate with the participant
   */
  public static void startSingleplayerSession(int entityId) {
    Tracking.startSingleplayerSession(entityId);
  }

  /**
   * Supplies the stable room ID announced by the server to this client.
   *
   * @param roomId stable room identifier
   */
  public static void clientRoomContext(String roomId) {
    Tracking.clientRoomContext(roomId);
  }

  /**
   * Returns the client-local played-before fact for the announced room.
   *
   * @return whether this client previously completed initial-world readiness for the room
   */
  public static boolean clientRoomPlayedBefore() {
    return Tracking.clientRoomPlayedBefore();
  }

  /** Marks the announced room played after InitialWorldReady was sent successfully. */
  public static void markClientRoomPlayed() {
    Tracking.markClientRoomPlayed();
  }

  /**
   * Records or resumes an anonymous participant after initial-world readiness.
   *
   * @param clientId transient network client ID
   * @param roomPlayedBefore client-local fact sent for this session
   * @return session-scoped anonymous participant
   */
  public static Optional<UUID> participantJoined(short clientId, boolean roomPlayedBefore) {
    return Tracking.participantJoined(clientId, roomPlayedBefore);
  }

  /**
   * Records a participant disconnect if tracking is still active.
   *
   * @param clientId transient network client ID
   */
  public static void participantLeft(short clientId) {
    Tracking.participantLeft(clientId);
  }

  /**
   * Associates a transient server entity with its session participant.
   *
   * @param clientId transient network client ID
   * @param entityId transient server entity ID
   */
  public static void associateEntity(short clientId, int entityId) {
    Tracking.associateEntity(clientId, entityId);
  }

  /** Aborts the active session at its current puzzle, if any. */
  public static void abortAtCurrentPuzzle() {
    Tracking.abortAtCurrentPuzzle();
  }

  /** Completes the active session normally. */
  public static void completed() {
    Tracking.completed();
  }

  /**
   * Returns whether a configured remote still lacks events or finish acknowledgement.
   *
   * @return whether remote tracking persistence remains incomplete
   */
  public static boolean remotePending() {
    return Tracking.remotePending();
  }

  /**
   * Checks whether the configured backend and its database are healthy without blocking the caller.
   *
   * @return empty when no backend is configured, otherwise the asynchronous health result
   */
  public static Optional<CompletableFuture<Boolean>> backendHealth() {
    return Tracking.backendHealth();
  }

  /** Displays or logs the recovery location when remote persistence is incomplete. */
  public static void warnIfRemotePending() {
    Tracking.warnIfRemotePending();
  }

  /**
   * Returns tracking configuration that must be forwarded to a child server through its
   * environment.
   *
   * @return immutable child-environment overrides
   */
  public static Map<String, String> childEnvironmentOverrides() {
    return TrackingConfig.childEnvironmentOverrides();
  }

  /**
   * Interprets an opaque managed-server status delivered to the hosting application.
   *
   * @param status opaque status payload from the managed server
   */
  public static void handleManagedServerStatus(String status) {
    Tracking.handleManagedServerStatus(status);
  }
}
