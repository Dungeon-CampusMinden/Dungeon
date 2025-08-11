package core.network.handler;

import core.network.ConnectionListener;
import core.network.MessageDispatcher;
import core.network.NetworkException;
import core.network.SnapshotTranslator;
import core.network.messages.c2s.InputMessage;
import core.network.messages.NetworkMessage;
import java.util.function.Consumer;

/**
 * Central handler for sending game-related messages.
 *
 * <p>Abstracts whether messages are sent over the network or processed locally. In single-player,
 * this directly invokes game logic. In multiplayer, this sends messages to the server.
 *
 * @see LocalNetworkHandler Single-player handler that processes messages locally.
 */
public interface INetworkHandler {
  /**
   * Initializes the handler.
   *
   * @param isServer True if this instance should act as a server.
   * @param serverAddress The address to connect to (if client). Ignored if server.
   * @param port The port to use for communication.
   * @param username The username for the connection.
   */
  void initialize(boolean isServer, String serverAddress, int port,String username) throws NetworkException;

  /** Convenience overload that defaults to unreliable delivery (UDP-friendly). */
  void sendInput(InputMessage input);

  /** Starts the handler's processing loop (if applicable). */
  void start();

  /** Stops the handler and cleans up resources. */
  void shutdown();

  /**
   * Checks if the handler is currently connected (relevant for client).
   *
   * @return true if connected, false otherwise.
   */
  boolean isConnected();

  /**
   * Checks if the handler is running as a server.
   *
   * @return true if server, false otherwise.
   */
  boolean isServer();

  /**
   * Retrieves the dispatcher responsible for handling incoming messages.
   *
   * <p>Game code or server should register their specific message handlers with this dispatcher.
   *
   * @return The MessageDispatcher instance.
   */
  MessageDispatcher messageDispatcher();

  /**
   * Returns the {@link SnapshotTranslator} used to build/apply snapshots.
   *
   * <p>Callers must set a translator via {@link #setSnapshotTranslator(SnapshotTranslator)} before
   * first use. Implementations must not lazily create a default; if unset, they should throw an
   * {@link IllegalStateException} with a clear, actionable message.
   */
  SnapshotTranslator snapshotTranslator();

  /**
   * Sets the {@link SnapshotTranslator} to use for snapshot build/application.
   *
   * @param translator instance to use; implementations may ignore null (keeping the current one)
   */
  void setSnapshotTranslator(SnapshotTranslator translator);

  /**
   * Internal method: Sets the consumer for raw incoming messages.
   *
   * <p>This method is intended for the internal use of the NetworkHandler implementation (e.g.,
   * KryoNetHandler) to feed raw messages into the MessageDispatcher. Game code should use {@link
   * #messageDispatcher()} to register specific handlers.
   *
   * @param rawMessageConsumer A consumer that processes raw incoming messages.
   */
  void _setRawMessageConsumer(Consumer<NetworkMessage> rawMessageConsumer);

  /**
   * Registers a listener for connection lifecycle events.
   *
   * <p>Implementations must ensure that listener callbacks are invoked on the game loop thread, not
   * on IO/transport threads.
   *
   * @param listener the listener to add
   */
  void addConnectionListener(ConnectionListener listener);

  /**
   * Unregisters a previously registered connection listener.
   *
   * @param listener the listener to remove
   */
  void removeConnectionListener(ConnectionListener listener);

  /**
   * Drains any queued inbound network messages and dispatches them on the game loop thread.
   *
   * <p>Default is a no-op; implementations with IO threads should override this and deliver
   * messages to {@link #messageDispatcher()} or the raw consumer set via {@link
   * #_setRawMessageConsumer(Consumer)}.
   */
  default void pollAndDispatch() {}
}
