package core.network;

import core.network.messages.NetworkMessage;
import core.network.messages.client2server.ClientMessage;
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
   */
  void initialize(boolean isServer, String serverAddress, int port) throws NetworkException;

  /**
   * Sends a client message to a server.
   *
   * <p>This method is called when a message is created by the game code (e.g., a command to
   * interact with an entity or use a skill). The message will be processed either locally via the
   * {@link LocalNetworkHandler} or sent to the server.
   *
   * <p>Implementations must ensure that any callbacks into game code via {@link
   * #messageDispatcher()} are invoked on the game loop thread, not on IO/transport threads.
   *
   * @param message The client message to process.
   * @param reliable Whether to request reliable delivery (may be ignored by local handlers).
   */
  void sendToServer(ClientMessage message, boolean reliable);

  /**
   * Convenience overload that defaults to reliable delivery.
   *
   * @param message The client message to process.
   */
  default void sendToServer(ClientMessage message) {
    sendToServer(message, true);
  }

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
}
