package core.network;

import core.network.messages.NetworkMessage;
import io.netty.channel.ChannelHandlerContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the dispatching of incoming {@link NetworkMessage}s to their respective handlers. Each
 * message type can have a single registered handler. Registering a new handler for a type replaces
 * any existing handler.
 */
public final class MessageDispatcher {
  private static final Logger LOGGER = Logger.getLogger(MessageDispatcher.class.getName());

  // A thread-safe map to store handlers for each message type.
  private final Map<Class<? extends NetworkMessage>, BiConsumer<ChannelHandlerContext, ?>>
      typedHandlers = new ConcurrentHashMap<>();

  /**
   * Registers a handler for a specific message type. If a handler is already registered for the
   * given type, it will be replaced.
   *
   * @param <T> The type of the message.
   * @param messageType The class of the message type to register the handler for.
   * @param handler The handler to process messages of the given type.
   */
  public <T extends NetworkMessage> void registerHandler(
      Class<T> messageType, BiConsumer<ChannelHandlerContext, ? super T> handler) {
    if (messageType == null || handler == null) {
      LOGGER.warning("Attempted to register a null messageType or handler.");
      return;
    }
    typedHandlers.put(messageType, handler);
    LOGGER.fine("Registered handler for message type: " + messageType.getSimpleName());
  }

  /**
   * Unregisters a handler for a specific message type. The handler is only removed if it matches
   * the currently registered handler for the type.
   *
   * @param <T> The type of the message.
   * @param messageType The class of the message type to unregister the handler for.
   * @param handler The handler to be unregistered.
   * @return true if the handler was successfully unregistered, false otherwise.
   */
  public <T extends NetworkMessage> boolean unregisterHandler(
      Class<T> messageType, BiConsumer<ChannelHandlerContext, ? super T> handler) {
    if (messageType == null || handler == null) return false;
    BiConsumer<ChannelHandlerContext, ?> existing = typedHandlers.get(messageType);
    if (existing != null && existing.equals(handler)) {
      typedHandlers.remove(messageType);
      LOGGER.fine("Unregistered handler for message type: " + messageType.getSimpleName());
      return true;
    }
    return false;
  }

  /**
   * Dispatches a message to the appropriate handler based on its type. If no handler is registered
   * for the message type, a log entry is created.
   *
   * @param message The message to be dispatched.
   */
  public void dispatch(ChannelHandlerContext ctx, NetworkMessage message) {
    if (message == null) {
      LOGGER.warning("Attempted to dispatch a null message.");
      return;
    }

    BiConsumer<ChannelHandlerContext, ?> handler = typedHandlers.get(message.getClass());
    if (handler != null) {
      try {
        @SuppressWarnings("unchecked")
        BiConsumer<ChannelHandlerContext, Object> c = (BiConsumer<ChannelHandlerContext, Object>) handler;
        c.accept(ctx, message);
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
          "No specific handler registered for message type: " + message.getClass().getSimpleName());
    }
  }
}
