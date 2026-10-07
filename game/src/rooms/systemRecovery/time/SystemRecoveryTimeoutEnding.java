package rooms.systemRecovery.time;

import engine.Entity;
import engine.Game;
import engine.components.InputComponent;
import engine.time.PlayClock;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.interaction.InteractionComponent;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Failure ending once the countdown has deleted the system core. */
public final class SystemRecoveryTimeoutEnding {

  private SystemRecoveryTimeoutEnding() {}

  /** Ends play as failed, blocks all room interaction and plays the failure outro. */
  public static void show() {
    Game.playClock().end(PlayClock.Outcome.FAILURE);
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
}
