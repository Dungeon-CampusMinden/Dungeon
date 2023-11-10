package core.game;

import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

import core.Entity;
import core.Game;
import core.System;
import core.components.PositionComponent;
import core.systems.CameraSystem;
import core.systems.DrawSystem;
import core.utils.Constants;
import core.utils.IVoidFunction;
import core.utils.components.MissingComponentException;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

public class GameLoop extends ScreenAdapter {
    private boolean doSetup = true;
    private boolean newLevelWasLoadedInThisLoop = false;
    private boolean uiDebugFlag = false;

    /** Starts the dungeon and requires a {@link Game}. */
    public static void run() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowSizeLimits(
                PreRunConfiguration.windowWidth(), PreRunConfiguration.windowHeight(), 9999, 9999);
        // The third and fourth parameters ("maxWidth" and "maxHeight") affect the resizing
        // behavior
        // of the window. If the window is enlarged or maximized, then it can assume these
        // dimensions at maximum. If you have a larger screen resolution than 9999x9999 pixels,
        // increase these parameters.
        config.setForegroundFPS(PreRunConfiguration.frameRate());
        config.setTitle(PreRunConfiguration.windowTitle());
        config.setWindowIcon(PreRunConfiguration.logoPath());
        config.disableAudio(PreRunConfiguration.disableAudio());

        if (PreRunConfiguration.fullScreen()) {
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        } else {
            config.setWindowedMode(
                    PreRunConfiguration.windowWidth(), PreRunConfiguration.windowHeight());
        }

        // uncomment this if you wish no audio
        new Lwjgl3Application(
                new com.badlogic.gdx.Game() {
                    @Override
                    public void create() {
                        setScreen(new Game());
                    }
                },
                config);
    }

    /**
     * Sets {@link #currentLevel} to the new level and changes the currently active entity storage.
     *
     * <p>Will remove all Systems using {@link Game#removeAllSystems()} from the Game. This will
     * trigger {@link System#onEntityRemove} for the old level. Then, it will readd all Systems
     * using {@link Game#add(System)}, triggering {@link System#onEntityAdd} for the new level.
     *
     * <p>Will re-add the hero if they exist.
     */
    private final IVoidFunction onLevelLoad =
            () -> {
                newLevelWasLoadedInThisLoop = true;
                boolean firstLoad = !levelStorageMap.containsKey(currentLevel());
                hero().ifPresent(Game::remove);
                // Remove the systems so that each triggerOnRemove(entity) will be called (basically
                // cleanup).
                Map<Class<? extends System>, System> s = Game.systems();
                removeAllSystems();
                activeEntityStorage =
                        levelStorageMap.computeIfAbsent(currentLevel(), k -> new HashSet<>());
                // readd the systems so that each triggerOnAdd(entity) will be called (basically
                // setup). This will also create new EntitySystemMapper if needed.
                s.values().forEach(Game::add);

                try {
                    hero().ifPresent(this::placeOnLevelStart);
                } catch (MissingComponentException e) {
                    LOGGER.warning(e.getMessage());
                }
                hero().ifPresent(Game::add);
                currentLevel().onLoad();
                PreRunConfiguration.userOnLevelLoad().accept(firstLoad);
            };

    /**
     * Main game loop.
     *
     * <p>Redraws the dungeon, updates the entity sets, and triggers the execution of the systems.
     * Will call {@link #onFrame}.
     *
     * @param delta the time since the last loop
     */
    @Override
    public void render(float delta) {
        if (doSetup) onSetup();
        DrawSystem.batch().setProjectionMatrix(CameraSystem.camera().combined);
        onFrame();
        clearScreen();

        for (System system : systems().values()) {
            // if a new level was loaded, stop this loop-run
            if (newLevelWasLoadedInThisLoop) break;
            if (system.isRunning()) system.execute();
        }
        newLevelWasLoadedInThisLoop = false;
        CameraSystem.camera().update();
        // stage logic
        Game.stage().ifPresent(Game::updateStage);
    }

    /**
     * Called once at the beginning of the game.
     *
     * <p>Will perform some setup.
     */
    private void onSetup() {
        doSetup = false;
        CameraSystem.camera().zoom = Constants.DEFAULT_ZOOM_FACTOR;
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
    private void onFrame() {
        debugKeys();
        fullscreenKey();
        PreRunConfiguration.userOnFrame().execute();
    }

    private static Stage stage;

    public static Optional<Stage> stage() {
        return Optional.ofNullable(stage);
    }

    private static void updateStage(Stage x) {
        x.act(Gdx.graphics.getDeltaTime());
        x.draw();
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

    /** Just for debugging, remove later. */
    private void debugKeys() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            // toggle UI "debug rendering"
            stage().ifPresent(x -> x.setDebugAll(uiDebugFlag = !uiDebugFlag));
        }
    }

    private void fullscreenKey() {
        if (Gdx.input.isKeyJustPressed(
                core.configuration.KeyboardConfig.TOGGLE_FULLSCREEN.value())) {
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
    private void placeOnLevelStart(Entity entity) {
        add(entity);
        PositionComponent pc =
                entity.fetch(PositionComponent.class)
                        .orElseThrow(
                                () ->
                                        MissingComponentException.build(
                                                entity, PositionComponent.class));
        pc.position(startTile());
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
        stage().ifPresent(
                        x -> {
                            x.getViewport().setWorldSize(width, height);
                            x.getViewport().update(width, height, true);
                        });
    }
}
