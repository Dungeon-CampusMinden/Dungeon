package starter;

import core.Game;
import core.game.PreRunConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal server starter to launch the Netty-based network service.
 *
 * <p>Usage: - Build: gradlew :dungeon:compileJava -x :dungeon:checkstyleMain -x
 * :dungeon:checkstyleTest - Run: gradlew :dungeon:runServerStarter (port 7777 by default)
 */
public class Server {
  private static final Logger LOG = LoggerFactory.getLogger(Server.class);

  public static void main(String[] args) {
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(true);
    Game.run();
  }
}
