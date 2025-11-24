package core.network.handler;

import core.network.ConnectionListener;
import core.network.MessageDispatcher;
import core.network.NetworkException;
import core.network.SnapshotTranslator;
import core.network.messages.NetworkMessage;
import core.network.messages.c2s.InputMessage;
import core.network.server.Session;
import java.util.concurrent.CompletableFuture;

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
  void initialize(boolean isServer, String serverAddress, int port, String username)
      throws NetworkException;

  /**
   * Sends a {@link NetworkMessage} to a specific client (if server) or to the server (if client).
   *
   * @param clientId The ID of the client to send the message to. Ignored if this is a client
   *     instance.
   * @param message The message to send.
   * @param reliable True to send via a reliable channel, false for unreliable.
   * @return A Future that completes with true if the message was sent successfully acknowledged
   *     (for reliable messages), or false otherwise. For unreliable messages, the Future completes
   *     immediately with true.
   */
  CompletableFuture<Boolean> send(short clientId, NetworkMessage message, boolean reliable);

  /**
   * Sends a {@link NetworkMessage} to all connected clients (if server).
   *
   * <p>For client instances, this method is unsupported and will throw an {@link
   * UnsupportedOperationException}.
   *
   * @param message The message to broadcast.
   * @param reliable True to send via a reliable channel, false for unreliable.
   * @return A Future that completes with true if the message was sent successfully and acknowledged
   *     by all clients (for reliable messages), or false otherwise. For unreliable messages, the
   *     Future completes immediately with true.
   */
  CompletableFuture<Boolean> broadcast(NetworkMessage message, boolean reliable);

  /**
   * Sends an input message to the server (if client) via the unreliable channel.
   *
   * @param input The input message to send.
   */
  void sendInput(InputMessage input);

  /** Starts the handler's processing loop. */
  void start();

  /** Stops the handler and cleans up resources. */
  default void shutdown() {
    shutdown("No reason given.");
  }

  /**
   * Stops the handler and cleans up resources.
   *
   * @param reason The reason for shutdown, used for logging or debugging.
   */
  void shutdown(String reason);

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
   * Returns the assigned client id after a successful handshake (clients only).
   *
   * <p>Implementations that are not clients may return 0.
   *
   * @return The assigned client id, or 0 if not applicable.
   */
  default int assignedClientId() {
    return 0;
  }

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
   * <p>Callers must set a translator via {@link #snapshotTranslator(SnapshotTranslator)} before
   * first use. Implementations must not lazily create a default; if unset, they should throw an
   * {@link IllegalStateException} with a clear, actionable message.
   *
   * @return the current SnapshotTranslator
   */
  SnapshotTranslator snapshotTranslator();

  /**
   * Sets the {@link SnapshotTranslator} to use for snapshot build/application.
   *
   * @param translator instance to use; implementations may ignore null (keeping the current one)
   */
  void snapshotTranslator(SnapshotTranslator translator);

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
   * Returns the current {@link Session} if connected (clients only).
   *
   * <p>For server implementations, this may return null or throw an {@link
   * UnsupportedOperationException}.
   *
   * @return the current Session, or null if not connected or not a client
   */
  Session session();

  /**
   * Drains any queued inbound network messages and dispatches them on the game loop thread.
   *
   * <p>This method should be called regularly from the main game loop to ensure timely processing
   * of incoming messages.
   */
  void pollAndDispatch();
}
