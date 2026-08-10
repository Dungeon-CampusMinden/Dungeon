package wizard.runner.runtime.multiplayer;

import contrib.entities.CharacterClass;
import contrib.entities.HeroController;
import core.Entity;
import core.Game;
import core.game.ECSManagement;
import core.game.ServerProcess;
import core.game.ServerStarter;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.elements.tile.ExitTile;
import core.level.loader.DungeonLoader;
import core.network.config.DefaultEntitySpawnStrategy;
import core.network.handler.NettyNetworkHandler;
import core.network.server.ClientState;
import core.network.server.ServerTransport;
import core.systems.FrictionSystem;
import core.systems.LevelSystem;
import core.systems.MoveSystem;
import core.systems.PositionSystem;
import core.systems.VelocitySystem;
import core.utils.Point;
import core.utils.Tuple;
import core.utils.logging.DungeonLoggerConfig;
import foundation.definition.RoomDefinition;
import foundation.multiplayer.binding.ServerBinding;
import foundation.multiplayer.bootstrap.BootstrapEntitySpawnStrategy;
import foundation.multiplayer.game.FoundationSnapshotTranslator;
import foundation.multiplayer.game.ServerGameBinding;
import foundation.multiplayer.session.MultiplayerSession;
import foundation.room.level.RoomLevel;
import foundation.room.model.FoundationRoom;
import java.io.IOException;
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
  private final AtomicBoolean exitRequested = new AtomicBoolean();
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
                              () ->
                                  requestExit("Foundation multiplayer room completed", completion));
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
      startManagedStopMonitor(completion);
      Game.run();
      awaitCompletion(completion);
    } finally {
      try {
        requestExit("Foundation multiplayer host stopped", completion);
      } finally {
        DungeonLoggerConfig.shutdown();
      }
    }
  }

  private void startManagedStopMonitor(final CountDownLatch completion) {
    if (!Boolean.getBoolean(ServerProcess.MANAGED_PROPERTY)) {
      return;
    }
    Thread.ofPlatform()
        .daemon()
        .name("foundation-managed-host-stop")
        .start(
            () -> {
              try {
                System.in.read();
              } catch (IOException exception) {
                // A broken management pipe has the same lifecycle meaning as EOF.
              }
              requestExit("Foundation multiplayer host stopped by its managing client", completion);
            });
  }

  private void requestExit(final String reason, final CountDownLatch completion) {
    if (!exitRequested.compareAndSet(false, true)) {
      return;
    }
    try {
      Game.exit(reason);
    } finally {
      completion.countDown();
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
    room.definition().sections().stream()
        .flatMap(section -> section.riddles().stream())
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
