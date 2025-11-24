package core.network.handler;

import core.network.*;
import core.network.client.ClientNetwork;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.InputMessage;
import core.network.server.ServerRuntime;
import core.network.server.Session;
import core.utils.logging.DungeonLogger;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Unified network handler implementing dual-mode (server/client) networking capabilities via
 * Netty-based communication.
 *
 * <p>Operates in either server or client mode, determined at initialization. Server mode manages
 * multiple client connections through {@link ServerRuntime}; client mode manages a single
 * connection to a remote server through {@link ClientNetwork}. A common interface abstracts
 * mode-specific behavior, with unsupported operations throwing {@link
 * UnsupportedOperationException}.
 *
 * <p>Responsibilities:
 *
 * <ul>
 *   <li>Lifecycle management: initialization, start, shutdown
 *   <li>Message routing: send, broadcast, input dispatch
 *   <li>Connection state: connectivity checks, client ID assignment
 *   <li>Message processing: dispatch coordination and snapshot translation
 *   <li>Event handling: connection listeners and raw message consumers
 * </ul>
 *
 * @see INetworkHandler
 * @see ClientNetwork
 * @see ServerRuntime
 */
public class NettyNetworkHandler implements INetworkHandler {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(NettyNetworkHandler.class);

  private boolean serverMode;
  private int port;

  private final ClientNetwork client = new ClientNetwork();
  private ServerRuntime server;
  private SnapshotTranslator translator;

  @Override
  public void initialize(boolean isServer, String serverAddress, int port, String username)
      throws NetworkException {
    this.serverMode = isServer;
    this.port = port;
    if (!serverMode) {
      client.initialize(serverAddress, port, username);
    }
  }

  @Override
  public CompletableFuture<Boolean> send(short clientId, NetworkMessage message, boolean reliable) {
    if (serverMode) {
      return server.sendMessage(clientId, message, reliable);
    } else return client.sendReliable(message);
  }

  @Override
  public CompletableFuture<Boolean> broadcast(NetworkMessage message, boolean reliable) {
    if (serverMode) {
      return server.broadcastMessage(message, reliable);
    } else {
      throw new UnsupportedOperationException("Broadcast is not supported in client mode.");
    }
  }

  @Override
  public void start() {
    if (serverMode) {
      server = new ServerRuntime(port);
      server.start();
    } else {
      client.start();
    }
  }

  @Override
  public void shutdown(String reason) {
    if (serverMode) {
      if (server != null) server.stop();
    } else {
      client.shutdown(reason);
    }
  }

  @Override
  public boolean isConnected() {
    return serverMode || client.isConnected();
  }

  @Override
  public boolean isServer() {
    return serverMode;
  }

  @Override
  public int assignedClientId() {
    return serverMode ? 0 : clientIdSafe();
  }

  private int clientIdSafe() {
    try {
      return client.clientId();
    } catch (Exception e) {
      LOGGER.debug("clientId not available yet", e);
      return 0;
    }
  }

  @Override
  public MessageDispatcher messageDispatcher() {
    return client.dispatcher();
  }

  @Override
  public SnapshotTranslator snapshotTranslator() {
    if (translator == null) {
      throw new IllegalStateException("SnapshotTranslator has not been set.");
    }
    return translator;
  }

  @Override
  public void snapshotTranslator(SnapshotTranslator translator) {
    this.translator = Objects.requireNonNull(translator, "translator cannot be null");
  }

  @Override
  public void sendInput(InputMessage input) {
    if (serverMode) return;
    client.sendUnreliableInput(input);
  }

  @Override
  public void addConnectionListener(ConnectionListener listener) {
    if (!serverMode) client.addConnectionListener(listener);
  }

  @Override
  public void removeConnectionListener(ConnectionListener listener) {
    if (!serverMode) client.removeConnectionListener(listener);
  }

  @Override
  public Session session() {
    if (serverMode)
      throw new UnsupportedOperationException("Session not available in server mode.");
    return client.session();
  }

  @Override
  public void pollAndDispatch() {
    if (!serverMode) client.pollAndDispatch();
    else server.pollAndDispatch();
  }
}
