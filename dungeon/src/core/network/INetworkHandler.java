package core.network;

import core.Entity;
import core.network.messages.NetworkMessage;
import core.utils.Direction;
import core.utils.Point;
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
   * Sends a hero movement command.
   *
   * @param direction The direction of movement.
   */
  void sendHeroMovement(Direction direction);

  /**
   * Sends a hero movement command to a specific point.
   *
   * @param targetPoint The target point to move the hero to.
   */
  void sendHeroMovement(Point targetPoint);

  /**
   * Sends a command to use a skill.
   *
   * @param skillIndex The index or identifier of the skill.
   * @param targetPoint Optional target point for the skill (e.g., cursor position).
   */
  void sendUseSkill(int skillIndex, Point targetPoint); // Consider skill ID instead of index?

  /**
   * Sends a command to interact with the world (e.g., open chest, talk to NPC).
   *
   * @param interactable The entity to interact with (e.g., chest, NPC).
   */
  void sendInteract(Entity interactable);

  // --- State and Event Handling ---
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
