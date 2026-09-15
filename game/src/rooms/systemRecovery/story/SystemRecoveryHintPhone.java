package rooms.systemRecovery.story;

import engine.Entity;
import engine.Game;
import feature.hints.Hint;
import feature.hints.HintSystem;
import feature.hud.dialogs.DialogFactory;
import java.util.Optional;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressPlace;
import rooms.systemRecovery.util.SystemRecoveryQuestLogUtil;
import rooms.systemRecovery.util.SystemRecoveryText;

/**
 * Telephone front end for the shared System Recovery hint sequence.
 *
 * <p>The phone asks the server-side {@link HintSystem} for the next hint. The hint index is shared
 * by the whole room, matching the shared quest log. Declining a confirmation does not advance the
 * index; accepting it writes the revealed hint into the matching quest-log tab.
 */
public final class SystemRecoveryHintPhone {

  private SystemRecoveryHintPhone() {}

  /** Opens the next-hint conversation for the player who interacted with the phone. */
  public static void request(Entity player) {
    if (player == null) return;

    Game.system(
        HintSystem.class,
        hintSystem -> {
          Optional<Hint> nextHint = hintSystem.peekSharedHint();
          Optional<SystemRecoveryProgressPlace> activePlace =
              SystemRecoveryProgressNet.activePlace();
          if (nextHint.isEmpty() || activePlace.isEmpty()) {
            DialogFactory.showDialogDialog(
                SystemRecoveryText.phoneCall("hint-none"), () -> {}, player.id());
            return;
          }

          Hint hint = nextHint.orElseThrow();
          SystemRecoveryProgressPlace place = activePlace.orElseThrow();
          String conversation =
              hint.solution()
                  ? SystemRecoveryText.phoneCall("hint-solution-warning")
                  : SystemRecoveryText.phoneCall("hint-offer");
          DialogFactory.showDialogDialog(
              conversation,
              () -> showConfirmation(hintSystem, player, hint.solution()),
              player.id());
        });
  }

  private static void showConfirmation(HintSystem hintSystem, Entity player, boolean solution) {
    String message =
        solution
            ? SystemRecoveryText.key("hints.solution-confirm")
            : SystemRecoveryText.key("hints.next-confirm");
    String title =
        solution
            ? SystemRecoveryText.key("hints.solution-confirm-title")
            : SystemRecoveryText.key("hints.next-confirm-title");
    DialogFactory.showYesNoDialog(
        message,
        title,
        () ->
            hintSystem
                .acceptSharedHint()
                .ifPresentOrElse(
                    accepted -> {
                      SystemRecoveryProgressNet.activePlace()
                          .ifPresent(place -> SystemRecoveryQuestLogUtil.addHintEntry(place.riddleKey(), accepted));
                      String key = accepted.solution() ? "hint-solution-delivery" : "hint-delivery";
                      DialogFactory.showDialogDialog(
                          SystemRecoveryText.phoneCall(key, accepted.text()),
                          () -> {},
                          player.id());
                    },
                    () -> request(player)),
        () -> {},
        player.id());
  }
}
