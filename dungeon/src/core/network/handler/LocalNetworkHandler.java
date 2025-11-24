package core.network.handler;

import contrib.entities.HeroController;
import core.Game;
import core.game.PreRunConfiguration;
import core.network.ConnectionListener;
import core.network.MessageDispatcher;
import core.network.NetworkException;
import core.network.SnapshotTranslator;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.InputMessage;
import core.network.server.ClientState;
import core.network.server.Session;
import core.utils.logging.DungeonLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A mock network handler for single-player/local or test mode that simulates network behavior
 * without actual network communication.
 *
 * <p>This handler processes game logic locally without real network communication, acting as both
 * client and server to keep architecture consistent between modes.
 */
public class LocalNetworkHandler implements INetworkHandler {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(LocalNetworkHandler.class);

  // Message / translation utilities
  private final MessageDispatcher dispatcher = new MessageDispatcher();
  private volatile SnapshotTranslator translator;

  // Dummy session and client state
  private final byte[] dummySessionToken = new byte[] {0, 1, 2, 3, 4, 5, 6, 7};
  private final ClientState dummyState =
      new ClientState((short) 0, PreRunConfiguration.username(), 0, dummySessionToken);
  private final Session dummySession =
      new Session(
          null,
          (addr, msg) -> send((short) 0, null, true),
          (ctx, msg) -> send((short) 0, null, true));

  // Connection listeners
  private final List<ConnectionListener> connectionListeners = new ArrayList<>();

  // Lifecycle flags
  private boolean isRunning = false;
  private boolean isInitialized = false;

  @Override
  public void initialize(boolean isServer, String serverAddress, int port, String username)
      throws NetworkException {
    this.isInitialized = true;
    dummySession.attachClientState(dummyState);
  }

  @Override
  public CompletableFuture<Boolean> send(short clientId, NetworkMessage message, boolean reliable) {
    return CompletableFuture.completedFuture(true);
  }

  @Override
  public CompletableFuture<Boolean> broadcast(NetworkMessage message, boolean reliable) {
    // No op
    return CompletableFuture.completedFuture(true);
  }

  @Override
  public void sendInput(InputMessage input) {
    Game.player()
        .ifPresent(
            hero -> {
              HeroController.enqueueInput(dummyState, input);
              HeroController.drainAndApplyInputs(); // Apply immediately in local mode
            });
  }

  @Override
  public void start() {
    if (!isInitialized) {
      LOGGER.error("LocalNetworkHandler cannot start because it is not initialized.");
      return;
    }
    this.isRunning = true;
    Game.player().ifPresent(dummyState::playerEntity);
    LOGGER.info("LocalNetworkHandler started.");
    notifyConnected();
  }

  @Override
  public void shutdown(String reason) {
    this.isRunning = false;
    this.isInitialized = false;
    notifyDisconnected(reason);
    LOGGER.info("LocalNetworkHandler shutdown complete. Reason: {}", reason);
  }

  @Override
  public boolean isConnected() {
    return isRunning && isInitialized;
  }

  @Override
  public boolean isServer() {
    return true;
  }

  @Override
  public MessageDispatcher messageDispatcher() {
    return dispatcher;
  }

  @Override
  public SnapshotTranslator snapshotTranslator() {
    if (translator == null) {
      throw new IllegalStateException(
          "SnapshotTranslator not set on INetworkHandler. Set via "
              + "snapshotTranslator(...) before starting network or provide translator in "
              + "starter.");
    }
    return translator;
  }

  @Override
  public void snapshotTranslator(SnapshotTranslator translator) {
    if (translator != null) this.translator = translator;
  }

  @Override
  public void pollAndDispatch() {
    // No op
  }

  @Override
  public synchronized void addConnectionListener(ConnectionListener listener) {
    if (listener == null) return;
    connectionListeners.add(listener);
  }

  @Override
  public synchronized void removeConnectionListener(ConnectionListener listener) {
    if (listener == null) return;
    connectionListeners.remove(listener);
  }

  @Override
  public Session session() {
    return dummySession;
  }

  private void notifyConnected() {
    List<ConnectionListener> snapshot;
    synchronized (this) {
      snapshot = new ArrayList<>(connectionListeners);
    }
    for (ConnectionListener listener : snapshot) {
      try {
        listener.onConnected();
      } catch (Exception e) {
        LOGGER.warn("ConnectionListener.onConnected threw", e);
      }
    }
  }

  private void notifyDisconnected(String reason) {
    List<ConnectionListener> snapshot;
    synchronized (this) {
      snapshot = new ArrayList<>(connectionListeners);
    }
    for (ConnectionListener listener : snapshot) {
      try {
        listener.onDisconnected(reason);
      } catch (Exception e) {
        LOGGER.warn("ConnectionListener.onDisconnected threw", e);
      }
    }
  }
}
