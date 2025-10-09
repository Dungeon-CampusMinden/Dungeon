package core.network.server;

import static core.network.config.NetworkConfig.SERVER_SNAPSHOT_HZ;
import static core.network.config.NetworkConfig.SERVER_TICK_HZ;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessFiles;
import contrib.entities.HeroController;
import contrib.entities.HeroFactory;
import contrib.systems.*;
import core.Entity;
import core.Game;
import core.components.PositionComponent;
import core.game.ECSManagment;
import core.game.GameLoop;
import core.game.PreRunConfiguration;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.loader.DungeonLoader;
import core.network.SnapshotTranslator;
import core.network.messages.c2s.InputMessage;
import core.network.messages.s2c.GameOverEvent;
import core.network.messages.s2c.HeroSpawnEvent;
import core.network.messages.s2c.LevelChangeEvent;
import core.systems.*;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.Vector2;
import java.util.Queue;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthoritativeServerLoop {
  private static final Logger LOGGER = LoggerFactory.getLogger(AuthoritativeServerLoop.class);
  private static final boolean PRINT_RTT = false; // to debug latency issues

  private final ServerTransport net;
  private final SnapshotTranslator translator;
  private final ScheduledExecutorService executor;
  private volatile int serverTick = 0;

  public AuthoritativeServerLoop(ServerTransport netService, SnapshotTranslator translator) {
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

  public void start() {
    Gdx.files = new HeadlessFiles();

    PreRunConfiguration.frameRate(SERVER_TICK_HZ);

    createSystems();
    try {
      DungeonLoader.afterAllLevels(() -> Game.network().broadcast(new GameOverEvent(), true));
      DungeonLoader.addLevel(Tuple.of("maze", DungeonLevel.class));
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

  public void stop() {
    executor.shutdownNow();
    LOGGER.info("ServerLoop stopped");
  }

  private void createSystems() {
    ECSManagment.add(new PositionSystem());
    ECSManagment.add(
        new LevelSystem(
            () -> {
              GameLoop.onLevelLoad.execute();
              this.broadcastLevelChange();
            }));
    ECSManagment.add(new VelocitySystem());
    ECSManagment.add(new FrictionSystem());
    ECSManagment.add(new MoveSystem());
    ECSManagment.add(new ProjectileSystem());
    ECSManagment.add(new HealthSystem());
    ECSManagment.add(new PathSystem());
    ECSManagment.add(new AISystem());
    ECSManagment.add(new CollisionSystem());
    ECSManagment.add(new FallingSystem());
    ECSManagment.add(new ManaRestoreSystem());
    ECSManagment.add(new DrawSystem());
  }

  private void tick() {
    try {
      //noinspection NonAtomicOperationOnVolatileField only place where serverTick is modified
      serverTick++;
      syncClientsToEntities();
      drainAndApplyInputs(net.inputQueue());
      ECSManagment.runOneFrame();
    } catch (Exception e) {
      LOGGER.error("Tick error", e);
    } catch (Throwable t) {
      LOGGER.error("Unexpected error in server loop", t);
      stop();
    }
  }

  /**
   * Drains the input queue and applies valid inputs to the corresponding hero entities. Processes
   * messages in arrival order (FIFO), but discards stale/duplicate inputs based on sequence
   * numbers. Intended to be called per server tick in the AuthoritativeServerLoop for batched,
   * deterministic application.
   *
   * @param queue The concurrent input queue (global or per-client).
   */
  private void drainAndApplyInputs(Queue<Tuple<ClientState, InputMessage>> queue) {
    Tuple<ClientState, InputMessage> tuple;
    while ((tuple = queue.poll()) != null) {
      ClientState clientState = tuple.a();
      InputMessage msg = tuple.b();

      // TODO: Reconcile inputs based on clientTick vs serverTick and RTT

      // Get hero entity
      Entity entity = clientState.heroEntity().orElse(null);
      if (entity == null) {
        LOGGER.warn("No hero entity for client {}", clientState);
        continue;
      }

      // Apply input
      try {
        switch (msg.action()) {
          case MOVE -> HeroController.moveHero(entity, Vector2.of(msg.point()).direction());
          case MOVE_PATH -> HeroController.moveHeroPath(entity, msg.point());
          case CAST_SKILL -> HeroController.useSkill(entity, msg.point());
          case NEXT_SKILL -> HeroController.changeSkill(entity, true);
          case PREV_SKILL -> HeroController.changeSkill(entity, false);
          case INTERACT -> HeroController.interact(entity, msg.point());
          default -> LOGGER.warn("Unknown action {} for client {}", msg.action(), clientState);
        }
        // On success: Update processed seq and activity
        clientState.updateProcessedSeq(msg.sequence());
        clientState.updateLastActivity();
        LOGGER.trace("Applied input for client {} (action: {})", clientState, msg.action());
      } catch (Exception e) {
        LOGGER.error("Failed to apply input for client {}: {}", clientState, e.getMessage());
      }
    }
  }

  private void sendSnapshot() {
    translator
        .translateToSnapshot(serverTick)
        .ifPresent(
            snapshot -> {
              Game.network().broadcast(snapshot, true);
            });
  }

  private void broadcastLevelChange() {
    Game.network().broadcast(LevelChangeEvent.currentLevel(), true);
  }

  private void syncClientsToEntities() {
    // Spawn Hero if TCP client exists but no entity is associated
    for (ClientState state : net.connectedClients()) {
      if (state.heroEntity().isEmpty()) {
        state.heroEntity(spawnHeroForClient(state));
      }
    }

    // TODO: Will not working, because the sessions will get removed
    // For every session that is no longer active, remove its entity from the game
    for (Session session : net.sessions().values()) {
      if (session.isClosed()) {
        session
            .clientState()
            .ifPresent(
                state ->
                    state
                        .heroEntity()
                        .ifPresent(
                            hero -> {
                              Game.remove(hero);
                              state.heroEntity(null);
                              LOGGER.info(
                                  "Removed entity for disconnected client {}", state.clientId());
                            }));
      }
    }
  }

  private Entity spawnHeroForClient(ClientState state) {
    Entity hero = HeroFactory.newHero(true, state.username());
    hero.fetch(PositionComponent.class)
        .ifPresent(pc -> pc.position(Game.startTile().map(Tile::position).orElse(new Point(0, 0))));
    // Add the hero to the game, after the client knows the id.
    Game.network()
        .send(state.clientId(), new HeroSpawnEvent(hero.id()), true)
        .thenAccept(
            success -> {
              if (success) {
                Game.add(hero);
              } else {
                LOGGER.warn(
                    "Failed to send HeroSpawnEvent to client {}, not adding hero to game", state);
              }
            });
    return hero;
  }
}
