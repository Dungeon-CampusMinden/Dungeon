package core.network;

import core.network.messages.NetworkMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages dispatching of incoming {@link NetworkMessage}s to a single registered handler per
 * message type. Registering a new handler for a type replaces any existing handler.
 */
public final class MessageDispatcher {
  private static final Logger LOGGER =
    Logger.getLogger(MessageDispatcher.class.getName());

  private final Map<Class<? extends NetworkMessage>, Consumer<?>> typedHandlers =
    new ConcurrentHashMap<>();

  public <T extends NetworkMessage> void registerHandler(
    Class<T> messageType, Consumer<? super T> handler) {
    if (messageType == null || handler == null) {
      LOGGER.warning("Attempted to register a null messageType or handler.");
      return;
    }
    typedHandlers.put(messageType, handler);
    LOGGER.fine("Registered handler for message type: " + messageType.getSimpleName());
  }

  public <T extends NetworkMessage> boolean unregisterHandler(
    Class<T> messageType, Consumer<? super T> handler) {
    if (messageType == null || handler == null) return false;
    Consumer<?> existing = typedHandlers.get(messageType);
    if (existing != null && existing.equals(handler)) {
      typedHandlers.remove(messageType);
      LOGGER.fine("Unregistered handler for message type: " + messageType.getSimpleName());
      return true;
    }
    return false;
  }

  public void dispatch(NetworkMessage message) {
    if (message == null) {
      LOGGER.warning("Attempted to dispatch a null message.");
      return;
    }

    Consumer<?> handler = typedHandlers.get(message.getClass());
    if (handler != null) {
      try {
        @SuppressWarnings("unchecked")
        Consumer<Object> c = (Consumer<Object>) handler;
        c.accept(message);
      } catch (Exception e) {
        LOGGER.log(
          Level.SEVERE,
          "Error in message handler for message "
            + message.getClass().getSimpleName()
            + ": "
            + e.getMessage(),
          e);
      }
    } else {
      LOGGER.info(
        "No specific handler registered for message type: "
          + message.getClass().getSimpleName());
    }
  }
}
