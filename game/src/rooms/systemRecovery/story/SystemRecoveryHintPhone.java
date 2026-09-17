package rooms.systemRecovery.story;

import engine.Entity;
import engine.Game;
import feature.hints.Hint;
import feature.hints.HintSystem;
import feature.hud.dialogs.DialogFactory;
import java.util.Optional;
import java.util.OptionalInt;
import rooms.systemRecovery.petrinet.SystemRecoveryHintCatalog;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.util.SystemRecoveryQuestLogUtil;
import rooms.systemRecovery.util.SystemRecoveryText;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzleEvents;

/**
 * Telephone front end for the shared System Recovery hint sequence.
 *
 * <p>The phone asks the server-side {@link HintSystem} for the next hint. The hint index is shared
 * by the whole room, matching the shared quest log. Declining a confirmation does not advance the
 * index; accepting it writes the revealed hint into the matching quest-log tab.
 */
public final class SystemRecoveryHintPhone {

  private SystemRecoveryHintPhone() {}

  /**
   * Opens the next-hint conversation for the player who interacted with the phone.
   *
   * @param player player who requested the hint
   */
  public static void request(Entity player) {
    if (player == null) return;

    Game.system(
        HintSystem.class,
        hintSystem -> {
          Optional<Hint> nextHint = hintSystem.peekSharedHint();
          Optional<SystemRecoveryLearningStep> activeStep = SystemRecoveryProgressNet.activeStep();
          if (nextHint.isEmpty() || activeStep.isEmpty()) {
            DialogFactory.showDialogDialog(
                SystemRecoveryText.echoCall("hint-none"), () -> {}, player.id());
            return;
          }

          Hint hint = nextHint.orElseThrow();
          SystemRecoveryLearningStep step = activeStep.orElseThrow();
          OptionalInt placeEntityId = SystemRecoveryProgressNet.hintEntityId(step);
          if (placeEntityId.isEmpty()) {
            DialogFactory.showDialogDialog(
                SystemRecoveryText.echoCall("hint-none"), () -> {}, player.id());
            return;
          }
          HintOffer offer = new HintOffer(step, placeEntityId.orElseThrow(), hint);
          String conversation =
              hint.solution()
                  ? SystemRecoveryText.echoCall("hint-solution-warning")
                  : SystemRecoveryText.echoCall("hint-offer");
          DialogFactory.showDialogDialog(
              conversation, () -> showConfirmation(hintSystem, player, offer), player.id());
        });
  }

  private static void showConfirmation(HintSystem hintSystem, Entity player, HintOffer offer) {
    boolean solution = offer.hint().solution();
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
            SystemRecoveryProgressNet.activeStep()
                .filter(offer.step()::equals)
                .flatMap(
                    ignored -> hintSystem.acceptSharedHint(offer.placeEntityId(), offer.hint()))
                .ifPresentOrElse(
                    accepted -> {
                      String hintId = SystemRecoveryHintCatalog.hintId(offer.step(), accepted);
                      offer
                          .step()
                          .puzzle()
                          .ifPresent(
                              puzzle ->
                                  SystemRecoveryPuzzleEvents.hintUsed(puzzle, hintId, player));
                      SystemRecoveryQuestLogUtil.addHintEntry(offer.step().riddleKey(), accepted);
                      String key = accepted.solution() ? "hint-solution-delivery" : "hint-delivery";
                      DialogFactory.showDialogDialog(
                          SystemRecoveryText.echoCall(key, accepted.text()), () -> {}, player.id());
                    },
                    () -> request(player)),
        () -> {},
        player.id());
  }

  /**
   * Immutable snapshot of the hint offer shown before the confirmation dialog.
   *
   * @param step active learning step for which the hint was requested
   * @param placeEntityId ECS place entity whose shared hint sequence was offered
   * @param hint shared hint offered by the hint system
   */
  private record HintOffer(SystemRecoveryLearningStep step, int placeEntityId, Hint hint) {}
}
