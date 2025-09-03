package core.network.server;

import contrib.entities.HeroController;
import contrib.entities.HeroFactory;
import contrib.systems.*;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.game.ECSManagment;
import core.game.ECSTickRunner;
import core.game.PreRunConfiguration;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.network.SnapshotTranslator;
import core.network.messages.*;
import core.network.messages.c2s.InputMessage;
import core.network.messages.s2c.GameOverEvent;
import core.network.messages.s2c.LevelChangeEvent;
import core.systems.*;
import core.utils.Tuple;
import core.utils.Vector2;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal authoritative server loop that consumes inputs and broadcasts world snapshots.
 *
 * <p>Prototype: uses a simple in-memory entity map keyed by clientId; positions updated by inputs.
 */
public final class AuthoritativeServerLoop {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthoritativeServerLoop.class);
  private static final int TICK_HZ = 20;
  private static final int snapshotHz = 20;

  private final ServerNetworkService net;
  private final ScheduledExecutorService executor;

  // Authoritative entities simulated with ECS; map clientId -> entity
  private final ConcurrentHashMap<Integer, Entity> clientEntities = new ConcurrentHashMap<>();

  // Tick counters
  private volatile long serverTick = 0;

  // translator comes from network handler
  private final SnapshotTranslator translator;

  /**
   * Creates an authoritative server loop.
   *
   * @param netService server network service
   * @param translator explicit snapshot translator (required)
   */
  public AuthoritativeServerLoop(ServerNetworkService netService, SnapshotTranslator translator) {
    this.net = netService;
    this.translator = java.util.Objects.requireNonNull(translator, "translator");
    this.executor =
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              Thread t = new Thread(r, "AuthoritativeServerLoop");
              t.setDaemon(true);
              return t;
            });
  }

  /** Starts the simulation and snapshot senders. */
  public void start() {
    try {
      PreRunConfiguration.frameRate(TICK_HZ);
    } catch (Throwable ignored) {
    }

    createSystems();
    // Load initial level and notify clients once
    try {
      DungeonLoader.afterAllLevels(() -> broadcast(new GameOverEvent()));
      DungeonLoader.addLevel(Tuple.of("maze", DungeonLevel.class));
      DungeonLoader.loadLevel(0);
      broadcastLevelChange();
    } catch (Exception e) {
      LOGGER.warn("Failed to load initial level on server", e);
    }

    long tickPeriodMs = 1000L / TICK_HZ;
    long snapshotPeriodMs = 1000L / snapshotHz;

    executor.scheduleAtFixedRate(this::tick, 0, tickPeriodMs, TimeUnit.MILLISECONDS);
    executor.scheduleAtFixedRate(
        this::sendSnapshot, snapshotPeriodMs, snapshotPeriodMs, TimeUnit.MILLISECONDS);
    LOGGER.info("AuthoritativeServerLoop started: tickHz={}, snapshotHz={}", TICK_HZ, snapshotHz);
  }

  private void createSystems() {
    ECSManagment.add(new PositionSystem());
    ECSManagment.add(new LevelSystem(() -> {}));
    ECSManagment.add(new VelocitySystem());
    ECSManagment.add(new FrictionSystem());
    ECSManagment.add(new MoveSystem());
    DrawSystem.ALLOW_RENDER = false; // Only update components, not render
    ECSManagment.add(new DrawSystem());
    ECSManagment.add(new ProjectileSystem());
    ECSManagment.add(new HealthSystem());
    ECSManagment.add(new PathSystem());
    ECSManagment.add(new AISystem());
    ECSManagment.add(new CollisionSystem());
    ECSManagment.add(new FallingSystem());
  }

  /** Stops the simulation. */
  public void stop() {
    executor.shutdownNow();
    LOGGER.info("AuthoritativeServerLoop stopped");
  }

  private void tick() {
    try {
      serverTick++;
      syncClientsToEntities();
      drainAndApplyInputs(net.inputQueue());
      ECSTickRunner.runOneFrame(s -> true);
    } catch (Exception e) {
      LOGGER.warn("Tick error", e);
    }
  }

  private void drainAndApplyInputs(ConcurrentLinkedQueue<InputMessage> queue) {
    InputMessage msg;
    while ((msg = queue.poll()) != null) {
      int clientId = msg.clientId();
      Entity playerEntity = clientEntities.get(clientId);
      if (playerEntity == null) continue;
      LOGGER.info("" + "Received input message: " + msg);
      switch (msg.action()) {
        case MOVE:
          HeroController.moveHero(playerEntity, Vector2.of(msg.point()).direction());
          break;
        case MOVE_PATH:
          HeroController.moveHeroPath(playerEntity, msg.point());
          break;
        case CAST_SKILL:
          HeroController.useSkill(playerEntity, 0, msg.point());
          break;
        case INTERACT:
          HeroController.interact(playerEntity, msg.point());
          break;
      }
    }
  }

  private void sendSnapshot() {
    translator.translateToSnapshot(serverTick, clientEntities).ifPresent(this::broadcast);
  }

  private void broadcastLevelChange() {
    String levelName;
    try {
      levelName = DungeonLoader.currentLevel();
    } catch (Throwable t) {
      LOGGER.warn("Failed to broadcast current level", t);
      return;
    }
    LOGGER.info("Broadcasting level change to clients: {}", levelName);
    // TODO: for now, we use null as the spawn point, so the client spawns at the start tile
    broadcast(new LevelChangeEvent(levelName, null));
  }

  public void broadcast(NetworkMessage event) {
    for (Map.Entry<Integer, InetSocketAddress> entry : net.udpClients().entrySet()) {
      net.sendUdpObject(entry.getValue(), event);
    }
  }

  private void syncClientsToEntities() {
    // Create entities for new clients
    for (Integer clientId : net.tcpClientMap().values()) {
      clientEntities.computeIfAbsent(clientId, this::spawnHeroForClient);
    }
    // Cleanup entities for disconnected clients
    for (Integer clientId : clientEntities.keySet()) {
      if (!net.tcpClientMap().containsValue(clientId)) {
        Entity entity = clientEntities.remove(clientId);
        if (entity != null) {
          Game.remove(entity);
          LOGGER.info("Removed entity for disconnected client {}", clientId);
        }
      }
    }
  }

  private Entity spawnHeroForClient(Integer clientId) {
    try {
      String playerName = net.clientName(clientId).orElse("Player_" + clientId);
      Entity hero =
          HeroFactory.newHero(true, playerName); // Local because we have authoritative control
      hero.fetch(PositionComponent.class).ifPresent(pc -> pc.position(Game.startTile().get()));
      Game.add(hero);
      System.out.println("Spawned hero for client " + clientId + ": " + hero.name());
      return hero;
    } catch (IOException e) {
      LOGGER.error("Failed to spawn hero for client {}", clientId, e);
      return null;
    }
  }
}
