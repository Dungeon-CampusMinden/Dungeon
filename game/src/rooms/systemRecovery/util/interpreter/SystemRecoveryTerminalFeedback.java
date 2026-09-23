package rooms.systemRecovery.util.interpreter;

import engine.Game;
import engine.network.NetworkUtils;
import engine.network.messages.s2c.DialogFeedbackMessage;
import feature.hud.dialogs.DialogFeedbackFingerprint;
import feature.hud.dialogs.DialogFeedbackRouter;
import java.util.Set;
import rooms.systemRecovery.modules.computer.content.SystemCoreMetaTab;
import rooms.systemRecovery.modules.computer.content.TerminalTab;
import rooms.systemRecovery.modules.interpreter.TerminalAttempt;

/** Routes authoritative terminal feedback to the client that submitted the source. */
public final class SystemRecoveryTerminalFeedback {

  private SystemRecoveryTerminalFeedback() {}

  /**
   * Sends feedback after the server has evaluated a terminal submission.
   *
   * <p>Singleplayer uses the same message contract but delivers it directly to the local dialog.
   * Multiplayer routes the reliable message only to the client controlling the submitting player.
   *
   * @param attempt authoritative terminal attempt
   * @param successful whether the server accepted the attempt
   */
  public static void send(TerminalAttempt attempt, boolean successful) {
    if (attempt.dialogId() == null || attempt.playerId() < 0) return;

    DialogFeedbackMessage message =
        new DialogFeedbackMessage(
            attempt.dialogId(),
            targetTab(attempt),
            DialogFeedbackFingerprint.of(attempt.source()),
            successful ? "computer.feedback-correct" : "computer.feedback-incorrect",
            successful);

    if (Game.isSingleplayer()) {
      DialogFeedbackRouter.deliver(message);
      return;
    }
    if (!Game.network().isServer()) return;

    Set<Short> clientIds = NetworkUtils.entityIdsToClientIds(new int[] {attempt.playerId()});
    clientIds.forEach(clientId -> Game.network().send(clientId, message, true));
  }

  private static String targetTab(TerminalAttempt attempt) {
    return attempt.state() == TerminalStep.SYSTEM_CORE_META.stateId()
        ? SystemCoreMetaTab.KEY
        : TerminalTab.KEY;
  }
}
