package core.game;

import core.configuration.Configuration;
import core.utils.IVoidFunction;
import core.utils.logging.LoggerConfig;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * PreRunConfiguration class represents the pre-run configuration for the game. It includes various
 * settings such as window dimensions, frame rate, full-screen mode, and more. This class contains
 * all the necessary configurations that need to be set before the game starts.
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

    /** Getters and Setters for window dimensions. */
    public static int windowWidth() {
        return WINDOW_WIDTH;
    }

    public static void windowWidth(int windowWidth) {
        WINDOW_WIDTH = windowWidth;
    }

    public static int windowHeight() {
        return WINDOW_HEIGHT;
    }

    public static void windowHeight(int windowHeight) {
        WINDOW_HEIGHT = windowHeight;
    }

    /** Getters and Setters for frame rate. */
    public static int frameRate() {
        return FRAME_RATE;
    }

    public static void frameRate(int frameRate) {
        FRAME_RATE = frameRate;
    }

    /** Getters and Setters for full-screen mode. */
    public static boolean fullScreen() {
        return FULL_SCREEN;
    }

    public static void fullScreen(boolean fullscreen) {
        FULL_SCREEN = fullscreen;
    }

    /** Getters and Setters for window title. */
    public static String windowTitle() {
        return WINDOW_TITLE;
    }

    public static void windowTitle(String windowTitle) {
        WINDOW_TITLE = windowTitle;
    }

    /** Getters and Setters for logo path. */
    public static String logoPath() {
        return LOGO_PATH;
    }

    public static void logoPath(String logoPath) {
        LOGO_PATH = logoPath;
    }

    /** Getters and Setters for audio settings. */
    public static boolean disableAudio() {
        return DISABLE_AUDIO;
    }

    public static void disableAudio(boolean disableAudio) {
        DISABLE_AUDIO = disableAudio;
    }

    /** Getters and Setters for user-defined functions. */
    public static IVoidFunction userOnFrame() {
        return userOnFrame;
    }

    public static void userOnFrame(IVoidFunction userOnFrame) {
        PreRunConfiguration.userOnFrame = userOnFrame;
    }

    public static IVoidFunction userOnSetup() {
        return userOnSetup;
    }

    public static void userOnSetup(IVoidFunction userOnSetup) {
        PreRunConfiguration.userOnSetup = userOnSetup;
    }

    public static Consumer<Boolean> userOnLevelLoad() {
        return userOnLevelLoad;
    }

    public static void userOnLevelLoad(Consumer<Boolean> userOnLevelLoad) {
        PreRunConfiguration.userOnLevelLoad = userOnLevelLoad;
    }

    /**
     * Initialize the base logger. Will remove the console handler and put all log messages in the
     * log files.
     */
    public static void initBaseLogger() {
        LoggerConfig.initBaseLogger();
    }

    /**
     * Load the configuration from the given path. If the configuration has already been loaded, the
     * cached version will be used.
     *
     * @param pathAsString the path to the config file as a string
     * @param klass the class where the ConfigKey fields are located
     * @throws IOException if the file could not be read
     */
    public static void loadConfig(String pathAsString, Class<?>... klass) throws IOException {
        Configuration.loadAndGetConfiguration(pathAsString, klass);
    }
}
