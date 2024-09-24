package core.game;

import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.level.generator.postGeneration.WallGenerator;
import core.level.generator.randomwalk.RandomWalkGenerator;
import core.systems.*;
import core.utils.IVoidFunction;
import core.utils.components.MissingComponentException;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

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
  private static final Logger LOGGER = Logger.getLogger(GameLoop.class.getSimpleName());
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
        Optional<Entity> hero = ECSManagment.hero();
        boolean firstLoad = !ECSManagment.levelStorageMap().containsKey(Game.currentLevel());
        hero.ifPresent(ECSManagment::remove);
        // Remove the systems so that each triggerOnRemove(entity) will be called (basically
        // cleanup).
        Map<Class<? extends System>, System> s = ECSManagment.systems();
        ECSManagment.removeAllSystems();
        ECSManagment.activeEntityStorage(
            ECSManagment.levelStorageMap()
                .computeIfAbsent(Game.currentLevel(), k -> new HashSet<>()));
        // readd the systems so that each triggerOnAdd(entity) will be called (basically
        // setup). This will also create new EntitySystemMapper if needed.
        s.values().forEach(ECSManagment::add);

        try {
          hero.ifPresent(this::placeOnLevelStart);
        } catch (MissingComponentException e) {
          LOGGER.warning(e.getMessage());
        }
        hero.ifPresent(ECSManagment::add);
        Game.currentLevel().onLoad();
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
    config.setTitle(PreRunConfiguration.windowTitle());
    config.setWindowIcon(PreRunConfiguration.logoPath().pathString());
    config.disableAudio(PreRunConfiguration.disableAudio());

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
    frame();
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
   * <p>Will perform some setup.
   */
  private void setup() {
    doSetup = false;
    createSystems();
    setupStage();
    PreRunConfiguration.userOnSetup().execute();
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
    pc.position(Game.startTile());
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
    ECSManagment.add(
        new LevelSystem(
            DrawSystem.painter(), new WallGenerator(new RandomWalkGenerator()), onLevelLoad));
    ECSManagment.add(new DrawSystem());
    ECSManagment.add(new VelocitySystem());
    ECSManagment.add(new PlayerSystem());
  }
}
