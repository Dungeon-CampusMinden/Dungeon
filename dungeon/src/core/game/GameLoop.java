package core.game;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.badlogic.gdx.utils.viewport.ScalingViewport;
import contrib.entities.deco.DecoFactory;
import contrib.systems.DebugDrawSystem;
import contrib.utils.CheckPatternPainter;
import core.Entity;
import core.Game;
import core.System;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.sound.player.GdxSoundPlayer;
import core.sound.player.ISoundPlayer;
import core.sound.player.NoSoundPlayer;
import core.systems.*;
import core.utils.Direction;
import core.utils.IVoidFunction;
import core.utils.components.MissingComponentException;
import core.utils.logging.DungeonLogger;
import java.util.*;

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
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(GameLoop.class);
  private static ISoundPlayer soundPlayer = new NoSoundPlayer();
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
   * <p>Will re-add the player if they exist.
   */
  private final IVoidFunction onLevelLoad =
      () -> {
        newLevelWasLoadedInThisLoop = true;
        Optional<Entity> hero = ECSManagment.player();
        boolean firstLoad =
            !ECSManagment.levelStorageMap().containsKey(Game.currentLevel().orElseThrow());
        hero.ifPresent(ECSManagment::remove);
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
          hero.ifPresent(this::placeOnLevelStart);
        } catch (MissingComponentException e) {
          LOGGER.warn(e.getMessage());
        }
        ECSManagment.allEntities()
            .filter(Entity::isPersistent)
            .map(ECSManagment::remove)
            .forEach(ECSManagment::add);

        Game.currentLevel()
            .ifPresent(
                level -> {
                  level
                      .decorations()
                      .forEach(tuple -> Game.add(DecoFactory.createDeco(tuple.b(), tuple.a())));
                });

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
    DrawSystem.batch().setProjectionMatrix(CameraSystem.camera().combined);
    frame(delta);
    clearScreen();

    for (System system : ECSManagment.systems().values()) {
      // if a new level was loaded, stop this loop-run
      if (newLevelWasLoadedInThisLoop) break;
      system.lastExecuteInFrames(system.lastExecuteInFrames() + 1);
      if (system.isRunning() && system.lastExecuteInFrames() >= system.executeEveryXFrames()) {
        system.execute();
        system.lastExecuteInFrames(0);
      }
    }
    newLevelWasLoadedInThisLoop = false;
    CameraSystem.camera().update();
    // stage logic
    stage().ifPresent(GameLoop::updateStage);
  }

  /**
   * Called once at the beginning of the game.
   *
   * <p>Will execute {@link LevelSystem#execute()} once to load the first level before the actual
   * game loop starts. This ensures the first level is set at the start of the game loop, even if
   * the {@link LevelSystem} is not executed as the first system in the game loop.
   *
   * <p>Will perform some setup.
   */
  private void setup() {
    doSetup = false;
    if (Gdx.audio != null && !PreRunConfiguration.disableAudio()) {
      AssetManager assetManager = new AssetManager();
      soundPlayer = new GdxSoundPlayer(assetManager);
    }
    createSystems();
    setupStage();
    PreRunConfiguration.userOnSetup().execute();
    Game.systems().get(LevelSystem.class).execute();
  }

  /**
   * Called at the beginning of each frame, before the entities are updated and the systems are
   * executed.
   *
   * <p>This is the place to add basic logic that isn't part of any system.
   *
   * @param delta The time since the last loop.
   */
  private void frame(float delta) {
    fullscreenKey();
    Game.soundPlayer().update(delta);
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
   * @param entity entity to set on the start of the level, normally this is the player.
   */
  private void placeOnLevelStart(final Entity entity) {
    ECSManagment.add(entity);
    entity
        .fetch(PositionComponent.class)
        .ifPresent(
            pc -> {
              Game.startTile()
                  .ifPresentOrElse(
                      pc::position, () -> LOGGER.warn("No start tile found for the current level"));
              pc.viewDirection(Direction.DOWN); // look down by default
            });

    // reset animations
    entity.fetch(DrawComponent.class).ifPresent(DrawComponent::resetState);
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

  /**
   * Get the sound player used by the game.
   *
   * @return The sound player.
   */
  public static ISoundPlayer soundPlayer() {
    return soundPlayer;
  }

  /** Create the systems. */
  private void createSystems() {
    ECSManagment.add(new PositionSystem());
    ECSManagment.add(new CameraSystem());
    ECSManagment.add(new LevelSystem(onLevelLoad));
    ECSManagment.add(new DrawSystem());
    ECSManagment.add(new VelocitySystem());
    ECSManagment.add(new FrictionSystem());
    ECSManagment.add(new MoveSystem());
    ECSManagment.add(new InputSystem());
    ECSManagment.add(new DebugDrawSystem());
    ECSManagment.add(new SoundSystem());
  }
}
