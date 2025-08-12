package starter;

import core.game.PreRunConfiguration;
import core.network.DefaultSnapshotTranslator;
import core.network.server.AuthoritativeServerLoop;
import core.network.server.ServerNetworkService;
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
    int port = PreRunConfiguration.networkPort();
    if (args != null && args.length > 0) {
      try {
        port = Integer.parseInt(args[0]);
      } catch (NumberFormatException ignored) {
      }
    }

    LOG.info(
        "Dungeon server starter ready. JDK: {}, port={}", System.getProperty("java.version"), port);
    ServerNetworkService service = new ServerNetworkService();
    service.start(port);
    var translator = new DefaultSnapshotTranslator();
    // Provide translator to both service and loop (loop requires it; service for other components)
    service.setSnapshotTranslator(translator);
    AuthoritativeServerLoop loop = new AuthoritativeServerLoop(service, translator);
    loop.start();
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  LOG.info("Stopping server...");
                  loop.stop();
                  service.stop();
                }));
  }
}
