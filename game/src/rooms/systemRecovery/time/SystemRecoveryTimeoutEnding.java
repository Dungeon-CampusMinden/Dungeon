package rooms.systemRecovery.time;

import engine.Entity;
import engine.Game;
import engine.components.InputComponent;
import engine.time.PlayClock;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.components.UIComponent;
import feature.credits.CreditsFeature;
import feature.hud.UIUtils;
import feature.interaction.InteractionComponent;
import feature.survey.SurveyFeature;
import java.util.HashSet;
import java.util.Set;
import rooms.systemRecovery.SystemRecovery;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Failure ending once the countdown has deleted the system core. */
public final class SystemRecoveryTimeoutEnding {

  private static final Set<Integer> SHOWN_TO = new HashSet<>();

  private SystemRecoveryTimeoutEnding() {}

  /** Ends play as failed, blocks all room interaction and plays the failure outro. */
  public static void show() {
    Game.playClock().end(PlayClock.Outcome.FAILURE);
    Game.entities()
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .toList()
        .forEach(ui -> UIUtils.closeDialog(ui, true));
    Game.entities().forEach(entity -> entity.remove(InteractionComponent.class));
    SHOWN_TO.clear();
    showToNewPlayers();
  }

  /** Locks the controls of players who have not seen the failure outro yet and shows it to them. */
  public static void showToNewPlayers() {
    int[] newPlayers =
        Game.allPlayers()
            .filter(player -> SHOWN_TO.add(player.id()))
            .peek(
                player ->
                    player
                        .fetch(InputComponent.class)
                        .ifPresent(input -> input.deactivateControls(true)))
            .mapToInt(Entity::id)
            .toArray();
    if (newPlayers.length == 0) return;
    BlackFadeCutscene.show(
        SystemRecoveryText.timeoutEndingPages(),
        true,
        false,
        false,
        () ->
            CreditsFeature.showAfterGame(
                SystemRecovery.CREDITS_ROOM_ID,
                // The survey comes last and ends the game.
                () ->
                    SurveyFeature.show(
                        () -> Game.exit("System Recovery time limit expired"), newPlayers),
                newPlayers),
        newPlayers);
  }
}
