package core;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import contrib.configuration.ItemConfig;
import contrib.entities.EntityFactory;
import contrib.systems.*;
import contrib.utils.components.Debugger;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Main entry of the game.
 * Manages window and screens.
 */
public class Dungeon extends Game implements IMenuScreenObserver {

    /**
     * Part of the pre-run configuration. The fps of the game (frames per second)
     */
    private static int FRAME_RATE = 30;
    private static String WINDOW_TITLE = "PM-Dungeon";
    /**
     * The width of the game window in pixels.
     *
     * <p>Manipulating this value will only result in changes before {@link Dungeon#run} was executed.
     */
    private static int WINDOW_WIDTH = 640;

    /**
     * Part of the pre-run configuration. The height of the game window in pixels.
     */
    private static int WINDOW_HEIGHT = 480;
    /**
     * The width of the game window in pixels.
     *
     * <p>Manipulating this value will only result in changes before {@link Dungeon#run} was executed.
     */
    private static int WINDOW_WIDTH_MAX = 640;
    /**
     * Part of the pre-run configuration. The height of the game window in pixels.
     */
    private static int WINDOW_HEIGHT_MAX = 480;
    /**
     * Part of the pre-run configuration. If this value is true, the game will be started in full
     * screen mode.
     */
    private static boolean FULL_SCREEN = false;
    /**
     * Part of the pre-run configuration. If this value is true, the audio for the game will be
     * disabled.
     *
     * <p>Manipulating this value will only result in changes before {@link Dungeon#run} was executed.
     */
    private static boolean DISABLE_AUDIO = false;
    /**
     * Part of the pre-run configuration. The path (as String) to the logo of the Game-Window.
     */
    private static String LOGO_PATH = "logo/CatLogo_35x35.png";

    private Menu menuScreen;
    private core.Game gameScreen;

    @Override
    public void create() {
        Logger LOGGER = Logger.getLogger("Main");
        Debugger debugger = new Debugger();

        menuScreen = Menu.getInstance();
        menuScreen.addListener(this);

        gameScreen = core.Game.getInstance();
        try {
            core.Game.hero(EntityFactory.newHero());
            core.Game.loadConfig(
                "dungeon_config.json",
                contrib.configuration.KeyboardConfig.class,
                core.configuration.KeyboardConfig.class,
                ItemConfig.class);
            core.Game.userOnLevelLoad(
                () -> {
                    try {
                        EntityFactory.newChest();
                    } catch (IOException e) {
                        LOGGER.warning("Could not create new Chest: " + e.getMessage());
                        throw new RuntimeException();
                    }
                    try {
                        EntityFactory.newMonster();
                    } catch (IOException e) {
                        LOGGER.warning("Could not create new Monster: " + e.getMessage());
                        throw new RuntimeException();
                    }
                });
            core.Game.userOnFrame(() -> debugger.execute());
            core.Game.addSystem(new AISystem());
            core.Game.addSystem(new CollisionSystem());
            core.Game.addSystem(new HealthSystem());
            core.Game.addSystem(new XPSystem());
            core.Game.addSystem(new ProjectileSystem());
            core.Game.addSystem(new MultiplayerSynchronizationSystem(gameScreen.multiplayerManager()));
            gameScreen.stopSystems();
        }
        catch (Exception ex) {
            LOGGER.severe("Failed to create game screen. " + ex.getMessage());
        }

        setScreen(menuScreen);
    }

    @Override
    public void onSinglePlayerModeChosen() {
        setScreen(gameScreen);
        gameScreen.runSinglePlayer();
    }

    @Override
    public void onMultiPlayerHostModeChosen() {
        setScreen(gameScreen);
        gameScreen.openToLan();
    }

    @Override
    public void onMultiPlayerClientModeChosen(final String hostAddress, final Integer port) {
        setScreen(gameScreen);
        gameScreen.joinMultiplayerSession(hostAddress, port);
    }

    /**
     * Set the title of the game window.
     *
     * <p>Part of the pre-run configuration: Manipulating this value will only result in changes
     * before {@link Dungeon#run} was executed.
     *
     * @param windowTitle: new title
     */
    public static void windowTitle(String windowTitle) {
        WINDOW_TITLE = windowTitle;
    }

    /**
     * The frames per second of the game. The FPS determine in which interval the update cycle of
     * the systems is triggered. Each system is updated once per frame. With an FPS of 30, each
     * system is updated 30 times per second.
     * <p>Part of the pre-run configuration: Manipulating this value will only result in changes
     * before {@link core.Dungeon#run} was executed.
     *
     * @param frameRate: the new fps of the game
     */
    public static void frameRate(int frameRate) {
        FRAME_RATE = frameRate;
    }

    /**
     * Get the current frame rate of the game
     *
     * @return current frame rate of the game
     */
    public static int frameRate() {
        return FRAME_RATE;
    }

    /**
     * Width of the game-window in pixel
     *
     * @return the width of the game-window im pixel
     */
    public static int windowWidth() {
        return WINDOW_WIDTH;
    }

    /**
     * Height of the game-window in pixel
     *
     * @return the height of the game-window im pixel
     */
    public static int windowHeight() {
        return WINDOW_HEIGHT;
    }

    /**
     * Set the width of the game window in pixels.
     *
     * @param windowWidth: the new width of the game window in pixels.
     */
    public static void windowWidth(int windowWidth) {
        WINDOW_WIDTH = windowWidth;
    }

    /**
     * Set the height of the game window in pixels.
     *
     * @param windowHeight: the new height of the game window in pixels.
     */
    public static void windowHeight(int windowHeight) {
        WINDOW_HEIGHT = windowHeight;
    }

    /**
     * Get if the game is currently in full screen mode
     *
     * @return true if the game is currently in full screen mode
     */
    public static boolean fullScreen() {
        return FULL_SCREEN;
    }

    /**
     * Set the window to fullscreen mode or windowed mode.
     *
     * @param fullscreen true for fullscreen, false for windowed
     */
    public static void fullScreen(boolean fullscreen) {
        FULL_SCREEN = fullscreen;
    }

    /**
     * Set if you want to disable or enable the audi of the game.
     *
     * <p>Part of the pre-run configuration: Manipulating this value will only result in changes
     * before {@link Dungeon#run} was executed.
     *
     * @param disableAudio true if you want to disable the audio, false (default) if not.
     */
    public static void disableAudio(boolean disableAudio) {
        DISABLE_AUDIO = disableAudio;
    }

    /**
     * Set the path to the logo of the game window.
     *
     * @param logoPath: path to the nwe logo as String
     */
    public static void logoPath(String logoPath) {
        LOGO_PATH = logoPath;
    }

    public static void run() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowSizeLimits(WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_WIDTH_MAX, WINDOW_HEIGHT_MAX);
        // The third and fourth parameters ("maxWidth" and "maxHeight") affect the resizing
        // behavior
        // of the window. If the window is enlarged or maximized, then it can assume these
        // dimensions at maximum. If you have a larger screen resolution than 9999x9999 pixels,
        // increase these parameters.
        config.setForegroundFPS(FRAME_RATE);
        config.setTitle(WINDOW_TITLE);
        config.setWindowIcon(LOGO_PATH);
        config.disableAudio(DISABLE_AUDIO);

        if (FULL_SCREEN) {
            config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        } else {
            config.setWindowedMode(WINDOW_WIDTH, WINDOW_HEIGHT);
        }

        new Lwjgl3Application(new Dungeon(), config);
    }
}
