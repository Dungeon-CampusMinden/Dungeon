package core.network.input;

import core.Entity;
import core.network.messages.c2s.InputMessage;
import core.network.server.ClientState;
import core.utils.logging.DungeonLogger;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry and dispatcher for authoritative input command handlers.
 *
 * <p>Built-in input actions and custom commands are both resolved to a route key. Exactly one
 * handler is active per route key. Registering the same route again overrides the previous handler.
 */
public final class InputCommandRouter {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(InputCommandRouter.class);

  private static final String ROUTE_MOVE = "core:move";
  private static final String ROUTE_CAST_SKILL = "core:cast_skill";
  private static final String ROUTE_INTERACT = "core:interact";
  private static final String ROUTE_NEXT_SKILL = "core:next_skill";
  private static final String ROUTE_PREV_SKILL = "core:prev_skill";
  private static final String ROUTE_INV_DROP = "core:inv_drop";
  private static final String ROUTE_INV_MOVE = "core:inv_move";
  private static final String ROUTE_INV_USE = "core:inv_use";
  private static final String ROUTE_TOGGLE_INVENTORY = "core:toggle_inventory";

  private static final Map<String, RegisteredHandler> HANDLERS = new ConcurrentHashMap<>();
  private static final Object LOCK = new Object();

  private InputCommandRouter() {}

  /**
   * Functional interface for handling routed inputs.
   *
   * @see InputCommandContext
   */
  @FunctionalInterface
  public interface InputCommandHandler {
    /**
     * Executes handler logic for one routed input.
     *
     * @param context immutable context for the routed input
     */
    void handle(InputCommandContext context);
  }

  /**
   * Immutable context passed to command handlers.
   *
   * @param clientState requesting client state
   * @param playerEntity resolved player entity for the client
   * @param message input message being handled
   * @param routeKey resolved route key for the input
   */
  public record InputCommandContext(
      ClientState clientState, Entity playerEntity, InputMessage message, String routeKey) {
    /**
     * Returns the message payload cast to the requested type.
     *
     * @param type payload type
     * @param <T> payload type parameter
     * @return typed payload
     */
    public <T extends InputMessage.Payload> T payloadAs(Class<T> type) {
      return message.payloadAs(type);
    }
  }

  /**
   * Snapshot of a registered command handler.
   *
   * @param routeKey route key
   * @param ignorePause whether handler runs while paused
   */
  public record HandlerRegistration(String routeKey, boolean ignorePause) {}

  /**
   * Registers a command handler.
   *
   * <p>Only one handler is active per route key. If a route key already exists, it is overridden
   * and an info log is emitted.
   *
   * @param routeKey namespaced route key (`namespace:action`)
   * @param ignorePause whether the handler executes while paused
   * @param handler handler implementation
   * @return true when registration succeeds
   */
  public static boolean register(
      String routeKey, boolean ignorePause, InputCommandHandler handler) {
    Objects.requireNonNull(handler, "handler");
    if (!isValidRouteKey(routeKey)) {
      throw new IllegalArgumentException(
          "Invalid routeKey '" + routeKey + "'. Expected format <namespace>:<action>.");
    }

    synchronized (LOCK) {
      RegisteredHandler previous =
          HANDLERS.put(routeKey, new RegisteredHandler(ignorePause, handler));
      if (previous != null) {
        LOGGER.info("Overriding input handler for route='{}'.", routeKey);
      }
      return true;
    }
  }

  /**
   * Unregisters one handler by route key.
   *
   * @param routeKey route key
   * @return true if a handler was removed, false otherwise
   */
  public static boolean unregister(String routeKey) {
    return HANDLERS.remove(routeKey) != null;
  }

  /**
   * Returns a snapshot of all registered handlers.
   *
   * @return immutable list of registrations
   */
  public static List<HandlerRegistration> registrations() {
    return HANDLERS.entrySet().stream()
        .map(entry -> new HandlerRegistration(entry.getKey(), entry.getValue().ignorePause()))
        .sorted(Comparator.comparing(HandlerRegistration::routeKey))
        .toList();
  }

  /**
   * Resolves and dispatches one input message.
   *
   * @param clientState requesting client state
   * @param playerEntity resolved player entity for the client
   * @param message input message
   * @param paused whether the player is currently paused by an open pausing UI
   * @return true if the handler executed, false otherwise
   */
  public static boolean dispatch(
      ClientState clientState, Entity playerEntity, InputMessage message, boolean paused) {
    Objects.requireNonNull(clientState, "clientState");
    Objects.requireNonNull(playerEntity, "playerEntity");
    Objects.requireNonNull(message, "message");

    String routeKey = routeKey(message);
    RegisteredHandler handler = HANDLERS.get(routeKey);
    if (handler == null) {
      LOGGER.warn(
          "No input handler registered for route='{}' (action='{}').", routeKey, message.action());
      return false;
    }

    if (paused && !handler.ignorePause()) {
      return false;
    }

    handler.handler().handle(new InputCommandContext(clientState, playerEntity, message, routeKey));
    return true;
  }

  /**
   * Resolves the built-in route key for a non-custom action.
   *
   * @param action input action
   * @return route key for the action
   * @throws IllegalArgumentException if action is {@link InputMessage.Action#CUSTOM}
   */
  public static String routeKey(InputMessage.Action action) {
    Objects.requireNonNull(action, "action");
    return switch (action) {
      case MOVE -> ROUTE_MOVE;
      case CAST_SKILL -> ROUTE_CAST_SKILL;
      case INTERACT -> ROUTE_INTERACT;
      case NEXT_SKILL -> ROUTE_NEXT_SKILL;
      case PREV_SKILL -> ROUTE_PREV_SKILL;
      case INV_DROP -> ROUTE_INV_DROP;
      case INV_MOVE -> ROUTE_INV_MOVE;
      case INV_USE -> ROUTE_INV_USE;
      case TOGGLE_INVENTORY -> ROUTE_TOGGLE_INVENTORY;
      case CUSTOM ->
          throw new IllegalArgumentException("CUSTOM does not map to a fixed core route.");
    };
  }

  private static String routeKey(InputMessage message) {
    if (message.action() == InputMessage.Action.CUSTOM) {
      return message.payloadAs(InputMessage.Custom.class).commandId();
    }
    return routeKey(message.action());
  }

  private static boolean isValidRouteKey(String routeKey) {
    return InputMessage.isValidRouteKey(routeKey);
  }

  static void resetForTesting() {
    synchronized (LOCK) {
      HANDLERS.clear();
    }
  }

  private record RegisteredHandler(boolean ignorePause, InputCommandHandler handler) {}
}
