package core.network.server;

import static core.network.config.NetworkConfig.FULL_SNAPSHOT_INTERVAL_TICKS;
import static core.network.config.NetworkConfig.FULL_SNAPSHOT_RECOVERY_RETRY_INTERVAL_TICKS;
import static core.network.config.NetworkConfig.SERVER_DELTA_HISTORY_SIZE;
import static core.network.config.NetworkConfig.SERVER_DELTA_SNAPSHOT_HZ;
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
import core.network.FullSnapshotSendReason;
import core.network.NetworkTelemetry;
import core.network.delta.SnapshotDeltaCompressor;
import core.network.delta.SnapshotHistory;
import core.network.messages.s2c.DeltaSnapshotMessage;
import core.network.messages.s2c.EntitySpawnEvent;
import core.network.messages.s2c.GameOverEvent;
import core.network.messages.s2c.SnapshotMessage;
import core.utils.Point;
import core.utils.logging.DungeonLogger;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
  private final SnapshotHistory snapshotHistory = new SnapshotHistory(SERVER_DELTA_HISTORY_SIZE);
  private volatile int serverTick = 0;
  private String snapshotLevelName;

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

    DungeonLoader.afterAllLevels(
        () -> {
          Game.network().broadcast(new GameOverEvent("All levels completed"), true);
          Game.exit("Game Over");
        });

    long tickPeriodMs = 1000L / SERVER_TICK_HZ;
    long snapshotPeriodMs = 1000L / SERVER_DELTA_SNAPSHOT_HZ;

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

    LOGGER.info(
        "ServerLoop started: tickHz={}, fullSnapshotHz={}, deltaSnapshotHz={}",
        SERVER_TICK_HZ,
        FULL_SNAPSHOT_INTERVAL_TICKS,
        SERVER_DELTA_SNAPSHOT_HZ);
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
      long tickStartNanos = System.nanoTime();
      //noinspection NonAtomicOperationOnVolatileField only place where serverTick is modified
      serverTick++;
      // Drain any inbound network messages on the game thread before running systems
      long networkDispatchStartNanos = System.nanoTime();
      try {
        Game.network().pollAndDispatch();
      } catch (Exception e) {
        LOGGER.warn("Error while polling network messages: " + e.getMessage());
      } finally {
        NetworkTelemetry.recordNetworkDispatchBatch(System.nanoTime() - networkDispatchStartNanos);
      }
      syncClientsToEntities();
      PreRunConfiguration.userOnFrame().execute();
      ECSManagement.executeOneTick(core.System.AuthoritativeSide.SERVER);
      NetworkTelemetry.recordFrameTime(System.nanoTime() - tickStartNanos);
    } catch (Exception e) {
      LOGGER.error("Tick error", e);
    } catch (Throwable t) {
      LOGGER.fatal("Unexpected error in server loop", t);
      stop();
    }
  }

  private void sendSnapshot() {
    Set<ClientState> clients = net.connectedClients();
    if (clients.isEmpty()) {
      return;
    }
    clearSnapshotBaselinesOnLevelChange(clients);

    long buildStartNanos = System.nanoTime();
    Game.network()
        .snapshotTranslator()
        .translateToSnapshot(serverTick)
        .ifPresent(
            snapshot -> {
              NetworkTelemetry.recordSnapshotBuild(System.nanoTime() - buildStartNanos);
              snapshotHistory.add(snapshot, protectedSnapshotTicks(clients));
              NetworkTelemetry.recordSnapshotHistory(
                  snapshot.serverTick(),
                  snapshotHistory.size(),
                  snapshotHistory.capacity(),
                  SERVER_TICK_HZ);
              clients.forEach(client -> sendSnapshotToClient(client, snapshot));
            });
  }

  private void sendSnapshotToClient(ClientState client, SnapshotMessage currentSnapshot) {
    ClientSnapshotSyncState snapshotSync = client.snapshotSync();
    int ackTick = snapshotSync.lastAckedSnapshotTick();
    boolean hasAck = snapshotSync.hasAck();
    boolean baselineInHistory = hasAck && snapshotHistory.contains(ackTick);
    NetworkTelemetry.recordBaselineHealth(
        client.clientId(),
        currentSnapshot.serverTick(),
        ackTick,
        baselineInHistory,
        snapshotHistory.capacity(),
        SERVER_TICK_HZ);

    if (!hasAck) {
      if (snapshotSync.fullSnapshotRecoveryDue(
          currentSnapshot.serverTick(), FULL_SNAPSHOT_RECOVERY_RETRY_INTERVAL_TICKS)) {
        sendFullSnapshot(client, currentSnapshot, snapshotSync.pendingFullSnapshotReason());
        snapshotSync.pendingFullSnapshotReason(FullSnapshotSendReason.NO_ACK);
      }
      return;
    }

    if (snapshotSync.fullSnapshotDue(currentSnapshot.serverTick(), FULL_SNAPSHOT_INTERVAL_TICKS)) {
      sendFullSnapshot(client, currentSnapshot, FullSnapshotSendReason.PERIODIC_BASELINE);
      return;
    }

    Optional<SnapshotMessage> baseSnapshot = snapshotHistory.snapshot(ackTick);
    if (baseSnapshot.isEmpty()) {
      if (snapshotSync.fullSnapshotRecoveryDue(
          currentSnapshot.serverTick(), FULL_SNAPSHOT_RECOVERY_RETRY_INTERVAL_TICKS)) {
        sendFullSnapshot(client, currentSnapshot, FullSnapshotSendReason.MISSING_BASELINE_HISTORY);
      }
      return;
    }

    SnapshotMessage baseline = baseSnapshot.orElseThrow();
    client.ensureKnownSnapshotEntityIdsForBaseline(baseline);
    SnapshotDeltaCompressor.compress(baseline, currentSnapshot, client.knownSnapshotEntityIds())
        .ifPresent(
            delta -> {
              client.trackKnownSnapshotEntityIds(
                  delta.entityDeltas().stream()
                      .map(entityDelta -> entityDelta.entityId())
                      .toList());
              sendDeltaSnapshot(client, delta);
            });
  }

  private Set<Integer> protectedSnapshotTicks(Set<ClientState> clients) {
    Set<Integer> protectedTicks = new HashSet<>();
    clients.forEach(
        client -> protectedTicks.addAll(client.snapshotSync().protectedSnapshotTicks()));
    return Set.copyOf(protectedTicks);
  }

  private void sendDeltaSnapshot(ClientState client, DeltaSnapshotMessage delta) {
    client.snapshotSync().markDeltaSnapshotSent(delta.serverTick());
    Game.network().send(client.clientId(), delta, false);
  }

  private void sendFullSnapshot(
      ClientState client, SnapshotMessage currentSnapshot, FullSnapshotSendReason reason) {
    ClientSnapshotSyncState snapshotSync = client.snapshotSync();
    snapshotSync.markFullSnapshotScheduled(currentSnapshot.serverTick());
    NetworkTelemetry.recordFullSnapshotScheduled(
        client.clientId(), currentSnapshot.serverTick(), currentSnapshot.entities().size(), reason);
    Game.network()
        .send(client.clientId(), currentSnapshot, true)
        .thenAccept(
            success -> {
              if (success) {
                snapshotSync.markFullSnapshotSent(currentSnapshot.serverTick());
              } else {
                snapshotSync.markFullSnapshotSendFailed(currentSnapshot.serverTick());
              }
            });
  }

  private void clearSnapshotBaselinesOnLevelChange(Set<ClientState> clients) {
    Optional<String> currentLevel = currentLevelName();
    if (currentLevel.isEmpty()) {
      return;
    }
    String levelName = currentLevel.orElseThrow();
    if (!levelName.equals(snapshotLevelName)) {
      snapshotHistory.clear();
      clients.forEach(client -> client.clearSnapshotBaseline(FullSnapshotSendReason.LEVEL_CHANGE));
      snapshotLevelName = levelName;
    }
  }

  private Optional<String> currentLevelName() {
    try {
      return Optional.of(DungeonLoader.currentLevel());
    } catch (IndexOutOfBoundsException e) {
      return Optional.empty();
    }
  }

  private void syncClientsToEntities() {
    // Spawn Hero if TCP client exists but no entity is associated
    for (ClientState state : net.establishedClients()) {
      if (state.playerEntity().isEmpty()) {
        state.playerEntity(spawnHeroForClient(state));
      }
    }
  }

  private Entity spawnHeroForClient(ClientState state) {
    Entity hero =
        HeroBuilder.builder()
            .username(state.username())
            .characterClass(state.characterClass())
            .isLocalPlayer(true)
            .build();
    hero.fetch(PositionComponent.class)
        .ifPresent(pc -> pc.position(Game.startTile().map(Tile::position).orElse(new Point(0, 0))));
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
