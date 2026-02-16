package contrib.hud.dialogs;

import core.Game;
import core.network.messages.c2s.DialogResponseMessage;
import core.network.server.DialogTracker;
import core.utils.logging.DungeonLogger;
import java.util.function.Consumer;

/**
 * Resolves dialog callbacks and handles network communication for dialog responses.
 *
 * <p>This class acts as a bridge between dialogs and their callbacks, supporting both local
 * (single-player) and network (multiplayer) scenarios. When running as a network client, it sends
 * dialog responses to the server. When running locally, it directly executes the registered
 * callbacks.
 */
public final class DialogCallbackResolver {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(DialogCallbackResolver.class);

  /** Private constructor to prevent instantiation of utility class. */
  private DialogCallbackResolver() {}

  /**
   * Creates a callback consumer for a dialog button.
   *
   * <p>This method creates a consumer that handles button callbacks appropriately based on the
   * network context. If running as a network client, the callback will send a {@link
   * DialogResponseMessage} to the server. If running locally or on the server, the callback will
   * execute the registered callback directly.
   *
   * @param dialogId the unique identifier of the dialog
   * @param callbackKey the key identifying the specific callback for the button
   * @return a Consumer that accepts dialog payload data and executes the appropriate callback
   */
  public static Consumer<DialogResponseMessage.Payload> createButtonCallback(
      String dialogId, String callbackKey) {
    if (isNetworkClient()) {
      return (payload) -> {
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

  /**
   * Checks if the current instance is a network client that should use network callbacks.
   *
   * @return true if we're a client connected to a server
   */
  private static boolean isNetworkClient() {
    return !Game.network().isServer() && Game.network().isConnected();
  }
}
