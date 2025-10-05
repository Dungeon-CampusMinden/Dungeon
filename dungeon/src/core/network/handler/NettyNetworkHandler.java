package core.network.handler;

import core.network.ConnectionListener;
import core.network.DefaultSnapshotTranslator;
import core.network.MessageDispatcher;
import core.network.SnapshotTranslator;
import core.network.client.ClientNetwork;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.InputMessage;
import core.network.server.ServerRuntime;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyNetworkHandler implements INetworkHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(NettyNetworkHandler.class);

  private boolean serverMode;
  private int port;

  private final ClientNetwork client = new ClientNetwork();
  private ServerRuntime server;
  private SnapshotTranslator translator;

  @Override
  public void initialize(boolean isServer, String serverAddress, int port, String username) {
    this.serverMode = isServer;
    this.port = port;
    if (!serverMode) {
      client.initialize(serverAddress, port, username);
      if (translator != null) client.setSnapshotTranslator(translator);
    }
  }

  @Override
  public CompletableFuture<Boolean> send(int clientId, NetworkMessage message, boolean reliable) {
    if (serverMode) {
      return server.sendMessage(clientId, message, reliable);
    } else return client.sendReliable(message);
  }

  @Override
  public void broadcast(NetworkMessage message, boolean reliable) {
    if (serverMode) {
      server.broadcastMessage(message, reliable);
    } else {
      throw new UnsupportedOperationException("Broadcast is not supported in client mode.");
    }
  }

  @Override
  public void start() {
    if (serverMode) {
      SnapshotTranslator t = translator != null ? translator : new DefaultSnapshotTranslator();
      server = new ServerRuntime(port, t);
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
  public int getAssignedClientId() {
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
    if (serverMode) {
      if (translator == null) {
        throw new IllegalStateException(
            "SnapshotTranslator not set for server mode. Call "
                + "setSnapshotTranslator(...) before start().");
      }
      return translator;
    }
    return client.snapshotTranslator();
  }

  @Override
  public void setSnapshotTranslator(SnapshotTranslator translator) {
    if (translator == null) return;
    this.translator = translator;
    if (!serverMode) client.setSnapshotTranslator(translator);
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
  public void _setRawMessageConsumer(
      BiConsumer<ChannelHandlerContext, NetworkMessage> rawMessageConsumer) {
    if (!serverMode) client.setRawMessageConsumer(rawMessageConsumer);
  }

  @Override
  public void pollAndDispatch() {
    if (!serverMode) client.pollAndDispatch();
  }
}
