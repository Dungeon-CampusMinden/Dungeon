package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.tracking.Tracking;
import engine.utils.Tuple;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * One authoritative offering ends tracking; every connected player can finish reading the outro.
 */
final class ProgrammingEnding {
  private final BooleanSupplier ready;
  private final Set<Integer> presented = new HashSet<>();
  private final Set<Integer> confirmed = new HashSet<>();
  private boolean started;
  private boolean stopped;

  ProgrammingEnding(Entity inscription, BooleanSupplier ready) {
    this.ready = ready;
    inscription.add(new InteractionComponent(new Interaction((target, who) -> offer(who))));
  }

  private void offer(Entity who) {
    if (Game.isMultiplayerClient() || started || !ready.getAsBoolean()) return;
    var context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.TEXT)
            .put(DialogContextKeys.TITLE, "Am Herzfeuer")
            .put(
                DialogContextKeys.MESSAGE,
                "Lege die Kristalle ins Herzfeuer.\n\n"
                    + "Damit schließt ihr den Raum ab.\n"
                    + "Nach dem Abspann endet das Spiel für alle.")
            .put(DialogContextKeys.CONFIRM_LABEL, "Opfergabe darbringen")
            .build();
    UIComponent dialog = DialogFactory.show(context, who.id());
    dialog.registerCallback(
        DialogContextKeys.ON_CONFIRM,
        data -> {
          UIUtils.closeDialog(dialog, true);
          finish(who);
        });
  }

  private void finish(Entity who) {
    if (Game.isMultiplayerClient() || started || !ready.getAsBoolean()) return;
    started = true;
    ProgrammingProgress.interaction("heartfire", "offering", who);
    Tracking.completed();
    Game.levelEntities()
        .filter(entity -> entity.name().startsWith("programming-decisions-heart-offering-"))
        .toList()
        .forEach(Game::remove);
    Game.levelEntities()
        .filter(entity -> entity.name().equals("programming-prop-torch-decisions-heart"))
        .flatMap(entity -> entity.fetch(DrawComponent.class).stream())
        .forEach(draw -> draw.stateMachine().setState("on", null));
    Game.levelEntities()
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .toList()
        .forEach(ui -> UIUtils.closeDialog(ui, true));
    tick();
  }

  /** Runs even while a modal dialog pauses the movement systems. */
  void tick() {
    if (!started || stopped || Game.isMultiplayerClient()) return;
    var players = Game.allPlayers().toList();
    for (Entity player : players) {
      if (!presented.add(player.id())) continue;
      BlackFadeCutscene.show(
          ProgrammingStory.ending(),
          true,
          false,
          () -> {
            confirmed.add(player.id());
            BlackFadeCutscene.show(
                    List.of(Tuple.of("Geschafft!\nWarte, bis alle ihre Reise beendet haben.", 30)),
                    false,
                    false,
                    () -> {},
                    player.id())
                .registerCallback(DialogContextKeys.ON_RESUME, data -> {});
          },
          player.id());
    }
    if (players.stream().allMatch(player -> confirmed.contains(player.id()))) {
      stopped = true;
      Game.complete();
    }
  }

  boolean active() {
    return started;
  }
}
