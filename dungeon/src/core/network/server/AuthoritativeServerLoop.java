package core.network.server;

import static core.network.config.NetworkConfig.FULL_SNAPSHOT_INTERVAL_TICKS;
import static core.network.config.NetworkConfig.SERVER_SNAPSHOT_HZ;
import static core.network.config.NetworkConfig.SERVER_TICK_HZ;

import contrib.entities.HeroBuilder;
import contrib.entities.HeroController;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.game.ECSManagement;
import core.game.PreRunConfiguration;
import core.level.Tile;
import core.level.loader.DungeonLoader;
import core.network.SnapshotTranslator;
import core.network.debug.SnapshotDebugger;
import core.network.messages.s2c.EntitySpawnEvent;
import core.network.messages.s2c.GameOverEvent;
import core.network.messages.s2c.LevelState;
import core.network.messages.s2c.SnapshotMessage;
import core.utils.logging.DungeonLogger;
import java.util.concurrent.*;

/**
 * The main server loop for the authoritative multiplayer server.
 *
 * <p>Handles ticking the game state, processing client inputs, and sending snapshots to clients at
 * configured rates.
 *
 * <p>Spawns hero entities for connected clients and removes them on disconnection.
 *
 * <p>Usage:
 *
 * <pre>
 *   ServerTransport serverTransport = new ServerTransport(port);
 *   AuthoritativeServerLoop serverLoop = new AuthoritativeServerLoop(serverTransport);
 *   serverLoop.start();
 *   // ... later when stopping the server
 *   serverLoop.stop();
 * </pre>
 *
 * @see ServerTransport The network transport used for communication.
 * @see HeroController The controller for hero entity actions.
 */
public final class AuthoritativeServerLoop {
  private static final DungeonLogger LOGGER =
      DungeonLogger.getLogger(AuthoritativeServerLoop.class);
  private static final boolean PRINT_RTT = false; // to debug latency issues

  private final ServerTransport net;
  private final ScheduledExecutorService executor;
  private volatile int serverTick = 0;

  /**
   * Creates a new AuthoritativeServerLoop with the given ServerTransport.
   *
   * @param netService The ServerTransport to use for network communication.
   */
  public AuthoritativeServerLoop(ServerTransport netService) {
    this.net = netService;
    this.executor =
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              Thread t = new Thread(r, "AuthoritativeServerLoop");
              t.setDaemon(true);
              return t;
            });
  }

  /**
   * Starts the server loop, initializing necessary components and scheduling tick and snapshot
   * tasks.
   */
  public void start() {
    PreRunConfiguration.frameRate(SERVER_TICK_HZ);
    PreRunConfiguration.userOnSetup().execute();

    try {
      DungeonLoader.afterAllLevels(
          () -> {
            Game.network().broadcast(new GameOverEvent("All levels completed"), true);
            Game.exit("Game Over");
          });
      DungeonLoader.loadLevel(0);
    } catch (Exception e) {
      LOGGER.warn("Failed to load initial level on server", e);
    }

    long tickPeriodMs = 1000L / SERVER_TICK_HZ;
    long snapshotPeriodMs = 1000L / SERVER_SNAPSHOT_HZ;

    executor.scheduleAtFixedRate(this::tick, 0, tickPeriodMs, TimeUnit.MILLISECONDS);
    executor.scheduleAtFixedRate(
        this::sendSnapshot, snapshotPeriodMs, snapshotPeriodMs, TimeUnit.MILLISECONDS);

    if (PRINT_RTT) {
      executor.scheduleAtFixedRate(
          () -> {
            for (ClientState state : net.connectedClients()) {
              LOGGER.debug(
                  "Client {}: RTT={}ms, lastActivity={}ms ago",
                  state.clientId(),
                  String.format("%.1f", state.rttEstimateMs()),
                  System.currentTimeMillis() - state.lastActivityTimeMs());
            }
          },
          1000,
          1000,
          TimeUnit.MILLISECONDS);
    }

    LOGGER.info("ServerLoop started: tickHz={}, snapshotHz={}", SERVER_TICK_HZ, SERVER_SNAPSHOT_HZ);
  }

  /** Stops the server loop, shutting down the executor service. */
  public void stop() {
    executor.shutdownNow();
    LOGGER.info("ServerLoop stopped");
  }

  /**
   * Checks if the server loop is currently running.
   *
   * @return true if running, false otherwise.
   */
  public boolean isRunning() {
    return !executor.isShutdown();
  }

  private void tick() {
    try {
      //noinspection NonAtomicOperationOnVolatileField only place where serverTick is modified
      serverTick++;
      // Drain any inbound network messages on the game thread before running systems
      try {
        Game.network().pollAndDispatch();
      } catch (Exception e) {
        LOGGER.warn("Error while polling network messages: " + e.getMessage());
      }
      syncClientsToEntities();
      PreRunConfiguration.userOnFrame().execute();
      ECSManagement.executeOneTick(core.System.AuthoritativeSide.SERVER);
    } catch (Exception e) {
      LOGGER.error("Tick error", e);
    } catch (Throwable t) {
      LOGGER.fatal("Unexpected error in server loop", t);
      stop();
    }
  }

  private void sendSnapshot() {
    net.connectedClients().forEach(this::sendSnapshotToClient);
  }

  /**
   * Sends a snapshot to a specific client. Decides whether to send a full snapshot or delta based
   * on the time since last full snapshot.
   *
   * @param clientState the client to send the snapshot to
   */
  private void sendSnapshotToClient(ClientState clientState) {
    int lastFullTick = clientState.lastFullSnapshotTick();
    boolean needsFullSnapshot =
        lastFullTick < 0 || (serverTick - lastFullTick) >= FULL_SNAPSHOT_INTERVAL_TICKS;

    if (needsFullSnapshot) {
      // Send full snapshot via TCP
      Game.network()
          .snapshotTranslator()
          .translateToSnapshot(serverTick)
          .ifPresent(
              snapshot -> {
                SnapshotMessage filteredSnapshot = snapshot.filterForRecipient(clientState);
                SnapshotDebugger.logFull(filteredSnapshot);
                Game.network().send(clientState.clientId(), filteredSnapshot, true);

                // Update client's cache with the full snapshot data
                clientState.lastFullSnapshotTick(serverTick);
                clientState.lastSentLevelState(LevelState.currentLevelStateFull());

                // Update entity state cache and track static vs mobile entities
                clientState.lastSentEntityStates().clear();
                clientState.lastVisibleEntityIds().clear();
                clientState.sentStaticEntityIds().clear();

                for (var entityState : filteredSnapshot.entities()) {
                  int entityId = entityState.entityId();
                  clientState.lastSentEntityStates().put(entityId, entityState);

                  // Classify entity as static or mobile
                  Game.findEntityById(entityId)
                      .ifPresent(
                          entity -> {
                            if (SnapshotTranslator.relevantForDelta(entity)) {
                              clientState.lastVisibleEntityIds().add(entityId);
                            } else {
                              clientState.sentStaticEntityIds().add(entityId);
                            }
                          });
                }
              });
    } else {
      // Send delta snapshot via UDP (with automatic TCP fallback)
      Game.network()
          .snapshotTranslator()
          .translateToDelta(serverTick, clientState)
          .ifPresent(
              delta -> {
                SnapshotDebugger.logDelta(delta);
                Game.network().send(clientState.clientId(), delta, false);
              });
    }
  }

  private void syncClientsToEntities() {
    // Spawn Hero if TCP client exists but no entity is associated
    for (ClientState state : net.connectedClients()) {
      if (state.playerEntity().isEmpty()) {
        state.playerEntity(spawnHeroForClient(state));
      }
    }
  }

  private Entity spawnHeroForClient(ClientState state) {
    Entity hero = HeroBuilder.builder().username(state.username()).isLocalPlayer(true).build();
    hero.fetch(PositionComponent.class)
        .ifPresent(
            pc ->
                pc.position(
                    Game.startTile()
                        .map(Tile::position)
                        .orElse(PositionComponent.ILLEGAL_POSITION)));
    // Add the hero to the game, after the client knows the id.
    Game.network()
        .send(state.clientId(), new EntitySpawnEvent(hero), true)
        .thenAccept(
            success -> {
              if (success) {
                Game.add(hero);
              } else {
                LOGGER.warn(
                    "Failed to send Hero's EntitySpawnEvent to client {}, not adding hero to game",
                    state);
              }
            });
    return hero;
  }
}
