package core.network.handler;

import contrib.components.HealthComponent;
import contrib.entities.HeroController;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.network.ConnectionListener;
import core.network.MessageDispatcher;
import core.network.NetworkException;
import core.network.SnapshotTranslator;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.InputMessage;
import core.network.messages.s2c.EntityState;
import core.network.messages.s2c.SnapshotMessage;
import core.utils.Vector2;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A mock network handler for single-player games.
 *
 * <p>This handler processes game logic locally without real network communication, acting as both
 * client and server to keep architecture consistent between modes.
 *
 * <p>It uses a {@link MessageDispatcher} to route incoming messages and a raw message consumer to
 * send game state updates to the game loop.
 *
 * <p><b>Usage:</b>
 *
 * <ol>
 *   <li>Initialize with {@link #initialize(boolean, String, int, String)} .
 *   <li>Start processing with {@link #start()}.
 *   <li>Send client inputs via {@link #sendInput(InputMessage)} via UDP .
 *   <li>Or send raw messages via {@link #send(NetworkMessage)} via TCP.
 *   <li>Trigger state updates with {@link #triggerStateUpdate()}.
 *   <li>Stop the handler with {@link #shutdown()}.
 * </ol>
 *
 * <p><b>Note:</b> Not suitable for multiplayer scenarios.
 */
public class LocalNetworkHandler implements INetworkHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(LocalNetworkHandler.class);

  private final MessageDispatcher dispatcher = new MessageDispatcher(); // Instantiate dispatcher
  private BiConsumer<ChannelHandlerContext, NetworkMessage>
      rawMessageConsumer; // Internal consumer for raw messages
  private boolean isRunning = false;
  private boolean isInitialized = false;
  private final List<ConnectionListener> connectionListeners = new ArrayList<>();
  private volatile SnapshotTranslator translator;

  // No per-field delta tracking; we emit compact snapshots for local mode as well.

  @Override
  public void initialize(boolean isServer, String serverAddress, int port, String username)
      throws NetworkException {
    this.isInitialized = true;
  }

  @Override
  public void send(NetworkMessage message) {
    // For local processing, we don't send anything
  }

  @Override
  public void sendInput(InputMessage input) {
    // Apply same semantics as the authoritative server to the local hero entity
    Game.hero()
        .ifPresent(
            hero -> {
              switch (input.action()) {
                case MOVE:
                  HeroController.moveHero(hero, Vector2.of(input.point()).direction());
                  break;
                case MOVE_PATH:
                  HeroController.moveHeroPath(hero, input.point());
                  break;
                case CAST_SKILL:
                  HeroController.useSkill(hero, 0, input.point());
                  break;
                case INTERACT:
                  HeroController.interact(hero, input.point());
                  break;
              }
            });
  }

  @Override
  public void start() {
    if (!isInitialized) {
      LOGGER.error("LocalNetworkHandler cannot start because it is not initialized.");
      return;
    }
    this.isRunning = true;
    LOGGER.info("LocalNetworkHandler started.");
    notifyConnected();
  }

  @Override
  public void shutdown(String reason) {
    this.isRunning = false;
    this.isInitialized = false;
    notifyDisconnected(new NetworkException(reason));
    LOGGER.info("LocalNetworkHandler shutdown complete. Reason: {}", reason);
  }

  @Override
  public boolean isConnected() {
    return isRunning && isInitialized;
  }

  @Override
  public boolean isServer() {
    // In single player context managed by this handler, it acts as the authority.
    return true;
  }

  @Override
  public MessageDispatcher messageDispatcher() {
    return dispatcher;
  }

  @Override
  public void _setRawMessageConsumer(
      BiConsumer<ChannelHandlerContext, NetworkMessage> rawMessageConsumer) {
    this.rawMessageConsumer = rawMessageConsumer;
  }

  /**
   * Returns the configured SnapshotTranslator or throws if not set.
   *
   * <p>Explicit injection required: callers must set a translator before use.
   */
  @Override
  public SnapshotTranslator snapshotTranslator() {
    SnapshotTranslator t = translator;
    if (t == null)
      throw new IllegalStateException(
          "SnapshotTranslator not set on INetworkHandler. Set via setSnapshotTranslator(...) before starting network or provide translator in starter.");
    return t;
  }

  /** Sets the SnapshotTranslator to be used by this handler. */
  @Override
  public void setSnapshotTranslator(SnapshotTranslator translator) {
    if (translator != null) this.translator = translator;
  }

  @Override
  public void pollAndDispatch() {
    // Local handler processes immediately; nothing to drain.
  }

  @Override
  public synchronized void addConnectionListener(ConnectionListener listener) {
    if (listener == null) return;
    connectionListeners.add(listener);
  }

  @Override
  public synchronized void removeConnectionListener(ConnectionListener listener) {
    if (listener == null) return;
    connectionListeners.remove(listener);
  }

  private void notifyConnected() {
    List<ConnectionListener> snapshot;
    synchronized (this) {
      snapshot = new ArrayList<>(connectionListeners);
    }
    for (ConnectionListener listener : snapshot) {
      try {
        listener.onConnected();
      } catch (Exception e) {
        LOGGER.warn("ConnectionListener.onConnected threw", e);
      }
    }
  }

  private void notifyDisconnected(Throwable reason) {
    List<ConnectionListener> snapshot;
    synchronized (this) {
      snapshot = new ArrayList<>(connectionListeners);
    }
    for (ConnectionListener listener : snapshot) {
      try {
        listener.onDisconnected(reason);
      } catch (Exception e) {
        LOGGER.warn("ConnectionListener.onDisconnected threw", e);
      }
    }
  }

  /**
   * Collects the current state of relevant entities and sends it to the state update listener.
   *
   * <p>This simulates the server sending periodic state updates. This method should be called by
   * the game loop.
   */
  public void triggerStateUpdate() {
    if (!isRunning || !isInitialized || rawMessageConsumer == null) {
      LOGGER.debug(
          "LocalNetworkHandler not ready to send updates. Running: {}, Init: {}, Consumer: {}",
          isRunning,
          isInitialized,
          (rawMessageConsumer != null));
      return;
    }

    List<EntityState> snapshotEntities = new ArrayList<>();
    Game.levelEntities()
        .forEach(
            entity -> {
              EntityState.Builder builder = EntityState.builder();
              builder.entityId(entity.id());

              // PositionComponent
              Optional<PositionComponent> pcOpt = entity.fetch(PositionComponent.class);
              if (pcOpt.isEmpty()) return; // only include entities with a position
              builder.position(pcOpt.get().position());
              builder.viewDirection(pcOpt.get().viewDirection());

              // HealthComponent
              entity
                  .fetch(HealthComponent.class)
                  .ifPresent(
                      hc -> {
                        builder.currentHealth(hc.currentHealthpoints());
                        builder.maxHealth(hc.maximalHealthpoints());
                      });

              // DrawComponent
              entity
                  .fetch(DrawComponent.class)
                  .ifPresent(dc -> builder.animation(dc.currentAnimationName()));

              snapshotEntities.add(builder.build());
            });

    rawMessageConsumer.accept(null, new SnapshotMessage(0L, snapshotEntities));
  }
}
