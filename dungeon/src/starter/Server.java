package starter;

import core.Game;
import core.game.PreRunConfiguration;

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
    Game.run();
  }
}
