package escaperoom.foundation.multiplayer.bootstrap;

import engine.network.MessageDispatcher;
import engine.network.messages.s2c.EntitySpawnBatch;
import engine.network.messages.s2c.EntitySpawnEvent;
import engine.network.server.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Buffers ordinary initial entity spawns until one room-identity marker arrives.
 *
 * <p>All non-marker spawn events retain their ordinary order across batch boundaries. The normal
 * Dungeon initial-world completion and readiness handlers remain untouched.
 */
public final class ClientBootstrapCoordinator {
  private final MessageDispatcher dispatcher;
  private final BootstrapAction bootstrapAction;
  private final Consumer<String> failureAction;
  private final List<BufferedSpawn> bufferedSpawns = new ArrayList<>();
  private String establishedRoomInputSha256;
  private boolean installed;
  private boolean bootstrapComplete;
  private boolean terminalFailure;

  /**
   * Creates an injectable coordinator without installing its batch handler.
   *
   * @param dispatcher client message dispatcher
   * @param bootstrapAction local-room compatibility check and setup
   * @param failureAction bounded failure callback
   */
  public ClientBootstrapCoordinator(
      final MessageDispatcher dispatcher,
      final BootstrapAction bootstrapAction,
      final Consumer<String> failureAction) {
    this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
    this.bootstrapAction = Objects.requireNonNull(bootstrapAction, "bootstrapAction");
    this.failureAction = Objects.requireNonNull(failureAction, "failureAction");
  }

  /** Replaces only the ordinary initial entity-batch handler exactly once. */
  public synchronized void install() {
    if (!installed) {
      dispatcher.registerHandler(EntitySpawnBatch.class, this::onBatch);
      installed = true;
    }
  }

  /** Resets per-connection state while retaining the established room identity. */
  public synchronized void resetConnection() {
    bufferedSpawns.clear();
    bootstrapComplete = false;
    terminalFailure = false;
  }

  /**
   * Returns whether bootstrap completed successfully for the current connection.
   *
   * @return true after room compatibility validation and ordinary entity redispatch
   */
  public synchronized boolean bootstrapComplete() {
    return bootstrapComplete;
  }

  private synchronized void onBatch(final Session session, final EntitySpawnBatch batch) {
    if (terminalFailure) {
      return;
    }
    try {
      List<EntitySpawnEvent> events = List.copyOf(batch.entities());
      events.forEach(event -> Objects.requireNonNull(event, "Foundation bootstrap event"));
      if (!bootstrapComplete) {
        String roomInputSha256 = marker(events);
        if (roomInputSha256 == null) {
          events.forEach(event -> bufferedSpawns.add(new BufferedSpawn(session, event)));
          return;
        }
        completeBootstrap(session, events, roomInputSha256);
      } else if (events.stream().anyMatch(BootstrapMarker::isMarker)) {
        throw new IllegalArgumentException("duplicate Foundation bootstrap marker");
      } else {
        redispatch(session, events);
      }
    } catch (RuntimeException exception) {
      fail(boundedMessage(exception));
    }
  }

  private void completeBootstrap(
      final Session session,
      final List<EntitySpawnEvent> currentEvents,
      final String roomInputSha256) {
    if (establishedRoomInputSha256 != null && !establishedRoomInputSha256.equals(roomInputSha256)) {
      throw new IllegalArgumentException("Foundation room input identity changed on reconnect");
    }
    bootstrapAction.apply(roomInputSha256);
    establishedRoomInputSha256 = roomInputSha256;
    bootstrapComplete = true;
    List<BufferedSpawn> pendingSpawns = List.copyOf(bufferedSpawns);
    bufferedSpawns.clear();
    pendingSpawns.forEach(spawn -> dispatcher.dispatch(spawn.session(), spawn.event()));
    redispatch(session, currentEvents);
  }

  private static String marker(final List<EntitySpawnEvent> events) {
    String roomInputSha256 = null;
    for (EntitySpawnEvent event : events) {
      if (!BootstrapMarker.isMarker(event)) {
        continue;
      }
      if (roomInputSha256 != null) {
        throw new IllegalArgumentException("duplicate Foundation bootstrap marker");
      }
      roomInputSha256 = BootstrapMarker.decode(event);
    }
    return roomInputSha256;
  }

  private void redispatch(final Session session, final List<EntitySpawnEvent> events) {
    events.stream()
        .filter(event -> !BootstrapMarker.isMarker(event))
        .forEach(event -> dispatcher.dispatch(session, event));
  }

  private void fail(final String reason) {
    terminalFailure = true;
    bootstrapComplete = false;
    bufferedSpawns.clear();
    failureAction.accept(reason);
  }

  private static String boundedMessage(final RuntimeException exception) {
    String message = exception.getMessage();
    if (message == null || message.isBlank()) {
      return "Foundation bootstrap failed";
    }
    return message.length() <= 512 ? message : message.substring(0, 512);
  }

  private record BufferedSpawn(Session session, EntitySpawnEvent event) {}

  /** Local-room compatibility and setup callback run before ordinary redispatch. */
  @FunctionalInterface
  public interface BootstrapAction {
    /**
     * Validates the host identity against the locally derived room and completes local setup.
     *
     * @param roomInputSha256 canonical complete DEER-project identity
     */
    void apply(String roomInputSha256);
  }
}
