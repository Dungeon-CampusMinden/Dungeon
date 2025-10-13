package starter;

import core.Game;
import core.game.PreRunConfiguration;
import core.level.DungeonLevel;
import core.level.loader.DungeonLoader;
import core.utils.Tuple;

/**
 * Minimal server starter to launch the Netty-based network service.
 *
 * <p>Usage: - Build: gradlew :dungeon:compileJava -x :dungeon:checkstyleMain -x
 * :dungeon:checkstyleTest - Run: gradlew :dungeon:runServerStarter (port 7777 by default)
 */
public class Server {

  public static void main(String[] args) {
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(true);
    DungeonLoader.addLevel(Tuple.of("maze", DungeonLevel.class));
    Game.run();
  }
}
