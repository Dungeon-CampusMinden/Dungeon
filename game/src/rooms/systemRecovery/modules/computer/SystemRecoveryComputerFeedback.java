package rooms.systemRecovery.modules.computer;

import engine.Game;
import engine.network.NetworkUtils;
import engine.network.messages.s2c.DialogFeedbackMessage;
import feature.hud.dialogs.DialogFeedbackFingerprint;
import feature.hud.dialogs.DialogFeedbackRouter;
import java.util.Set;

/** Sends authoritative status updates for actions in the System Recovery computer. */
public final class SystemRecoveryComputerFeedback {

  private SystemRecoveryComputerFeedback() {}

  /**
   * Sends a localized result to the computer tab that initiated the write.
   *
   * <p>Singleplayer delivers the same message directly. In multiplayer only the client owning
   * the submitting player receives it, so another player's computer dialog cannot be updated.
   *
   * @param dialogId open computer dialog receiving the update
   * @param targetTabKey stable local tab key
   * @param source submitted program source used for stale-response protection
   * @param playerId player who submitted the program
   * @param messageKey System Recovery translation key for the result
   * @param successful whether the server accepted the program
   */
  public static void send(
      String dialogId,
      String targetTabKey,
      String source,
      int playerId,
      String messageKey,
      boolean successful) {
    if (dialogId == null || dialogId.isBlank() || playerId < 0) return;

    DialogFeedbackMessage message =
        new DialogFeedbackMessage(
            dialogId,
            targetTabKey,
            DialogFeedbackFingerprint.of(source),
            messageKey,
            successful);
    if (Game.isSingleplayer()) {
      DialogFeedbackRouter.deliver(message);
      return;
    }
    if (!Game.network().isServer()) return;

    Set<Short> clientIds = NetworkUtils.entityIdsToClientIds(new int[] {playerId});
    clientIds.forEach(clientId -> Game.network().send(clientId, message, true));
  }
}
