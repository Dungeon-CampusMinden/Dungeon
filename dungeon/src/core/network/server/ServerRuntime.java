package core.network.server;

import core.game.PreRunConfiguration;
import core.network.messages.NetworkMessage;
import core.utils.logging.DungeonLogger;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public final class ServerRuntime {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ServerRuntime.class);
  private static final Random RANDOM = new Random();

  /** The unique session ID for this server instance, randomly generated on startup. */
  public static final int SESSION_ID = RANDOM.nextInt();

  private final int port;

  private ServerTransport transport;
  private AuthoritativeServerLoop loop;

  public ServerRuntime(int port) {
    this.port = port;
  }

  public void start() {
    LOGGER.info("Starting server on port {}", PreRunConfiguration.networkPort());
    this.transport = new ServerTransport();
    this.transport.start(port);
    this.loop = new AuthoritativeServerLoop(transport);
    this.loop.start();
  }

  public void stop() {
    if (loop != null) loop.stop();
    if (transport != null) transport.stop();
  }

  public void broadcastMessage(NetworkMessage message, boolean reliable) {
    if (loop != null) {
      this.transport.broadcast(message, reliable);
    } else {
      LOGGER.warn("Server not initialized, cannot broadcast message");
    }
  }

  public CompletableFuture<Boolean> sendMessage(
      short clientId, NetworkMessage message, boolean reliable) {
    if (loop != null) {
      Session session = transport.clientIdToSessionMap().get(clientId);
      if (session == null) {
        LOGGER.warn("No session found for clientId {}, cannot send message", clientId);
        return CompletableFuture.completedFuture(false);
      }
      return session.sendMessage(message, reliable);
    } else {
      LOGGER.warn("Server not initialized, cannot send message to client {}", clientId);
      return CompletableFuture.completedFuture(false);
    }
  }
}
