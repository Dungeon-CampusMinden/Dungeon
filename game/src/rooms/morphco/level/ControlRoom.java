package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.level.elements.tile.DoorTile;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import feature.components.UIComponent;
import feature.emote.Emote;
import feature.hud.UIUtils;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.systems.EventScheduler;
import java.util.List;
import java.util.function.BiConsumer;
import rooms.morphco.petrinet.MorphCoMilestone;
import rooms.morphco.puzzles.CardSwipe;
import rooms.morphco.puzzles.MorphemeQuestions;
import rooms.morphco.ui.MorphCoDialogs;
import rooms.morphco.util.MorphCoAchievements;
import rooms.morphco.util.MorphCoText;
import rooms.morphco.util.MorphCoTracking;

/**
 * The entrance and the control room: card slot, alarm console, phone and the production control
 * system, which restarts the production at the end.
 */
final class ControlRoom {

  private static final String CONSOLE = "world.control.console-title";

  private final MorphCoLevel level;
  private final MorphCoState state;
  private Entity alarmLight;
  private Entity console;
  private Entity phoneEmote;

  ControlRoom(MorphCoLevel level) {
    this.level = level;
    this.state = level.state();
  }

  /** Creates the objects of the entrance and the control room and sets the doors. */
  void spawn() {
    if (!state.has(MorphCoState.CARD_USED)) level.door("door_control").close();
    if (!state.done(MorphCoMilestone.CAUSES_FOUND)) level.door("door_production").close();

    on("card-slot", (self, player) -> cardSlot(player));
    on(
        "warning-sign",
        (self, player) ->
            MorphCoProps.popup(player, "world.titles.entrance", "world.entrance.warning-sign"));
    MorphCoObjects.on(
        "console",
        entity -> {
          console = entity;
          MorphCoProps.interact(entity, (self, player) -> console(player));
          if (state.done(MorphCoMilestone.ALARM_OFF)) MorphCoProps.switchOn(entity);
        });
    MorphCoObjects.on(
        MorphCoLighting.ALARM_LIGHT,
        entity -> {
          alarmLight = entity;
          if (state.done(MorphCoMilestone.ALARM_OFF)) Game.remove(entity);
        });
    on("pc", (self, player) -> computer(player));
    MorphCoObjects.on(
        "phone",
        entity -> {
          MorphCoProps.interact(entity, (self, player) -> phone(player));
          if (state.has(MorphCoState.MESSAGE_HEARD)) return;
          if (phoneEmote != null) Game.remove(phoneEmote);
          // Stays until the message has been heard.
          phoneEmote = MorphCoProps.emote(entity, Emote.EXCLAMATION, Integer.MAX_VALUE);
        });
    on(
        "control-table",
        (self, player) ->
            MorphCoProps.popup(player, "world.control.table-title", "world.control.table"));
    on(
        "coffee",
        (self, player) ->
            MorphCoProps.popup(player, "world.control.coffee-title", "world.control.coffee"));
    on(
        "plant-control",
        (self, player) -> {
          MorphCoProps.popup(player, "world.control.plant-title", "world.control.plant");
          level.seen(MorphCoState.PLANT_PREFIX, "control", player);
        });
    on(
        "fuse-box",
        (self, player) ->
            MorphCoProps.popup(player, "world.control.fuse-box-title", "world.control.fuse-box"));
    on(
        "cctv",
        (self, player) ->
            MorphCoProps.popup(player, "world.control.cctv-title", "world.control.cctv"));
    poster("poster_control1", "poster_luggage", "world.control.poster1");
    poster("poster_control2", "poster_summer", "world.control.poster2");
    poster("poster_control3", "poster_safety", "world.control.poster3");
    poster("poster_control4", "poster_clock", "world.control.poster4");
  }

  private static void on(String name, BiConsumer<Entity, Entity> onInteract) {
    MorphCoObjects.on(name, entity -> MorphCoProps.interact(entity, onInteract));
  }

  private void poster(String name, String art, String textKey) {
    on(
        name,
        (self, player) -> {
          MorphCoProps.picture(player, art, "world.control.poster-title", textKey);
          level.seen(MorphCoState.POSTER_PREFIX, name, player);
        });
  }

  private void cardSlot(Entity player) {
    if (level.door("door_control").isOpen()) {
      MorphCoProps.popup(player, "world.titles.entrance", "world.entrance.card-slot-open");
      return;
    }
    UIComponent ui =
        DialogFactory.show(
            DialogContext.builder().type(MorphCoDialogs.CARD_READER).build(), player.id());
    ui.registerCallback(
        MorphCoDialogs.SUBMIT,
        payload -> {
          if (!(payload instanceof DialogResponseMessage.StringValue(String swipe))) return;
          if (CardSwipe.rate(swipe) != CardSwipe.Result.ACCEPTED) {
            Game.audio().playGlobal(SoundSpec.builder("kenney_ui_error_001").targets(player.id()));
            return;
          }
          Game.audio().playGlobal(SoundSpec.builder("kenney_ui_select_001"));
          // The green light stays on for a moment before the reader closes.
          EventScheduler.scheduleAction(() -> openControlRoom(player), 700);
        });
  }

  private void openControlRoom(Entity player) {
    DoorTile door = level.door("door_control");
    // Another player may have swiped a moment earlier.
    if (door.isOpen()) return;
    door.open();
    // Closes this reader and any other player's; nobody needs it any more.
    Game.levelEntities()
        .flatMap(entity -> entity.fetch(UIComponent.class).stream())
        .filter(ui -> ui.dialogContext().dialogType() == MorphCoDialogs.CARD_READER)
        .toList()
        .forEach(ui -> UIUtils.closeDialog(ui, true));
    level.observe(MorphCoState.CARD_USED);
    Game.audio().playGlobal(SoundSpec.builder("qubodup_DoorOpen"));
    MorphCoTracking.interaction("card-slot", "open-door", player);
    MorphCoProps.popup(player, "world.titles.entrance", "world.entrance.card-slot");
  }

  private void console(Entity player) {
    if (state.done(MorphCoMilestone.ALARM_OFF)) {
      MorphCoProps.popup(player, CONSOLE, "world.control.console-done");
      return;
    }
    List<ChoiceOption> options =
        List.of(
            option("console-conveyor", "conveyor"),
            option("console-alarm", "alarm"),
            option("console-coffee", "coffee"),
            option("console-music", "music"),
            option("console-light", "light"));
    DialogFactory.showMultipleChoiceDialog(
        MorphCoText.key("world.control.console"),
        MorphCoText.key(CONSOLE),
        options,
        false,
        true,
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue(String choice)) {
            pressButton(choice, player);
          }
        },
        () -> {},
        player.id());
  }

  private static ChoiceOption option(String key, String value) {
    return ChoiceOption.of(MorphCoText.key("world.control." + key), value);
  }

  private void pressButton(String button, Entity player) {
    Game.audio().playGlobal(SoundSpec.builder("kenney_ui_switch_001"));
    boolean alarm = "alarm".equals(button);
    MorphCoTracking.attempt(
        MorphCoTracking.Puzzle.ALARM, "console", "choice", button, alarm, player);
    if (!alarm) {
      MorphCoProps.popupReaction(
          player, Emote.ANGER, CONSOLE, "world.control.console-" + button + "-result");
      return;
    }
    if (!level.complete(MorphCoMilestone.ALARM_OFF)) return;
    level.stopAlarm();
    MorphCoProps.switchOn(console);
    if (alarmLight != null) Game.remove(alarmLight);
    alarmLight = null;
    MorphCoTracking.solved(MorphCoTracking.Puzzle.ALARM);
    MorphCoTracking.started(MorphCoTracking.Puzzle.CAUSES);
    MorphCoAchievements.ALARM_OFF.unlock();
    MorphCoProps.popupReaction(
        player, Emote.FACE_HAPPY, CONSOLE, "world.control.console-alarm-off");
  }

  private void phone(Entity player) {
    String intro = state.has(MorphCoState.MESSAGE_HEARD) ? "phone-quiet" : "phone";
    List<ChoiceOption> options =
        List.of(
            option("phone-messages", "messages"),
            option("phone-security", "security"),
            option("phone-rosi", "rosi"),
            option("phone-hang-up", "hang-up"));
    DialogFactory.showMultipleChoiceDialog(
        MorphCoText.key("world.control." + intro),
        MorphCoText.key("world.control.phone-title"),
        options,
        false,
        true,
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue(String choice)) {
            call(choice, player);
          }
        },
        () -> {},
        player.id());
  }

  private void call(String choice, Entity player) {
    switch (choice) {
      case "messages" -> {
        boolean fresh = level.observe(MorphCoState.MESSAGE_HEARD);
        if (phoneEmote != null) Game.remove(phoneEmote);
        phoneEmote = null;
        MorphCoProps.popup(
            player,
            "world.control.phone-title",
            fresh ? "world.control.phone-message" : "world.control.phone-no-message");
      }
      case "security" -> {
        level.observe(MorphCoState.CALLED_SECURITY);
        MorphCoProps.popup(
            player, "world.control.phone-title", "world.control.phone-security-result");
        nobodyHome(player);
      }
      case "rosi" -> {
        level.observe(MorphCoState.CALLED_ROSI);
        MorphCoProps.popup(player, "world.control.phone-title", "world.control.phone-rosi-result");
        nobodyHome(player);
      }
      default -> {
        // Hanging up needs no feedback.
      }
    }
  }

  private void nobodyHome(Entity player) {
    if (state.has(MorphCoState.CALLED_SECURITY) && state.has(MorphCoState.CALLED_ROSI)) {
      MorphCoAchievements.NOBODY_HOME.unlock(player);
    }
  }

  private void computer(Entity player) {
    if (!state.done(MorphCoMilestone.ALARM_OFF)) {
      MorphCoProps.popupReaction(player, Emote.SWIRL, "pc.title", "pc.too-loud");
      return;
    }
    if (level.complete(MorphCoMilestone.CAUSES_FOUND)) {
      level.door("door_production").open();
      MorphCoTracking.solved(MorphCoTracking.Puzzle.CAUSES);
      MorphCoTracking.started(MorphCoTracking.Puzzle.HATCH);
      MorphCoTracking.started(MorphCoTracking.Puzzle.WORD_CLASSES);
    }
    boolean f04 = state.done(MorphCoMilestone.F04_REPAIRED);
    boolean a12 = state.done(MorphCoMilestone.A12_REPAIRED);
    if (f04 && a12) MorphCoTracking.started(MorphCoTracking.Puzzle.RESTART);
    DialogContext context =
        DialogContext.builder()
            .type(MorphCoDialogs.COMPUTER)
            .put(MorphCoDialogs.F04_OK, f04)
            .put(MorphCoDialogs.A12_OK, a12)
            .build();
    UIComponent ui = DialogFactory.show(context, player.id());
    ui.registerCallback(
        MorphCoDialogs.SUBMIT,
        payload -> {
          if (!(payload instanceof DialogResponseMessage.StringValue(String answer))) return;
          boolean ready =
              state.done(MorphCoMilestone.F04_REPAIRED)
                  && state.done(MorphCoMilestone.A12_REPAIRED);
          boolean correct = ready && MorphemeQuestions.restartAnswer(answer);
          MorphCoTracking.attempt(
              MorphCoTracking.Puzzle.RESTART,
              "security-question",
              "number",
              answer,
              correct,
              player);
          // A short pause lets the player read the confirmation on the screen.
          if (correct) EventScheduler.scheduleAction(() -> level.finish(player), 1500);
        });
  }
}
