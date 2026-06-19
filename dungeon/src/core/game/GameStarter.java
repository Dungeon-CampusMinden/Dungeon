package core.game;

import com.badlogic.gdx.graphics.Color;
import java.util.Optional;

/**
 * Describes how a concrete game integrates with the reusable {@link MainMenu}.
 *
 * <p>An explicit project provides a single {@link GameStarter} implementation and hands it to
 * {@link MainMenu#run(GameStarter)}. The main menu starts the application, shows itself as the
 * first view, and uses the starter to:
 *
 * <ul>
 *   <li>{@link #configureClient() configure} the multiplayer client once before the window opens,
 *   <li>launch a dedicated server child process for the "Host Game" option (see {@link
 *       #serverMainClass()} and {@link #serverArguments()}),
 *   <li>transition into the client view for both "Host Game" and "Join Game".
 * </ul>
 */
public interface GameStarter {

  /**
   * Human-readable title of the game.
   *
   * <p>Used for the game window title and the heading shown in the main menu.
   *
   * @return the game title
   */
  String title();

  /**
   * An optional background image shown in the main menu, scaled to cover the whole screen.
   *
   * @return the internal path string to the background image, or an empty {@link Optional} for no
   *     background
   */
  default Optional<String> backgroundImage() {
    return Optional.empty();
  }

  /**
   * Accent color used by the main menu for the big game title.
   *
   * @return title accent color
   */
  default Color accentColor() {
    return Color.WHITE;
  }

  /**
   * Configures the multiplayer client before the window opens.
   *
   * <p>This is where an explicit project performs the same pre-run configuration it would otherwise
   * do in its {@code main} method (register levels, load the keyboard config, set the snapshot
   * translator/entity spawn strategy, register a {@link
   * PreRunConfiguration#userOnSetup(core.utils.IVoidFunction) setup callback}, etc.). It is invoked
   * exactly once, before the main menu is shown.
   */
  void configureClient();

  /**
   * The main class to launch for the dedicated server child process when the player chooses "Host
   * Game".
   *
   * <p>The class is started in a separate JVM with {@link #serverArguments()} appended. Reusing the
   * project's existing {@code --server} entry point is the recommended approach.
   *
   * @return the server main class
   */
  Class<?> serverMainClass();

  /**
   * Arguments passed to the {@link #serverMainClass() server main class} when hosting.
   *
   * @return the server process arguments (defaults to {@code --server})
   */
  default String[] serverArguments() {
    return new String[] {ServerProcess.SERVER_ARGUMENT};
  }

  /**
   * The port the hosted server should listen on and the client should connect to.
   *
   * @return the local server port (defaults to the configured {@link
   *     PreRunConfiguration#networkPort()})
   */
  default int localServerPort() {
    return PreRunConfiguration.networkPort();
  }
}
