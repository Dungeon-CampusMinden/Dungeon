package starter;

import contrib.utils.components.Debugger;
import core.Game;
import core.configuration.KeyboardConfig;
import core.game.PreRunConfiguration;
import core.network.ConnectionListener;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;

/** The main class for the Multiplayer Client for development and testing purposes. */
public final class MultiplayerClient {

  /**
   * Main method to start the dev client.
   *
   * @param args command line arguments
   * @throws IOException if an I/O error occurs
   */
  public static void main(String[] args) throws IOException {
    // PreRun configuration for multiplayer client
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(false);
    PreRunConfiguration.networkPort(7777);

    // Game Settings
    Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    Game.disableAudio(false);
    Game.frameRate(60);
    Game.userOnSetup(
        () -> {
          Game.add(new Debugger());
          System.out.println("DevClient started");
          Game.network()
              .addConnectionListener(
                  new ConnectionListener() {
                    @Override
                    public void onConnected() {
                      Game.windowTitle("Client - " + PreRunConfiguration.username());
                    }

                    @Override
                    public void onDisconnected(String reason) {}
                  });
        });

    // Start the game
    Game.run();
  }
}
