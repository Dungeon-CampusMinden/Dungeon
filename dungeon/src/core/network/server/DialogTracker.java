package core.network.server;

import contrib.components.UIComponent;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContextKeys;
import core.Game;
import core.game.PreRunConfiguration;
import core.network.NetworkUtils;
import core.network.messages.s2c.DialogCloseMessage;
import core.network.messages.s2c.DialogShowMessage;
import core.utils.logging.DungeonLogger;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Server-side tracker for active networked dialogs.
 *
 * <p>Maintains a registry of dialogs sent to clients, storing their callbacks and tracking which
 * clients are authorized to respond. Provides validation and first-responder logic for shared
 * dialogs.
 *
 * <p>Key responsibilities:
 *
 * <ul>
 *   <li>Register dialogs with their callbacks and target clients
 *   <li>Validate that responding clients were authorized
 *   <li>Ensure only the first responder's callback executes (for shared dialogs)
 *   <li>Support dialog resync on client reconnect
 *   <li>Allow server-initiated dialog closure
 * </ul>
 *
 * @see DialogShowMessage
 * @see core.network.messages.c2s.DialogResponseMessage
 * @see contrib.hud.dialogs.DialogCallbackResolver
 */
public final class DialogTracker {
  private static final DialogTracker INSTANCE = new DialogTracker();
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(DialogTracker.class);

  /**
   * Internal data structure for tracking a dialog.
   *
   * @param uiComponent the UIComponent representing the dialog
   * @param claimedByClientId the client ID that claimed this dialog (null if unclaimed)
   * @param authorizedClientIds the set of client IDs authorized to respond
   */
  private record DialogInfo(
      UIComponent uiComponent, Short claimedByClientId, Set<Short> authorizedClientIds) {}

  private final Map<String, DialogInfo> dialogs = new ConcurrentHashMap<>();
  private final Map<Short, Set<String>> clientDialogs = new ConcurrentHashMap<>();

  private DialogTracker() {}

  /**
   * Returns the singleton instance of the DialogTracker.
   *
   * @return the DialogTracker instance
   */
  public static DialogTracker instance() {
    return INSTANCE;
  }

  /**
   * Registers a new dialog for network tracking.
   *
   * @param uiComponent the UIComponent representing the dialog
   * @throws IllegalArgumentException if dialogId is null or already registered
   */
  public void registerDialog(UIComponent uiComponent) {
    if (dialogs.containsKey(uiComponent.dialogContext().dialogId())) {
      throw new IllegalArgumentException(
          "Dialog with id '" + uiComponent.dialogContext().dialogId() + "' is already registered");
    }

    DialogInfo info =
        new DialogInfo(
            uiComponent, null, NetworkUtils.entityIdsToClientIds(uiComponent.targetEntityIds()));
    dialogs.put(uiComponent.dialogContext().dialogId(), info);

    // Track dialogs per client for resync
    for (short clientId : info.authorizedClientIds()) {
      clientDialogs
          .computeIfAbsent(clientId, k -> ConcurrentHashMap.newKeySet())
          .add(uiComponent.dialogContext().dialogId());
    }

    LOGGER.debug(
        "Registered dialog '{}' for {} clients (entityId={})",
        uiComponent.dialogContext().dialogId(),
        info.authorizedClientIds().size(),
        uiComponent.dialogContext().ownerEntity());
  }

  /**
   * Checks if a client is authorized to respond to a dialog.
   *
   * @param clientId the client attempting to respond
   * @param dialogId the dialog being responded to
   * @return true if the client is in the dialog's target list or if the dialog is open to all
   *     clients
   */
  public boolean canRespond(short clientId, String dialogId) {
    DialogInfo info = dialogs.get(dialogId);
    if (info == null) {
      return false;
    }
    return info.authorizedClientIds().isEmpty() || info.authorizedClientIds().contains(clientId);
  }

  /**
   * Attempts to claim a dialog for response (first-responder wins).
   *
   * <p>For dialogs shared between multiple clients, only the first client to call this method will
   * succeed. Subsequent calls return false.
   *
   * @param dialogId the dialog to claim
   * @param clientId the client attempting to claim
   * @return true if this client is the first responder, false otherwise
   */
  public boolean tryClaimDialog(String dialogId, short clientId) {
    DialogInfo info = dialogs.get(dialogId);
    if (info == null) {
      return false;
    }

    // Use synchronized on the dialogs map to ensure atomic claim
    synchronized (dialogs) {
      // Re-check after acquiring lock
      DialogInfo currentInfo = dialogs.get(dialogId);
      if (currentInfo == null) {
        return false;
      }
      Short claimedId = currentInfo.claimedByClientId(); // can be null
      if (currentInfo.claimedByClientId() != null && !claimedId.equals(clientId)) {
        return false; // Already claimed by another client
      }

      // Create new info with claim
      DialogInfo claimedInfo =
          new DialogInfo(currentInfo.uiComponent(), clientId, currentInfo.authorizedClientIds());
      dialogs.put(dialogId, claimedInfo);
      return true;
    }
  }

  /**
   * Clears all tracked dialogs, closing them without notifying clients.
   *
   * <p>Primarily used during level resets.
   */
  public void clear() {
    for (String dialogId : dialogs.keySet()) {
      closeDialog(dialogId, false);
    }
    dialogs.clear();
    clientDialogs.clear();
  }

  /**
   * Retrieves a callback by key for execution.
   *
   * <p>If the callbackKey is {@link DialogContextKeys#ON_CLOSE}, wraps the original callback to
   * also close the dialog after execution.
   *
   * @param dialogId the dialog containing the callback
   * @param callbackKey the key identifying the callback
   * @return an Optional containing the callback, or empty if not found
   */
  public Optional<Consumer<Serializable>> getCallback(String dialogId, String callbackKey) {
    DialogInfo info = dialogs.get(dialogId);
    if (info == null) {
      return Optional.empty();
    }
    if (Objects.equals(callbackKey, DialogContextKeys.ON_CLOSE)) {
      return Optional.of(
          (data) -> {
            try {
              Optional.ofNullable(info.uiComponent().callbacks().get(DialogContextKeys.ON_CLOSE))
                  .ifPresent(cb -> cb.accept(data));
            } finally {
              UIUtils.closeDialog(info.uiComponent());
            }
          });
    }
    return Optional.ofNullable(info.uiComponent().callbacks().get(callbackKey));
  }

  /**
   * Gets the entity ID associated with a dialog.
   *
   * @param dialogId the dialog ID
   * @return the entity ID, or -1 if dialog not found
   */
  public int getEntityId(String dialogId) {
    DialogInfo info = dialogs.get(dialogId);
    return info != null ? info.uiComponent().dialogContext().ownerEntity().id() : -1;
  }

  /**
   * Closes a dialog from the server side.
   *
   * <p>Sends {@link DialogCloseMessage} to all target clients and removes the dialog from tracking.
   * Use this when programmatically closing dialogs (e.g., NPC dies while dialog is open).
   *
   * @param dialogId the dialog to close
   * @param notifyClients whether to send close messages to clients
   */
  public void closeDialog(String dialogId, boolean notifyClients) {
    DialogInfo info = dialogs.remove(dialogId);
    if (info == null) {
      LOGGER.debug("Attempted to close non-existent dialog: {}", dialogId);
      return;
    }

    if (notifyClients && PreRunConfiguration.isNetworkServer()) {
      // Send close message to all target clients
      DialogCloseMessage closeMsg = new DialogCloseMessage(dialogId);
      for (Short clientId : info.authorizedClientIds()) {
        Game.network().send(clientId, closeMsg, true);
      }
    }

    // Remove from client tracking
    for (Short clientId : info.authorizedClientIds()) {
      Set<String> clientDialogSet = clientDialogs.get(clientId);
      if (clientDialogSet != null) {
        clientDialogSet.remove(dialogId);
      }
    }

    LOGGER.debug(
        "Closed dialog '{}' on {} clients", dialogId, info.uiComponent.targetEntityIds().length);
  }

  /**
   * Resynchronizes active dialogs to a reconnecting client.
   *
   * <p>Sends {@link DialogShowMessage} for all dialogs the client was authorized to see before
   * disconnecting.
   *
   * @param clientId the reconnecting client's ID
   */
  public void resyncDialogsToClient(short clientId) {
    Set<String> dialogIds = clientDialogs.get(clientId);
    if (dialogIds == null || dialogIds.isEmpty()) {
      return;
    }

    LOGGER.debug("Resyncing {} dialogs to client {}", dialogIds.size(), clientId);

    for (String dialogId : Set.copyOf(dialogIds)) {
      DialogInfo info = dialogs.get(dialogId);
      if (info == null) {
        // Dialog was removed, clean up
        dialogIds.remove(dialogId);
        continue;
      }

      // Send dialog show message
      DialogShowMessage msg =
          new DialogShowMessage(
              info.uiComponent().dialogContext(), info.uiComponent().canBeClosed());
      Game.network().send(clientId, msg, true);
    }
  }
}
