package rooms.systemRecovery.time;

import engine.Entity;
import engine.Game;
import engine.components.InputComponent;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.interaction.InteractionComponent;
import rooms.systemRecovery.level.SystemRecoveryPointRegistry;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.petrinet.SystemRecoveryProgressNet;
import rooms.systemRecovery.util.SystemRecoveryText;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzleEvents;
import tracking.core.TrackingInteractionStatus;

/** Failure ending once the countdown has deleted the system core. */
public final class SystemRecoveryTimeoutEnding {

  private SystemRecoveryTimeoutEnding() {}

  /** Tracks the timeout, blocks all room interaction and plays the failure outro. */
  public static void show() {
    trackTimeLimitExpired();
    Game.entities()
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .toList()
        .forEach(ui -> UIUtils.closeDialog(ui, true));
    Game.allPlayers()
        .forEach(
            player ->
                player
                    .fetch(InputComponent.class)
                    .ifPresent(input -> input.deactivateControls(true)));
    Game.entities().forEach(entity -> entity.remove(InteractionComponent.class));
    BlackFadeCutscene.show(
        SystemRecoveryText.timeoutEndingPages(),
        true,
        false,
        false,
        () -> Game.exit("System Recovery time limit expired"),
        Game.allPlayers().mapToInt(Entity::id).toArray());
  }

  /**
   * Marks each player's run as ended by the time limit at the active puzzle.
   *
   * <p>{@link Game#exit(String)} later aborts the tracking session exactly like quitting, so this
   * interaction is the only trace that distinguishes a timeout from an abort.
   */
  private static void trackTimeLimitExpired() {
    SystemRecoveryProgressNet.activeStep()
        .flatMap(SystemRecoveryLearningStep::puzzle)
        .ifPresent(
            puzzle ->
                Game.allPlayers()
                    .forEach(
                        player ->
                            SystemRecoveryPuzzleEvents.interaction(
                                puzzle,
                                SystemRecoveryPointRegistry.TIMER,
                                "time-limit",
                                TrackingInteractionStatus.BLOCKED,
                                "expired",
                                player)));
  }
}
