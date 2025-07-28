package core.game;

import core.configuration.Configuration;
import core.utils.IVoidFunction;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.LoggerConfig;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.logging.Level;

/**
 * Offers API functions for the configuration of the game.
 *
 * <p>Includes various settings such as window dimensions, frame rate, full-screen mode, and more.
 * This class contains all the necessary configurations that need to be set before the game starts.
 *
 * <p>Use {@link #userOnFrame(IVoidFunction)}, {@link #userOnSetup(IVoidFunction)}, and {@link
 * #userOnLevelLoad(Consumer)} to configure event callbacks. This is the best way to include your
 * own program logic outside a {@link System}.
 *
 * <p>All API methods can also be accessed via the {@link core.Game} class.
 */
public final class PreRunConfiguration {

  // Multiplayer
  private static boolean MULTIPLAYER_ENABLED = false;
  private static boolean NETWORK_IS_SERVER = true;
  private static String NETWORK_SERVER_ADDRESS = "127.0.0.1";
  private static int NETWORK_PORT = 7777;

  private static int WINDOW_WIDTH = 1280;
  private static int WINDOW_HEIGHT = 720;
  private static int FRAME_RATE = 30;
  private static boolean FULL_SCREEN = false;

  private static boolean RESIZEABLE = true;
  private static String WINDOW_TITLE = "PM-Dungeon";
  private static IPath LOGO_PATH = new SimpleIPath("logo/cat_logo_35x35.png");
  private static boolean DISABLE_AUDIO = false;
  private static boolean DRAW_CHECK_PATTERN = true;
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
   * Checks if the game-window can be resized.
   *
   * @return True if the game-window can be resized. , false otherwise.
   */
  public static boolean resizeable() {
    return RESIZEABLE;
  }

  /**
   * Sets whether the game-window can be resized..
   *
   * @param resizeable True to enable resizing, false otherwise.
   */
  public static void resizeable(boolean resizeable) {
    RESIZEABLE = resizeable;
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
  public static IPath logoPath() {
    return LOGO_PATH;
  }

  /**
   * Sets the path to the game logo.
   *
   * @param logoPath The path to the game logo.
   */
  public static void logoPath(IPath logoPath) {
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
   * Enables or disables the check pattern drawing mode.
   *
   * @param enabled {@code true} to draw the level with a check pattern, {@code false} to draw it
   *     without
   */
  public static void enableCheckPattern(boolean enabled) {
    DRAW_CHECK_PATTERN = enabled;
  }

  /**
   * Checks if the check pattern drawing mode is enabled.
   *
   * @return {@code true} if the level will be drawn with a check pattern, {@code false} otherwise
   */
  public static boolean isCheckPatternEnabled() {
    return DRAW_CHECK_PATTERN;
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
  public static void userOnFrame(final IVoidFunction userOnFrame) {
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
  public static void userOnSetup(final IVoidFunction userOnSetup) {
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
  public static void userOnLevelLoad(final Consumer<Boolean> userOnLevelLoad) {
    PreRunConfiguration.userOnLevelLoad = userOnLevelLoad;
  }

  /**
   * Initialize the base logger.
   *
   * <p>Set a logging level, and remove the console handler, and write all log messages into the log
   * files.
   *
   * @param level Set logging level to {@code level}
   */
  public static void initBaseLogger(Level level) {
    LoggerConfig.initBaseLogger(level);
  }

  /**
   * Loads the configuration from the given path. If the configuration has already been loaded, the
   * cached version will be used.
   *
   * @param path The path to the config file.
   * @param klass The class where the ConfigKey fields are located.
   * @throws IOException If the file could not be read.
   */
  public static void loadConfig(final IPath path, Class<?>... klass) throws IOException {
    Configuration.loadAndGetConfiguration(path, klass);
  }

  /**
   * Checks if this instance is the server in a multiplayer game.
   *
   * @return True if this instance is the server, false otherwise.
   */
  public static boolean isNetworkServer() {
    return NETWORK_IS_SERVER;
  }

  /**
   * Sets whether this instance is the server in a multiplayer game.
   *
   * @param isServer True if this instance is the server, false otherwise.
   */
  public static void isNetworkServer(boolean isServer) {
    NETWORK_IS_SERVER = isServer;
  }

  /**
   * Gets the server address for multiplayer.
   *
   * @return The server address.
   */
  public static String networkServerAddress() {
    return NETWORK_SERVER_ADDRESS;
  }

  /**
   * Sets the server address for multiplayer.
   *
   * @param serverAddress The server address.
   */
  public static void networkServerAddress(String serverAddress) {
    NETWORK_SERVER_ADDRESS = serverAddress;
  }

  /**
   * Gets the network port for multiplayer.
   *
   * @return The network port.
   */
  public static int networkPort() {
    return NETWORK_PORT;
  }

  /**
   * Sets the network port for multiplayer.
   *
   * @param port The network port.
   */
  public static void networkPort(int port) {
    NETWORK_PORT = port;
  }

  /**
   * Checks if multiplayer is enabled.
   *
   * @return True if multiplayer is enabled, false otherwise.
   */
  public static boolean multiplayerEnabled() {
    return MULTIPLAYER_ENABLED;
  }

  /**
   * Sets whether multiplayer is enabled.
   *
   * @param multiplayerEnabled True to enable multiplayer, false otherwise.
   */
  public static void multiplayerEnabled(boolean multiplayerEnabled) {
    MULTIPLAYER_ENABLED = multiplayerEnabled;
  }
}
