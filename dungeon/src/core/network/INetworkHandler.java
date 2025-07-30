package core.network;

import core.network.messages.NetworkMessage;
import core.network.messages.client2server.ClientMessage;
import java.util.function.Consumer;

/**
 * Central handler for sending game-related messages. Abstracts whether messages are sent over the
 * network or processed locally. In single-player, this might directly invoke game logic. In
 * multiplayer, this sends messages to the server.
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
   * Sends a client message. This method is called when a message is created by the game code (e.g.,
   * a command to interact with an entity or use a skill). The message will be processed either
   * locally via the {@link LocalNetworkHandler} or sent to the server.
   *
   * @param message The client message to process.
   */
  void sendToClient(ClientMessage message);

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
   * Retrieves the dispatcher responsible for handling incoming messages. Game code should register
   * their specific message handlers with this dispatcher.
   *
   * @return The MessageDispatcher instance.
   */
  MessageDispatcher messageDispatcher();

  /**
   * Internal method: Sets the consumer for raw incoming messages. This method is intended for the
   * internal use of the NetworkHandler implementation (e.g., KryoNetHandler) to feed raw messages
   * into the MessageDispatcher. Game code should use {@link #messageDispatcher()} to register
   * specific handlers.
   *
   * @param rawMessageConsumer A consumer that processes raw incoming messages.
   */
  void _setRawMessageConsumer(Consumer<NetworkMessage> rawMessageConsumer);
}
