package core.game;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import contrib.utils.CheckPatternPainter;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.loader.DungeonLoader;
import core.network.MessageDispatcher;
import core.network.handler.LocalNetworkHandler;
import core.network.messages.s2c.*;
import core.systems.*;
import core.utils.Direction;
import core.utils.IVoidFunction;
import core.utils.components.MissingComponentException;
import java.io.IOException;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Dungeon-GameLoop.
 *
 * <p>This class contains the game loop method that is connected with libGDX. It controls the system
 * flow, will execute the Systems, and triggers the event callbacks configured in the {@link
 * PreRunConfiguration}.
 *
 * <p>Use {@link #run()} to start the game.
 *
 * <p>All API methods can also be accessed via the {@link core.Game} class.
 */
public final class GameLoop extends ScreenAdapter {
  private static final Logger LOGGER = LoggerFactory.getLogger(GameLoop.class);
  private static Stage stage;
  private boolean doSetup = true;
  private boolean newLevelWasLoadedInThisLoop = false;

  /**
   * Sets {@link Game#currentLevel} to the new level and changes the currently active entity
   * storage.
   *
   * <p>Will remove all Systems using {@link ECSManagment#removeAllSystems()} from the Game. This
   * will trigger {@link System#onEntityRemove} for the old level. Then, it will readd all Systems
   * using {@link ECSManagment#add(System)}, triggering {@link System#onEntityAdd} for the new
   * level.
   *
   * <p>Will re-add the hero if they exist.
   */
  private final IVoidFunction onLevelLoad =
      () -> {
        newLevelWasLoadedInThisLoop = true;
        List<Entity> allHeros = ECSManagment.allHeros().toList();
        boolean firstLoad = !ECSManagment.levelStorageMap().containsKey(Game.currentLevel());
        allHeros.forEach(ECSManagment::remove);
        // Remove the systems so that each triggerOnRemove(entity) will be called (basically
        // cleanup).
        Map<Class<? extends System>, System> s = ECSManagment.systems();
        ECSManagment.removeAllSystems();
        ECSManagment.activeEntityStorage(
            ECSManagment.levelStorageMap()
                .computeIfAbsent(Game.currentLevel().orElse(null), k -> new HashSet<>()));
        // readd the systems so that each triggerOnAdd(entity) will be called (basically
        // setup). This will also create new EntitySystemMapper if needed.
        s.values().forEach(ECSManagment::add);

        try {
          allHeros.forEach(this::placeOnLevelStart);
        } catch (MissingComponentException e) {
          LOGGER.warn(e.getMessage());
        }
        ECSManagment.allEntities()
            .filter(entity -> entity.isPersistent())
            .map(ECSManagment::remove)
            .forEach(ECSManagment::add);
        if (firstLoad && Game.isCheckPatternEnabled())
          CheckPatternPainter.paintCheckerPattern(Game.currentLevel().orElse(null).layout());
        PreRunConfiguration.userOnLevelLoad().accept(firstLoad);
      };

  // for singleton
  private GameLoop() {}

  /** Starts the dungeon. */
  public static void run() {
    Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
    config.setWindowSizeLimits(
        PreRunConfiguration.windowWidth(), PreRunConfiguration.windowHeight(), 9999, 9999);
    config.setForegroundFPS(PreRunConfiguration.frameRate());
    config.setResizable(PreRunConfiguration.resizeable());
    config.setTitle(PreRunConfiguration.windowTitle());
    config.setWindowIcon(PreRunConfiguration.logoPath().pathString());
    config.disableAudio(PreRunConfiguration.disableAudio());
    config.setWindowListener(WindowEventManager.windowListener());
    if (SharedLibraryLoader.isMac && Gdx.app == null) {
      org.lwjgl.system.Configuration.GLFW_LIBRARY_NAME.set("glfw_async");
    }
    if (PreRunConfiguration.fullScreen()) {
      config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
    } else {
      config.setWindowedMode(PreRunConfiguration.windowWidth(), PreRunConfiguration.windowHeight());
    }

    new Lwjgl3Application(
        new com.badlogic.gdx.Game() {
          @Override
          public void create() {
            setScreen(new GameLoop());
          }
        },
        config);
  }

  /**
   * Get the {@link Stage} that can be used to draw HUD elements.
   *
   * @return The configured stage, can be empty.
   */
  public static Optional<Stage> stage() {
    return Optional.ofNullable(stage);
  }

  private static void updateStage(final Stage stage) {
    stage.act(Gdx.graphics.getDeltaTime());
    stage.draw();
  }

  private static void setupStage() {
    stage =
        new Stage(
            new ScalingViewport(
                Scaling.stretch,
                PreRunConfiguration.windowWidth(),
                PreRunConfiguration.windowHeight()),
            new SpriteBatch());
    Gdx.input.setInputProcessor(stage);
  }

  /**
   * Main game loop.
   *
   * <p>Triggers the execution of the systems and the event callbacks.
   *
   * <p>Will trigger {@link #frame} and {@link PreRunConfiguration#userOnFrame()}.
   *
   * <p>On the first frame, {@link #setup()} and {@link PreRunConfiguration#userOnSetup()} are
   * triggered.
   *
   * @param delta The time since the last loop.
   */
  @Override
  public void render(float delta) {
    if (doSetup) setup();
    ECSManagment.system(
        DrawSystem.class,
        drawSystem -> drawSystem.batch().setProjectionMatrix(CameraSystem.camera().combined));
    // Drain any inbound network messages on the game thread before running systems
    try {
      Game.network().pollAndDispatch();
    } catch (Exception e) {
      LOGGER.warn("Error while polling network messages: " + e.getMessage());
    }
    frame();
    clearScreen();

    // Execute ECS tick using shared runner. In MP client mode, run render/input/camera only.
    final boolean isMultiplayerClient =
        PreRunConfiguration.multiplayerEnabled() && !PreRunConfiguration.isNetworkServer();
    ECSTickRunner.runOneFrame(
        s -> {
          if (newLevelWasLoadedInThisLoop) return false;
          if (!isMultiplayerClient) return true;
          return (s instanceof DrawSystem)
              || (s instanceof CameraSystem)
              || (s instanceof InputSystem);
        });

    newLevelWasLoadedInThisLoop = false;
    if (Game.network() instanceof LocalNetworkHandler localHandler) {
      // If we are in single player, we can trigger the state update directly.
      localHandler.triggerStateUpdate();
    }
    CameraSystem.camera().update();
    // stage logic
    stage().ifPresent(GameLoop::updateStage);
  }

  /**
   * Called once at the beginning of the game.
   *
   * <p>Will execute {@link LevelSystem#execute()} once to load the first level before the actual
   * game loop starts. This ensures the first level is set at the start of the game loop, even if
   * the {@link LevelSystem} is not executed as the first system in the game loop..
   *
   * <p>Will perform some setup.
   */
  private void setup() {
    doSetup = false;
    createSystems();
    setupMessageHandlers();
    setupStage();
    PreRunConfiguration.userOnSetup().execute();
    Game.systems().get(LevelSystem.class).execute();
  }

  private void setupMessageHandlers() {
    MessageDispatcher dispatcher = Game.network().messageDispatcher();

    dispatcher.registerHandler(
        ConnectReject.class,
        (ctx, event) -> {
          LOGGER.warn("Received ConnectReject: {}", event.reason());
          ctx.close();
        });

    dispatcher.registerHandler(
        EntitySpawnEvent.class,
        (ctx, event) -> {
          LOGGER.info("Received EntitySpawnEvent event: " + event.entityId());

          // check if the entity already exists
          if (Game.allEntities().anyMatch(e -> e.id() == event.entityId())) {
            LOGGER.warn(
                "Received spawn event for already existing entity with ID: " + event.entityId());
            return;
          }

          Entity newEntity = new Entity(event.entityId());
          PositionComponent pc = new PositionComponent(event.position());
          pc.viewDirection(event.viewDirection());
          newEntity.add(pc);
          DrawComponent dc;
          try {
            dc = new DrawComponent(event.texturePath());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          dc.currentAnimation(event.currentAnimation());
          dc.tintColor(event.tintColor());
          newEntity.add(dc);
          Game.add(newEntity);
        });

    dispatcher.registerHandler(
        EntityDespawnEvent.class,
        (ctx, event) -> {
          LOGGER.info(
              "Received EntityDespawnEvent event: "
                  + event.entityId()
                  + ", reason: "
                  + event.reason());
          Entity entity =
              Game.allEntities().filter(e -> e.id() == event.entityId()).findFirst().orElse(null);
          if (entity == null) {
            LOGGER.warn("Received despawn event for unknown entity with ID: " + event.entityId());
            return;
          }
          Game.remove(entity);
        });

    dispatcher.registerHandler(
        LevelChangeEvent.class,
        (ctx, event) -> {
          LOGGER.info(
              "Received LevelChangeEvent event: "
                  + event.levelName()
                  + ", spawn point: "
                  + event.spawnPoint());
          if (event.levelName() == null || event.levelName().isBlank()) {
            LOGGER.warn(
                "Received LevelChangeEvent with empty level name. Value was: " + event.levelName());
            return;
          }
          try {
            DungeonLoader.loadLevel(event.levelName());
            if (event.spawnPoint() == null) {
              Game.hero().ifPresent(this::placeOnLevelStart);
            } else {
              Debugger.TELEPORT(event.spawnPoint());
            }
          } catch (Exception e) {
            LOGGER.warn("Failed to handle LevelChangeEvent: " + e.getMessage());
          }
        });
    dispatcher.registerHandler(
        GameOverEvent.class,
        (ctx, event) -> {
          LOGGER.info("Received GameOverEvent event");
          Game.exit("Game Over");
        });

    dispatcher.registerHandler(
        SnapshotMessage.class,
        (ctx, event) -> {
          try {
            Game.network().snapshotTranslator().applySnapshot(event, dispatcher);
          } catch (Exception ignored) {
          }
        });
  }

  /**
   * Called at the beginning of each frame, before the entities are updated and the systems are
   * executed.
   *
   * <p>This is the place to add basic logic that isn't part of any system.
   */
  private void frame() {
    fullscreenKey();
    PreRunConfiguration.userOnFrame().execute();
  }

  private void fullscreenKey() {
    if (Gdx.input.isKeyJustPressed(core.configuration.KeyboardConfig.TOGGLE_FULLSCREEN.value())) {
      if (!Gdx.graphics.isFullscreen()) {
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
      } else {
        Gdx.graphics.setWindowedMode(
            PreRunConfiguration.windowWidth(), PreRunConfiguration.windowHeight());
      }
    }
  }

  /**
   * Set the position of the given entity to the position of the level-start.
   *
   * <p>A {@link PositionComponent} is needed.
   *
   * @param entity entity to set on the start of the level, normally this is the hero.
   */
  private void placeOnLevelStart(final Entity entity) {
    ECSManagment.add(entity);
    PositionComponent pc =
        entity
            .fetch(PositionComponent.class)
            .orElseThrow(() -> MissingComponentException.build(entity, PositionComponent.class));
    Game.startTile()
        .ifPresentOrElse(
            pc::position, () -> LOGGER.warn("No start tile found for the current level"));

    pc.viewDirection(Direction.DOWN); // look down by default
  }

  /**
   * Clear the screen. Removes all.
   *
   * <p>Needs to be called before redraw something.
   */
  private void clearScreen() {
    Gdx.gl.glClearColor(0, 0, 0, 1);
    Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);
  }

  @Override
  public void resize(int width, int height) {
    super.resize(width, height);
    stage()
        .ifPresent(
            x -> {
              x.getViewport().setWorldSize(width, height);
              x.getViewport().update(width, height, true);
            });
  }

  /** Create the systems. */
  private void createSystems() {
    ECSManagment.add(new PositionSystem());
    ECSManagment.add(new CameraSystem());
    ECSManagment.add(new LevelSystem(onLevelLoad));
    ECSManagment.add(new DrawSystem());
    ECSManagment.add(new VelocitySystem());
    ECSManagment.add(new InputSystem());
    ECSManagment.add(new FrictionSystem());
    ECSManagment.add(new MoveSystem());
  }
}
