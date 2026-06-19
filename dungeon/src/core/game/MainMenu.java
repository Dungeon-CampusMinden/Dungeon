package core.game;

import core.Game;

/**
 * Reusable entry point that boots an explicit project behind a {@link MainMenuScreen main menu}.
 *
 * <p>Instead of configuring the game and immediately calling {@link Game#run()}, an explicit
 * project implements a {@link GameStarter} and starts it through {@link #run(GameStarter)}. The
 * application starts up, shows the main menu as the first view, and only enters the core {@link
 * GameLoop} once the player chooses to host or join a game.
 *
 * <p>The engine is multiplayer-only: the main application always runs as a client, and the "Host
 * Game" option launches a dedicated server in a separate process.
 *
 * <p>Example:
 *
 * <pre>{@code
 * public static void main(String[] args) {
 *   MainMenu.run(new MyGameStarter());
 * }
 * }</pre>
 */
public final class MainMenu {

  private MainMenu() {}

  /**
   * Configures the client, then starts the application showing the main menu.
   *
   * @param starter the explicit project's integration
   */
  public static void run(GameStarter starter) {
    starter.configureClient();

    // The main application is always a multiplayer client; hosting spawns a separate server.
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(false);
    Game.windowTitle(starter.title());

    GameLoop.initialScreen(() -> new MainMenuScreen(starter));
    Game.run();
  }
}
