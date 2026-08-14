package engine.game;

import engine.Game;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reusable entry point that boots an explicit project behind a {@link MainMenuScreen main menu}.
 *
 * <p>An explicit project's {@code main} method only needs to set up logging, create a {@link
 * GameStarter} (menu/hosting integration), a {@link ClientStarter} and a {@link ServerStarter}, and
 * hand them together with the program arguments to {@link #run(String[], GameStarter,
 * ClientStarter, ServerStarter)}. {@code MainMenu} then decides whether this process is a dedicated
 * server or a client, applies the appropriate configuration, and starts the application.
 *
 * <p>The normal client runs behind the menu, and the "Host Game" option launches a dedicated server
 * in a separate process. Projects can also provide a true-singleplayer starter for the hidden
 * level-editor menu entry or the {@code --leveleditor} launch flag.
 *
 * <p>Example:
 *
 * <pre>{@code
 * public static void main(String[] args) {
 *   setupLogging();
 *   GameStarter game = GameStarter.builder("My Game", MyGame.class).build();
 *   ClientStarter client = ClientStarter.builder(MyGame::clientSetup).build();
 *   ServerStarter server = ServerStarter.builder(MyGame::serverSetup).build();
 *   MainMenu.run(args, game, client, server);
 * }
 * }</pre>
 */
public final class MainMenu {

  /** Resource that, when present and containing {@link #SERVER_PROPERTY}, forces server mode. */
  private static final String APPLICATION_PROPERTIES = "/application.properties";

  /** Property key in {@link #APPLICATION_PROPERTIES} that marks a server distribution. */
  private static final String SERVER_PROPERTY = "server";

  private static final String LEVEL_EDITOR_ARGUMENT = "--leveleditor";

  private MainMenu() {}

  /**
   * Boots the project: runs as a dedicated server if {@link #shouldRunMpServer(String[])}, starts
   * the level editor in true-singleplayer for {@code --leveleditor}, otherwise configures the
   * client and shows the main menu.
   *
   * <p>The {@link GameStarter#language() configured language} is applied first, so the menu and the
   * launched game are already shown in the desired language.
   *
   * @param args the program arguments (checked for server and level-editor mode flags)
   * @param game the menu/hosting integration
   * @param client the multiplayer client configuration
   * @param server the dedicated server configuration
   */
  public static void run(
      String[] args, GameStarter game, ClientStarter client, ServerStarter server) {
    if (shouldRunMpServer(args)) {
      Game.windowTitle(game.title());
      Game.localization().currentLanguage(game.language());
      server.apply();
      Game.run();
      return;
    }

    if (containsArgument(args, LEVEL_EDITOR_ARGUMENT)) {
      SingleplayerStarter singleplayer =
          game.singleplayer()
              .orElseThrow(
                  () ->
                      new IllegalStateException(
                          "Singleplayer launch requested, but no singleplayer starter is configured."));
      Game.windowTitle(game.title());
      Game.localization().currentLanguage(game.language());
      client.apply();
      singleplayer.applyLevelEditor();
      Game.run();
      return;
    }

    run(game, client);
  }

  /**
   * Configures the client and shows the menu for an application that handles server mode itself.
   *
   * @param game the menu/hosting integration
   * @param client the multiplayer client configuration
   */
  public static void run(GameStarter game, ClientStarter client) {
    Game.windowTitle(game.title());
    Game.localization().currentLanguage(game.language());
    client.apply();
    GameLoop.initialScreen(() -> new MainMenuScreen(game));
    Game.run();
  }

  /**
   * Decides whether this process should run as a dedicated multiplayer server.
   *
   * <p>This is the case when the program arguments contain {@link ServerProcess#SERVER_ARGUMENT}
   * (used by the dev server task and by the hosting child process) or when a packaged server
   * distribution bundles an {@code application.properties} resource that contains the {@code
   * server} key.
   *
   * @param args the program arguments
   * @return {@code true} if the process should run as a server
   */
  private static boolean shouldRunMpServer(String[] args) {
    if (args != null) {
      for (String arg : args) {
        if (ServerProcess.SERVER_ARGUMENT.equals(arg)) {
          return true;
        }
      }
    }
    return serverPropertyPresent();
  }

  private static boolean containsArgument(String[] args, String expected) {
    if (args == null) {
      return false;
    }
    for (String arg : args) {
      if (expected.equals(arg)) {
        return true;
      }
    }
    return false;
  }

  private static boolean serverPropertyPresent() {
    try (InputStream propertiesStream =
        MainMenu.class.getResourceAsStream(APPLICATION_PROPERTIES)) {
      if (propertiesStream == null) {
        return false;
      }
      Properties properties = new Properties();
      properties.load(propertiesStream);
      return properties.containsKey(SERVER_PROPERTY);
    } catch (IOException e) {
      return false;
    }
  }
}
