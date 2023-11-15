package core.game;

import core.configuration.Configuration;
import core.utils.IVoidFunction;
import core.utils.logging.LoggerConfig;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * The `PreRunConfiguration` class represents the pre-run configuration for the game. It includes
 * various settings such as window dimensions, frame rate, full-screen mode, and more. This class
 * contains all the necessary configurations that need to be set before the game starts.
 */
public class PreRunConfiguration {

    // Window Dimensions
    private static int WINDOW_WIDTH = 1280;
    private static int WINDOW_HEIGHT = 720;

    // Frame Rate
    private static int FRAME_RATE = 30;

    // Full-Screen Mode
    private static boolean FULL_SCREEN = false;

    // Window Title
    private static String WINDOW_TITLE = "PM-Dungeon";

    // Logo Path
    private static String LOGO_PATH = "logo/cat_logo_35x35.png";

    // Audio Settings
    private static boolean DISABLE_AUDIO = false;

    // User-Defined Functions
    private static IVoidFunction userOnFrame = () -> {};
    private static IVoidFunction userOnSetup = () -> {};
    private static Consumer<Boolean> userOnLevelLoad = (b) -> {};

    /**
     * Gets the width of the game window.
     *
     * @return The width of the game window.
     */
    public static int windowWidth() {
        return WINDOW_WIDTH;
    }

    /**
     * Sets the width of the game window.
     *
     * @param windowWidth The width of the game window.
     */
    public static void windowWidth(int windowWidth) {
        WINDOW_WIDTH = windowWidth;
    }

    /**
     * Gets the height of the game window.
     *
     * @return The height of the game window.
     */
    public static int windowHeight() {
        return WINDOW_HEIGHT;
    }

    /**
     * Sets the height of the game window.
     *
     * @param windowHeight The height of the game window.
     */
    public static void windowHeight(int windowHeight) {
        WINDOW_HEIGHT = windowHeight;
    }

    /**
     * Gets the frame rate of the game.
     *
     * @return The frame rate of the game.
     */
    public static int frameRate() {
        return FRAME_RATE;
    }

    /**
     * Sets the frame rate of the game.
     *
     * @param frameRate The frame rate of the game.
     */
    public static void frameRate(int frameRate) {
        FRAME_RATE = frameRate;
    }

    /**
     * Checks if the game is in full-screen mode.
     *
     * @return True if the game is in full-screen mode, false otherwise.
     */
    public static boolean fullScreen() {
        return FULL_SCREEN;
    }

    /**
     * Sets whether the game should be in full-screen mode.
     *
     * @param fullscreen True to enable full-screen mode, false otherwise.
     */
    public static void fullScreen(boolean fullscreen) {
        FULL_SCREEN = fullscreen;
    }

    /**
     * Gets the title of the game window.
     *
     * @return The title of the game window.
     */
    public static String windowTitle() {
        return WINDOW_TITLE;
    }

    /**
     * Sets the title of the game window.
     *
     * @param windowTitle The title of the game window.
     */
    public static void windowTitle(String windowTitle) {
        WINDOW_TITLE = windowTitle;
    }

    /**
     * Gets the path to the game logo.
     *
     * @return The path to the game logo.
     */
    public static String logoPath() {
        return LOGO_PATH;
    }

    /**
     * Sets the path to the game logo.
     *
     * @param logoPath The path to the game logo.
     */
    public static void logoPath(String logoPath) {
        LOGO_PATH = logoPath;
    }

    /**
     * Checks if audio is disabled.
     *
     * @return True if audio is disabled, false otherwise.
     */
    public static boolean disableAudio() {
        return DISABLE_AUDIO;
    }

    /**
     * Sets whether audio should be disabled.
     *
     * @param disableAudio True to disable audio, false otherwise.
     */
    public static void disableAudio(boolean disableAudio) {
        DISABLE_AUDIO = disableAudio;
    }

    /**
     * Gets the user-defined function for frame logic.
     *
     * @return The user-defined function for frame logic.
     */
    public static IVoidFunction userOnFrame() {
        return userOnFrame;
    }

    /**
     * Sets the user-defined function for frame logic.
     *
     * @param userOnFrame The user-defined function for frame logic.
     */
    public static void userOnFrame(IVoidFunction userOnFrame) {
        PreRunConfiguration.userOnFrame = userOnFrame;
    }

    /**
     * Gets the user-defined function for setup logic.
     *
     * @return The user-defined function for setup logic.
     */
    public static IVoidFunction userOnSetup() {
        return userOnSetup;
    }

    /**
     * Sets the user-defined function for setup logic.
     *
     * @param userOnSetup The user-defined function for setup logic.
     */
    public static void userOnSetup(IVoidFunction userOnSetup) {
        PreRunConfiguration.userOnSetup = userOnSetup;
    }

    /**
     * Gets the user-defined function for level load logic.
     *
     * @return The user-defined function for level load logic.
     */
    public static Consumer<Boolean> userOnLevelLoad() {
        return userOnLevelLoad;
    }

    /**
     * Sets the user-defined function for level load logic.
     *
     * @param userOnLevelLoad The user-defined function for level load logic.
     */
    public static void userOnLevelLoad(Consumer<Boolean> userOnLevelLoad) {
        PreRunConfiguration.userOnLevelLoad = userOnLevelLoad;
    }

    /**
     * Initializes the base logger. Removes the console handler and puts all log messages in the log
     * files.
     */
    public static void initBaseLogger() {
        LoggerConfig.initBaseLogger();
    }

    /**
     * Loads the configuration from the given path. If the configuration has already been loaded,
     * the cached version will be used.
     *
     * @param pathAsString The path to the config file as a string.
     * @param klass The class where the ConfigKey fields are located.
     * @throws IOException If the file could not be read.
     */
    public static void loadConfig(String pathAsString, Class<?>... klass) throws IOException {
        Configuration.loadAndGetConfiguration(pathAsString, klass);
    }
}
