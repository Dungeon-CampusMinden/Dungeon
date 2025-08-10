package starter;

import contrib.entities.HeroFactory;
import core.Game;
import core.configuration.KeyboardConfig;
import core.game.PreRunConfiguration;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dev client starter that connects to a local Netty server and runs the game.
 *
 * <p>Configures {@link PreRunConfiguration} for multiplayer client mode, initializes a the Netty
 * network handler via {@link core.Game#run()} (based on {@link PreRunConfiguration}) to dispatch
 * messages to the game dispatcher, spawns a local hero, and starts the game loop.
 */
public final class DevClient {
  private static final Logger LOG = LoggerFactory.getLogger(DevClient.class);

  public static void main(String[] args) throws IOException {
    // PreRun configuration for multiplayer client
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(false);
    PreRunConfiguration.networkServerAddress("127.0.0.1");
    PreRunConfiguration.networkPort(7777);
    PreRunConfiguration.username("Player1");

    // Game Settings
    Game.initBaseLogger(Level.ALL);
    DungeonLoader.addLevel(Tuple.of("maze", DungeonLevel.class));
    Game.loadConfig(new SimpleIPath("dungeon_config.json"), KeyboardConfig.class);
    Game.disableAudio(false);
    Game.frameRate(30);
    Game.windowTitle("Dev Client");

    // Spawn a local playable hero on setup
    Game.userOnSetup(
        () -> {
          try {
            Game.add(HeroFactory.newHero());
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });

    // Start the game
    Game.run();
  }
}
