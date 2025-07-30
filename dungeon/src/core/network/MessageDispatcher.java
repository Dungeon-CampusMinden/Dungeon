package core.network;

import core.network.messages.NetworkMessage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the dispatching of incoming {@link NetworkMessage}s to registered handlers, allowing for
 * type-specific registration. Allows multiple parts of the game (including third-party code) to
 * react to specific message types without modifying a central switch statement or requiring manual
 * type checks.
 */
public final class MessageDispatcher {
  private static final Logger LOGGER = Logger.getLogger(MessageDispatcher.class.getName());

  private final Map<Class<? extends NetworkMessage>, List<Consumer<NetworkMessage>>> typedHandlers =
      new ConcurrentHashMap<>();

  /**
   * Registers a new handler for a specific type of {@link NetworkMessage}. When a message of type
   * {@code messageType} is dispatched, the provided {@code handler} will be invoked with that
   * message.
   *
   * @param <T> The type of the message this handler consumes.
   * @param messageType The Class object representing the type of message to handle.
   * @param handler A Consumer that takes a message of type T.
   */
  @SuppressWarnings("unchecked")
  public <T extends NetworkMessage> void registerHandler(
      Class<T> messageType, Consumer<T> handler) {
    if (messageType == null || handler == null) {
      LOGGER.warning("Attempted to register a null messageType or handler.");
      return;
    }
    typedHandlers
        .computeIfAbsent(messageType, k -> new CopyOnWriteArrayList<>())
        .add((Consumer<NetworkMessage>) handler); // Cast is safe due to generic type T
    LOGGER.fine("Registered handler for message type: " + messageType.getSimpleName());
  }

  /**
   * Unregisters a handler for a specific type of {@link NetworkMessage}.
   *
   * @param <T> The type of the message this handler consumes.
   * @param messageType The Class object representing the type of message the handler was registered
   *     for.
   * @param handler The handler to unregister.
   * @return true if the handler was successfully removed, false otherwise.
   */
  public <T extends NetworkMessage> boolean unregisterHandler(
      Class<T> messageType, Consumer<T> handler) {
    if (messageType == null || handler == null) return false;
    List<Consumer<NetworkMessage>> handlersForType = typedHandlers.get(messageType);
    if (handlersForType != null) {
      boolean removed = handlersForType.remove(handler);
      if (removed) {
        LOGGER.fine("Unregistered handler for message type: " + messageType.getSimpleName());
      }
      return removed;
    }
    return false;
  }

  /**
   * Dispatches a received {@link NetworkMessage} to all registered handlers for its specific type.
   *
   * @param message The NetworkMessage to dispatch.
   */
  public void dispatch(NetworkMessage message) {
    if (message == null) {
      LOGGER.warning("Attempted to dispatch a null message.");
      return;
    }

    List<Consumer<NetworkMessage>> handlersForType = typedHandlers.get(message.getClass());
    if (handlersForType != null) {
      for (Consumer<NetworkMessage> handler : handlersForType) {
        try {
          handler.accept(message);
        } catch (Exception e) {
          LOGGER.log(
              Level.SEVERE,
              "Error in message handler for message "
                  + message.getClass().getSimpleName()
                  + ": "
                  + e.getMessage(),
              e);
        }
      }
    } else {
      LOGGER.info(
          "No specific handler registered for message type: " + message.getClass().getSimpleName());
    }
  }
}
