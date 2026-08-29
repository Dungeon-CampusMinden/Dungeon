package wizard.runner.runtime.multiplayer;

import engine.Entity;
import engine.Game;
import engine.game.ECSManagement;
import engine.game.ServerLifecycle;
import engine.game.ServerStarter;
import engine.level.Tile;
import engine.level.elements.tile.DoorTile;
import engine.level.elements.tile.ExitTile;
import engine.level.loader.DungeonLoader;
import engine.network.config.DefaultEntitySpawnStrategy;
import engine.network.handler.NettyNetworkHandler;
import engine.network.server.ClientState;
import engine.network.server.ServerTransport;
import engine.systems.FrictionSystem;
import engine.systems.LevelSystem;
import engine.systems.MoveSystem;
import engine.systems.PositionSystem;
import engine.systems.VelocitySystem;
import engine.tracking.Tracking;
import engine.utils.Point;
import engine.utils.Tuple;
import engine.utils.logging.DungeonLoggerConfig;
import escaperoom.foundation.definition.RoomDefinition;
import escaperoom.foundation.multiplayer.binding.ServerBinding;
import escaperoom.foundation.multiplayer.bootstrap.BootstrapEntitySpawnStrategy;
import escaperoom.foundation.multiplayer.game.FoundationSnapshotTranslator;
import escaperoom.foundation.multiplayer.game.ServerGameBinding;
import escaperoom.foundation.multiplayer.session.MultiplayerSession;
import escaperoom.foundation.room.level.RoomLevel;
import escaperoom.foundation.room.model.FoundationRoom;
import feature.entities.CharacterClass;
import feature.entities.HeroController;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import wizard.runner.RunnerInterruptedException;

/** Room-first composition for one generic Foundation multiplayer host runtime. */
public final class MultiplayerHostRun {
  /** Stable generic Dungeon loader name shared by host and join. */
  public static final String GENERIC_LEVEL_NAME = "foundation-runner";

  private final FoundationRoom room;
  private final RoomDefinition definition;
  private final RoomLevel level;
  private final FoundationSnapshotTranslator snapshotTranslator;
  private final Entity bootstrapMarker;
  private final ServerBinding serverBinding;
  private final AtomicBoolean started = new AtomicBoolean();
  private volatile ServerGameBinding gameBinding;

  private MultiplayerHostRun(final FoundationRoom room) {
    this.room = Objects.requireNonNull(room, "room");
    definition = room.definition();
    level = RoomLevel.fromLayout(room.layout());
    snapshotTranslator = new FoundationSnapshotTranslator(room);
    MultiplayerSession session = new MultiplayerSession(definition, room.presentation());
    serverBinding = new ServerBinding(session, MultiplayerHostRun::readyClients);
    bootstrapMarker = new Entity("foundation-bootstrap-marker");
  }

  /**
   * Creates a host from one already derived room.
   *
   * @param room immutable in-memory room handoff
   * @return host composition backed by the supplied room
   */
  public static MultiplayerHostRun from(final FoundationRoom room) {
    return new MultiplayerHostRun(Objects.requireNonNull(room, "room"));
  }

  /** Starts the generic headless server exactly once with the derived room already installed. */
  public void run() {
    if (!started.compareAndSet(false, true)) {
      throw new IllegalStateException("Foundation multiplayer host run was already started");
    }

    CountDownLatch completion = new CountDownLatch(1);
    ServerLifecycle lifecycle =
        ServerLifecycle.install("Foundation multiplayer host stopped", completion::countDown);
    Tracking.configureRoom(definition.id());
    var spawnStrategy =
        new BootstrapEntitySpawnStrategy(
            bootstrapMarker, room.inputSha256(), new DefaultEntitySpawnStrategy());
    try {
      var starter =
          ServerStarter.builder(this::install)
              .levels(Tuple.of(GENERIC_LEVEL_NAME, RoomLevel.class))
              .characterClasses(room.playableCharacterClasses().toArray(CharacterClass[]::new))
              .maximumPlayers(definition.roster().slots().size())
              .entitySpawnStrategy(spawnStrategy)
              .snapshotTranslator(snapshotTranslator)
              .onFrame(
                  () -> {
                    ServerGameBinding binding = gameBinding;
                    if (binding == null) {
                      binding =
                          createGameBinding(
                              () -> lifecycle.requestExit("Foundation multiplayer room completed"));
                      gameBinding = binding;
                    }
                    binding.tick();
                    HeroController.drainAndApplyInputs();
                  })
              .disableAudio(true)
              .frameRate(30)
              .build();
      starter.apply();
      prepareLevel();
      Game.windowTitle(room.title() + " Server");
      Game.run();
      awaitCompletion(completion);
    } finally {
      try {
        lifecycle.requestExit("Foundation multiplayer host stopped");
      } finally {
        DungeonLoggerConfig.shutdown();
      }
    }
  }

  private void prepareLevel() {
    requiredLevelSystem();
    DungeonLoader.loadInMemoryLevel(GENERIC_LEVEL_NAME, level);
  }

  private void install() {
    ECSManagement.add(new PositionSystem());
    ECSManagement.add(new VelocitySystem());
    ECSManagement.add(new FrictionSystem());
    ECSManagement.add(new MoveSystem());
    Game.add(bootstrapMarker);
  }

  private ServerGameBinding createGameBinding(final Runnable onTerminalComplete) {
    return new ServerGameBinding(
        serverBinding,
        definition,
        room.presentation(),
        snapshotTranslator,
        requiredTile(RoomLevel.DOOR_POINT_NAME, DoorTile.class),
        requiredTile(RoomLevel.EXIT_POINT_NAME, ExitTile.class),
        requiredLevelSystem(),
        componentStations(),
        hintStations(),
        System::nanoTime,
        onTerminalComplete);
  }

  private Map<String, Point> componentStations() {
    Map<String, Point> stations = new LinkedHashMap<>();
    room.layout().riddlePlacements().stream()
        .flatMap(placement -> placement.components().stream())
        .forEach(
            component ->
                stations.put(
                    component.componentId(),
                    requiredPoint("component_" + component.componentId())));
    return Map.copyOf(stations);
  }

  private Map<String, Point> hintStations() {
    Map<String, Point> stations = new LinkedHashMap<>();
    room.definition().progression().riddleNodes().stream()
        .map(node -> node.riddle())
        .filter(riddle -> !riddle.hints().isEmpty())
        .forEach(riddle -> stations.put(riddle.id(), requiredPoint("hint_" + riddle.id())));
    return Map.copyOf(stations);
  }

  private Point requiredPoint(final String name) {
    Point point = level.namedPoints().get(name);
    if (point == null || level.tileAt(point).isEmpty()) {
      throw new IllegalStateException("derived Foundation level is missing point " + name);
    }
    return new Point(point);
  }

  private <T extends Tile> T requiredTile(final String name, final Class<T> type) {
    Tile tile = level.tileAt(requiredPoint(name)).orElseThrow();
    if (!type.isInstance(tile)) {
      throw new IllegalStateException("derived Foundation level has invalid tile " + name);
    }
    return type.cast(tile);
  }

  private static LevelSystem requiredLevelSystem() {
    return (LevelSystem)
        Optional.ofNullable(Game.systems().get(LevelSystem.class))
            .orElseThrow(() -> new IllegalStateException("Dungeon LevelSystem is missing"));
  }

  private static Set<ClientState> readyClients() {
    if (!(Game.network() instanceof NettyNetworkHandler handler)) {
      return Set.of();
    }
    return handler
        .serverRuntime()
        .flatMap(runtime -> runtime.transport())
        .map(ServerTransport::connectedClients)
        .orElseGet(Set::of);
  }

  private static void awaitCompletion(final CountDownLatch completion) {
    try {
      Objects.requireNonNull(completion, "completion").await();
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new RunnerInterruptedException(exception);
    }
  }
}
