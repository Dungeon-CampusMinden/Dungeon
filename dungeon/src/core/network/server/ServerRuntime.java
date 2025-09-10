package core.network.server;

import core.game.PreRunConfiguration;
import core.network.SnapshotTranslator;
import core.network.messages.NetworkMessage;
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

  public void broadcastMessage(NetworkMessage message, boolean reliable) {
    if (loop != null) {
      this.loop.broadcast(message, reliable);
    } else {
      LOGGER.warn("Server loop not initialized, cannot broadcast message");
    }
  }

  public void sendMessage(NetworkMessage message, int clientId) {
    if (loop != null) {
      this.loop.sendToClient(clientId, message);
    } else {
      LOGGER.warn("Server loop not initialized, cannot send message to client {}", clientId);
    }
  }
}
