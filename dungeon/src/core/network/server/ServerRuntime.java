package core.network.server;

import core.game.PreRunConfiguration;
import core.network.SnapshotTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerRuntime {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServerRuntime.class);

  private final int port;
  private final SnapshotTranslator translator;

  private ServerTransport transport;
  private AuthoritativeServerLoop loop;

  public ServerRuntime(int port, SnapshotTranslator translator) {
    this.port = port;
    this.translator = translator;
  }

  public void start() {
    LOGGER.info("Starting server on port {}", PreRunConfiguration.networkPort());
    this.transport = new ServerTransport();
    this.transport.start(port);
    this.loop = new AuthoritativeServerLoop(transport, translator);
    this.loop.start();
  }

  public void stop() {
    if (loop != null) loop.stop();
    if (transport != null) transport.stop();
  }
}
