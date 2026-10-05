package feature.hud.dialogs;

import engine.Entity;
import engine.Game;
import engine.network.NetworkUtils;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.network.server.DialogTracker;
import engine.utils.logging.DungeonLogger;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Resolves dialog callbacks and handles network communication for dialog responses.
 *
 * <p>This class acts as a bridge between dialogs and their callbacks, supporting local and network
 * scenarios. Network clients send responses for server-owned dialogs and directly execute callbacks
 * for dialogs owned by local-only entities.
 */
public final class DialogCallbackResolver {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(DialogCallbackResolver.class);

  /** Private constructor to prevent instantiation of utility class. */
  private DialogCallbackResolver() {}

  /**
   * Creates a callback consumer for a dialog button.
   *
   * <p>This method creates a consumer that handles button callbacks appropriately based on the
   * network context. Network clients send a {@link DialogResponseMessage} for server-owned dialogs
   * and execute callbacks directly for local-only dialogs. If running locally or on the server, the
   * callback will execute the registered callback directly.
   *
   * @param dialogId the unique identifier of the dialog
   * @param callbackKey the key identifying the specific callback for the button
   * @return a Consumer that accepts dialog payload data and executes the appropriate callback
   */
  public static Consumer<DialogResponseMessage.Payload> createButtonCallback(
      String dialogId, String callbackKey) {
    if (NetworkUtils.isNetworkClient()) {
      return (payload) -> {
        Optional<Consumer<DialogResponseMessage.Payload>> localCallback =
            findLocalCallback(Game.entities(), dialogId, callbackKey);
        if (localCallback.isPresent()) {
          localCallback.get().accept(payload);
          return;
        }

        DialogResponseMessage msg = new DialogResponseMessage(dialogId, callbackKey, payload);
        Game.network().send((short) 0, msg, true);
      };
    } else {
      return (payload) ->
          DialogTracker.instance()
              .getCallback(dialogId, callbackKey)
              .ifPresentOrElse(
                  callback -> callback.accept(payload),
                  () ->
                      LOGGER.warn(
                          "No callback found for dialogId: {} and callbackKey: {}",
                          dialogId,
                          callbackKey));
    }
  }

  static Optional<Consumer<DialogResponseMessage.Payload>> findLocalCallback(
      Stream<Entity> entities, String dialogId, String callbackKey) {
    Objects.requireNonNull(entities, "entities");
    return entities
        .filter(Entity::isLocal)
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .filter(component -> component.dialogContext().dialogId().equals(dialogId))
        .map(component -> localCallback(component, callbackKey))
        .filter(Objects::nonNull)
        .findFirst();
  }

  private static Consumer<DialogResponseMessage.Payload> localCallback(
      UIComponent component, String callbackKey) {
    var callbacks = component.callbacks();
    Consumer<DialogResponseMessage.Payload> callback = callbacks.get(callbackKey);
    if (!DialogContextKeys.ON_CLOSE.equals(callbackKey)) return callback;
    // A server dialog can have a local proxy entity, but never client-side callbacks.
    if (callbacks.isEmpty()) return null;
    return payload -> {
      try {
        if (callback != null) callback.accept(payload);
      } finally {
        UIUtils.closeDialog(component);
      }
    };
  }
}
