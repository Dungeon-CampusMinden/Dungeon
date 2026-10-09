package rooms.soulweaver.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.time.PlayClock;
import engine.utils.Tuple;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.components.LeverComponent;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.survey.SurveyFeature;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import rooms.soulweaver.save.SoulweaverSave;

/**
 * One authoritative offering ends tracking; every connected player can finish reading the outro.
 */
final class SoulweaverEnding {
  private final BooleanSupplier ready;
  private final Set<Integer> presented = new HashSet<>();
  private final Set<Integer> confirmed = new HashSet<>();
  private boolean started;
  private boolean stopped;

  SoulweaverEnding(Entity inscription, BooleanSupplier ready) {
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
    SoulweaverSave.delete();
    started = true;
    SoulweaverProgress.interaction("heartfire", "offering", who);
    Game.playClock().end(PlayClock.Outcome.SUCCESS);
    Game.levelEntities()
        .filter(entity -> entity.name().startsWith("soulweaver-decisions-heart-offering-"))
        .toList()
        .forEach(Game::remove);
    Game.levelEntities()
        .filter(entity -> entity.name().equals(SoulweaverProps.HEART_TORCH))
        .forEach(
            entity -> {
              var lever = entity.fetch(LeverComponent.class).orElseThrow();
              if (!lever.isOn()) lever.toggle();
              entity.fetch(DrawComponent.class).orElseThrow().sendSignal("on");
            });
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
          SoulweaverStory.ending(),
          true,
          false,
          () ->
              SurveyFeature.show(
                  () -> {
                    confirmed.add(player.id());
                    BlackFadeCutscene.show(
                            List.of(
                                Tuple.of(
                                    "Geschafft!\nWarte, bis alle ihre Reise beendet haben.", 30)),
                            false,
                            false,
                            () -> {},
                            player.id())
                        .registerCallback(DialogContextKeys.ON_RESUME, data -> {});
                  },
                  player.id()),
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
