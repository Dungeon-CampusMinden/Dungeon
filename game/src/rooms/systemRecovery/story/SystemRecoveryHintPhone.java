package rooms.systemRecovery.story;

import engine.Entity;
import engine.Game;
import feature.components.UIComponent;
import feature.hints.Hint;
import feature.hints.HintSystem;
import feature.hud.dialogs.DialogFactory;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import rooms.systemRecovery.modules.computer.SystemRecoveryDialogTypes;
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

  /**
   * Delivers the next shared hint without confirmation after the inactivity deadline.
   *
   * <p>Story dialogs and choices are not interrupted. An open computer may remain underneath the
   * hint, so its draft survives and the inactivity assistance also works while typing code.
   *
   * @return whether a hint was consumed and shown to the connected players
   */
  public static boolean deliverAutomaticHint() {
    List<Entity> players = Game.allPlayers().toList();
    if (players.isEmpty()) return false;
    boolean blockingDialog =
        Game.entities()
            .flatMap(entity -> entity.fetch(UIComponent.class).stream())
            .anyMatch(
                ui ->
                    ui.willPauseGame()
                        && ui.dialogContext().dialogType() != SystemRecoveryDialogTypes.COMPUTER);
    if (blockingDialog) return false;

    boolean[] delivered = {false};
    Game.system(
        HintSystem.class,
        hints -> {
          Optional<SystemRecoveryLearningStep> step = SystemRecoveryProgressNet.activeStep();
          Optional<Hint> nextHint = hints.peekSharedHint();
          if (step.isEmpty() || nextHint.isEmpty()) return;
          OptionalInt place = SystemRecoveryProgressNet.hintEntityId(step.orElseThrow());
          if (place.isEmpty()) return;
          hints
              .acceptSharedHint(place.orElseThrow(), nextHint.orElseThrow())
              .ifPresent(
                  accepted -> {
                    // Tracking needs a participant; the first player stands in for the room.
                    deliver(step.orElseThrow(), accepted, players.getFirst(), true, players);
                    delivered[0] = true;
                  });
        });
    return delivered[0];
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
                    accepted -> deliver(offer.step(), accepted, player, false, List.of(player)),
                    () -> request(player)),
        () -> {},
        player.id());
  }

  /**
   * Tracks a consumed hint, writes it to the matching quest log and shows it.
   *
   * @param step learning step whose hint sequence was consumed
   * @param accepted consumed hint
   * @param trackedPlayer participant the hint event is attributed to
   * @param automatic whether the hint was shown without a player request
   * @param recipients players who see the hint dialog
   */
  private static void deliver(
      SystemRecoveryLearningStep step,
      Hint accepted,
      Entity trackedPlayer,
      boolean automatic,
      List<Entity> recipients) {
    String hintId = SystemRecoveryHintCatalog.hintId(step, accepted);
    step.puzzle()
        .ifPresent(
            puzzle -> {
              if (automatic) {
                SystemRecoveryPuzzleEvents.automaticHintUsed(puzzle, hintId, trackedPlayer);
              } else {
                SystemRecoveryPuzzleEvents.hintUsed(puzzle, hintId, trackedPlayer);
              }
            });
    SystemRecoveryQuestLogUtil.addHintEntry(step.riddleKey(), accepted);
    String key = accepted.solution() ? "hint-solution-delivery" : "hint-delivery";
    recipients.forEach(
        player ->
            DialogFactory.showDialogDialog(
                SystemRecoveryText.echoCall(key, accepted.text()), () -> {}, player.id()));
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
