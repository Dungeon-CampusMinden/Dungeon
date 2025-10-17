package starter;

import contrib.utils.components.Debugger;
import core.Game;
import core.configuration.KeyboardConfig;
import core.game.PreRunConfiguration;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;

/**
 * Dev client starter that connects to a local Netty server and runs the game.
 *
 * <p>Configures {@link PreRunConfiguration} for multiplayer client mode, initializes a the Netty
 * network handler via {@link core.Game#run()} (based on {@link PreRunConfiguration}) to dispatch
 * messages to the game dispatcher, spawns a local hero, and starts the game loop.
 */
public final class DevClient {
  public static void main(String[] args) throws IOException {
    // PreRun configuration for multiplayer client
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(false);
    PreRunConfiguration.networkServerAddress("127.0.0.1");
    PreRunConfiguration.networkPort(7777);
    PreRunConfiguration.username("Player2");

    // Game Settings
    Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    Game.disableAudio(false);
    Game.frameRate(30);
    Game.windowTitle("Dev Client");
    Game.userOnSetup(
        () -> {
          Game.add(new Debugger());
          System.out.println("DevClient started");
        });

    // Start the game
    Game.run();
  }
}
