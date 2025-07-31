package core.network;

import contrib.components.HealthComponent;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.network.messages.NetworkMessage;
import core.network.messages.client2server.ClientMessage;
import core.network.messages.server2client.DrawUpdate;
import core.network.messages.server2client.HealthUpdate;
import core.network.messages.server2client.PositionUpdate;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

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
 *   <li>Initialize with {@link #initialize(boolean, String, int)}.
 *   <li>Start processing with {@link #start()}.
 *   <li>Send client messages via {@link #sendToServer(ClientMessage)}.
 *   <li>Trigger state updates with {@link #triggerStateUpdate()}.
 *   <li>Stop the handler with {@link #shutdown()}.
 * </ol>
 *
 * <p><b>Note:</b> Not suitable for multiplayer scenarios.
 */
public class LocalNetworkHandler implements INetworkHandler {

  private static final Logger LOGGER = Logger.getLogger(LocalNetworkHandler.class.getName());

  private final MessageDispatcher dispatcher = new MessageDispatcher(); // Instantiate dispatcher
  private Consumer<NetworkMessage> rawMessageConsumer; // Internal consumer for raw messages
  private boolean isRunning = false;
  private boolean isInitialized = false;

  private final Map<Integer, Integer> lastKnownHealth = new HashMap<>();
  private final Map<Integer, String> lastKnownAnimation = new HashMap<>();

  @Override
  public void initialize(boolean isServer, String serverAddress, int port) throws NetworkException {
    // No actual network initialization needed for local handler.
    // We can use the parameters for potential future logging or mock setup if needed.
    this.isInitialized = true;
    LOGGER.info(
        "LocalNetworkHandler initialized. (Server: "
            + isServer
            + ", Address: "
            + serverAddress
            + ", Port: "
            + port
            + ")");
  }

  @Override
  public void sendToServer(ClientMessage message) {
    if (!isRunning || !isInitialized) {
      LOGGER.warning("LocalNetworkHandler not running or initialized, ignoring sendHeroMovement.");
      return;
    }

    message.process();
  }

  @Override
  public void start() {
    if (!isInitialized) {
      LOGGER.severe("LocalNetworkHandler cannot start because it is not initialized.");
      return;
    }
    this.isRunning = true;
    LOGGER.info("LocalNetworkHandler started.");
  }

  @Override
  public void shutdown() {
    this.isRunning = false;
    this.isInitialized = false;
    LOGGER.info("LocalNetworkHandler shutdown.");
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
  public void _setRawMessageConsumer(Consumer<NetworkMessage> rawMessageConsumer) {
    this.rawMessageConsumer = rawMessageConsumer;
  }

  /**
   * Collects the current state of relevant entities and sends it to the state update listener.
   *
   * <p>This simulates the server sending periodic state updates. This method should be called by
   * the game loop.
   */
  public void triggerStateUpdate() {
    if (!isRunning || !isInitialized || rawMessageConsumer == null) {
      LOGGER.info(
          "LocalNetworkHandler not ready to send updates. Running: "
              + isRunning
              + ", Init: "
              + isInitialized
              + ", Consumer: "
              + (rawMessageConsumer != null));
      return;
    }

    Set<Integer> activeEntityIds = new HashSet<>();

    Game.entityStream()
        .forEach(
            entity -> {
              int entityId = entity.id();
              activeEntityIds.add(entityId);

              // Always send position data
              entity
                  .fetch(PositionComponent.class)
                  .ifPresent(
                      pc -> {
                        PositionUpdate posUpdate =
                            new PositionUpdate(entityId, pc.position(), pc.viewDirection());
                        rawMessageConsumer.accept(posUpdate);
                      });

              // Send health data
              entity
                  .fetch(HealthComponent.class)
                  .ifPresent(
                      hc -> {
                        int currentHealth = hc.currentHealthpoints();
                        if (!lastKnownHealth
                            .getOrDefault(entityId, Integer.MIN_VALUE)
                            .equals(currentHealth)) {
                          HealthUpdate healthUpdate =
                              new HealthUpdate(entityId, currentHealth, hc.maximalHealthpoints());
                          rawMessageConsumer.accept(healthUpdate);
                          lastKnownHealth.put(entityId, currentHealth);
                        }
                      });

              entity
                  .fetch(DrawComponent.class)
                  .ifPresent(
                      dc -> {
                        String currentAnimation = dc.currentAnimationName();
                        if (!lastKnownAnimation
                            .getOrDefault(entityId, "")
                            .equals(currentAnimation)) {
                          DrawUpdate drawUpdate =
                              new DrawUpdate(entityId, currentAnimation, dc.tintColor());
                          rawMessageConsumer.accept(drawUpdate);
                          lastKnownAnimation.put(entityId, currentAnimation);
                        }
                      });
            });
    lastKnownHealth.keySet().retainAll(activeEntityIds);
  }
}
