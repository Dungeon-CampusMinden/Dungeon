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
import core.network.messages.NetworkMessage;
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

public final class AuthoritativeServerLoop {
  private static final Logger LOGGER =
    LoggerFactory.getLogger(AuthoritativeServerLoop.class);

  private static final int TICK_HZ = 20;
  private static final int SNAPSHOT_HZ = 20;

  private final ServerTransport net;
  private final SnapshotTranslator translator;
  private final ScheduledExecutorService executor;

  private final ConcurrentHashMap<Integer, Entity> clientEntities =
    new ConcurrentHashMap<>();

  private volatile long serverTick = 0;

  public AuthoritativeServerLoop(
    ServerTransport netService, SnapshotTranslator translator) {
    this.net = netService;
    this.translator = java.util.Objects.requireNonNull(translator, "translator");
    this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r, "AuthoritativeServerLoop");
      t.setDaemon(true);
      return t;
    });
  }

  public void start() {
    try {
      PreRunConfiguration.frameRate(TICK_HZ);
    } catch (Throwable ignored) {}

    createSystems();
    try {
      DungeonLoader.afterAllLevels(() -> broadcast(new GameOverEvent()));
      DungeonLoader.addLevel(Tuple.of("maze", DungeonLevel.class));
      DungeonLoader.loadLevel(0);
      broadcastLevelChange();
    } catch (Exception e) {
      LOGGER.warn("Failed to load initial level on server", e);
    }

    long tickPeriodMs = 1000L / TICK_HZ;
    long snapshotPeriodMs = 1000L / SNAPSHOT_HZ;

    executor.scheduleAtFixedRate(
      this::tick, 0, tickPeriodMs, TimeUnit.MILLISECONDS);
    executor.scheduleAtFixedRate(
      this::sendSnapshot, snapshotPeriodMs, snapshotPeriodMs,
      TimeUnit.MILLISECONDS);

    LOGGER.info("ServerLoop started: tickHz={}, snapshotHz={}", TICK_HZ,
      SNAPSHOT_HZ);
  }

  public void stop() {
    executor.shutdownNow();
    LOGGER.info("ServerLoop stopped");
  }

  private void createSystems() {
    ECSManagment.add(new PositionSystem());
    ECSManagment.add(new LevelSystem(() -> {}));
    ECSManagment.add(new VelocitySystem());
    ECSManagment.add(new FrictionSystem());
    ECSManagment.add(new MoveSystem());
    ECSManagment.add(new ProjectileSystem());
    ECSManagment.add(new HealthSystem());
    ECSManagment.add(new PathSystem());
    ECSManagment.add(new AISystem());
    ECSManagment.add(new CollisionSystem());
    ECSManagment.add(new FallingSystem());
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

  private void drainAndApplyInputs(ConcurrentLinkedQueue<InputMessage> q) {
    InputMessage msg;
    while ((msg = q.poll()) != null) {
      int id = msg.clientId();
      Entity player = clientEntities.get(id);
      if (player == null) continue;
      switch (msg.action()) {
        case MOVE -> HeroController.moveHero(
          player, Vector2.of(msg.point()).direction());
        case MOVE_PATH -> HeroController.moveHeroPath(player, msg.point());
        case CAST_SKILL -> HeroController.useSkill(player, msg.point());
        case NEXT_SKILL -> HeroController.changeSkill(player, true);
        case PREV_SKILL -> HeroController.changeSkill(player, false);
        case INTERACT -> HeroController.interact(player, msg.point());
      }
    }
  }

  private void sendSnapshot() {
    translator.translateToSnapshot(serverTick, clientEntities)
      .ifPresent(this::broadcast);
  }

  private void broadcastLevelChange() {
    String levelName;
    try {
      levelName = DungeonLoader.currentLevel();
    } catch (Throwable t) {
      LOGGER.warn("Failed to read current level", t);
      return;
    }
    broadcast(new LevelChangeEvent(levelName, null));
  }

  public void broadcast(NetworkMessage event) {
    for (Map.Entry<Integer, InetSocketAddress> e : net.udpClients().entrySet()) {
      net.sendUdpObject(e.getValue(), event);
    }
  }

  private void syncClientsToEntities() {
    for (Integer id : net.tcpClientMap().values()) {
      clientEntities.computeIfAbsent(id, this::spawnHeroForClient);
    }
    for (Integer id : clientEntities.keySet()) {
      if (!net.tcpClientMap().containsValue(id)) {
        Entity entity = clientEntities.remove(id);
        if (entity != null) {
          Game.remove(entity);
          LOGGER.info("Removed entity for disconnected client {}", id);
        }
      }
    }
  }

  private Entity spawnHeroForClient(Integer clientId) {
    try {
      String name = net.clientName(clientId).orElse("Player_" + clientId);
      Entity hero = HeroFactory.newHero(true, name);
      hero.fetch(PositionComponent.class)
        .ifPresent(pc -> pc.position(Game.startTile().get()));
      Game.add(hero);
      return hero;
    } catch (IOException e) {
      LOGGER.error("Failed to spawn hero for client {}", clientId, e);
      return null;
    }
  }
}
