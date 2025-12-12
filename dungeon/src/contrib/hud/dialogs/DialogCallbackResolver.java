package contrib.hud.dialogs;

import core.Game;
import core.network.messages.c2s.DialogResponseMessage;
import core.network.messages.c2s.DialogResponseMessage.ResponseType;
import core.network.server.DialogTracker;
import core.utils.logging.DungeonLogger;
import java.io.Serializable;
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
   * @return a Consumer that accepts serializable data and executes the appropriate callback
   */
  public static Consumer<Serializable> createButtonCallback(String dialogId, String callbackKey) {
    if (isNetworkClient()) {
      return (data) -> {
        DialogResponseMessage msg =
            new DialogResponseMessage(dialogId, ResponseType.CALLBACK, callbackKey, data);
        Game.network().send((short) 0, msg, true);
      };
    } else {
      return (data) ->
          DialogTracker.instance()
              .getCallback(dialogId, callbackKey)
              .ifPresentOrElse(
                  callback -> callback.accept(data),
                  () ->
                      LOGGER.warn(
                          "No callback found for dialogId: {} and callbackKey: {}",
                          dialogId,
                          callbackKey));
    }
  }

  /**
   * Sends a dialog closed message over the network.
   *
   * <p>This method notifies the server (or other clients in network mode) that a dialog has been
   * closed. It creates and sends a {@link DialogResponseMessage} with {@link ResponseType#CLOSED}
   * to indicate the dialog closure.
   *
   * @param dialogId the unique identifier of the dialog that was closed
   */
  public static void sendDialogClosed(String dialogId) {
    DialogResponseMessage msg =
        new DialogResponseMessage(dialogId, ResponseType.CLOSED, null, null);
    Game.network().send((short) 0, msg, true);
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
