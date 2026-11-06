package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.level.elements.tile.DoorTile;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.SoundSpec;
import engine.utils.Rectangle;
import feature.components.UIComponent;
import feature.emote.Emote;
import feature.emote.EmoteFactory;
import feature.entities.deco.Deco;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.systems.EventScheduler;
import java.util.List;
import rooms.morphco.petrinet.MorphCoMilestone;
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

  private static final int EMOTE_DURATION_MS = 60 * 60 * 1000;
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

    MorphCoProps.texture(
        "card-slot",
        level.point("card_slot"),
        "objects/tech/Hand_scanner.png",
        0.8f,
        null,
        (self, player) -> cardSlot(player));
    MorphCoProps.texture(
        "warning-sign",
        level.point("warning_sign"),
        "objects/tech/Screen_info_2.png",
        0.9f,
        null,
        (self, player) ->
            MorphCoProps.popup(player, "world.titles.entrance", "world.entrance.warning-sign"));

    console =
        MorphCoProps.switchable(
            "console",
            level.point("console"),
            MorphCoProps.ART + "console_alarm.png",
            MorphCoProps.ART + "console_off.png",
            1.62f,
            new Rectangle(2f, 0.8f, 0.05f, 0f),
            (self, player) -> console(player));
    if (state.done(MorphCoMilestone.ALARM_OFF)) MorphCoProps.switchOn(console);
    if (!state.done(MorphCoMilestone.ALARM_OFF)) {
      alarmLight =
          MorphCoProps.texture(
              MorphCoLighting.ALARM_LIGHT,
              level.point("alarm_light"),
              "objects/tech/Alert_1.png",
              0.45f,
              null,
              null);
    }
    MorphCoProps.texture(
        "pc",
        level.point("pc"),
        "office/desk-with-pc.png",
        2f,
        new Rectangle(1.9f, 0.9f, 0.05f, 0f),
        (self, player) -> computer(player));
    Entity phone =
        MorphCoProps.tableTexture(
            "phone",
            level.point("phone"),
            level.point("control_table"),
            MorphCoProps.ART + "phone.png",
            9 / 16f,
            (self, player) -> phone(player));
    if (!state.has(MorphCoState.MESSAGE_HEARD)) {
      phoneEmote =
          EmoteFactory.createEmote(
              level.point("phone").translate(0f, 0.7f), Emote.EXCLAMATION, EMOTE_DURATION_MS);
      Game.add(phoneEmote);
    }
    phone.name("phone");

    MorphCoProps.texture(
        "control-table",
        level.point("control_table"),
        MorphCoProps.ART + "table.png",
        19 / 16f,
        new Rectangle(2.4f, 0.5f, 0.05f, 0f),
        (self, player) ->
            MorphCoProps.popup(player, "world.control.table-title", "world.control.table"));
    MorphCoProps.tableDeco(
        "coffee",
        level.point("coffee"),
        level.point("control_table"),
        Deco.Cup,
        (self, player) ->
            MorphCoProps.popup(player, "world.control.coffee-title", "world.control.coffee"));
    MorphCoProps.deco(
        "plant-control",
        level.point("plant_control"),
        Deco.Plant,
        (self, player) -> {
          MorphCoProps.popup(player, "world.control.plant-title", "world.control.plant");
          level.seen(MorphCoState.PLANT_PREFIX, "control", player);
        });
    MorphCoProps.deco(
        "fuse-box",
        level.point("fuse_box"),
        Deco.LabElectricPanel,
        (self, player) ->
            MorphCoProps.popup(player, "world.control.fuse-box-title", "world.control.fuse-box"));
    MorphCoProps.deco(
        "cctv",
        level.point("cctv"),
        Deco.LabWallDevice,
        (self, player) ->
            MorphCoProps.popup(player, "world.control.cctv-title", "world.control.cctv"));
    poster("poster_control1", "poster_luggage.png", "world.control.poster1");
    poster("poster_control2", "poster_summer.png", "world.control.poster2");
    poster("poster_control3", "poster_safety.png", "world.control.poster3");
    poster("poster_control4", "poster_clock.png", "world.control.poster4");
    MorphCoProps.texture(
        "production-sign",
        level.point("production_sign"),
        MorphCoProps.ART + "sign_production.png",
        0.81f,
        null,
        (self, player) -> productionSign(player));
  }

  private void poster(String point, String art, String textKey) {
    MorphCoProps.texture(
        point,
        level.point(point),
        MorphCoProps.ART + art,
        1.3f,
        null,
        (self, player) -> {
          MorphCoProps.popup(player, "world.control.poster-title", textKey);
          level.seen(MorphCoState.POSTER_PREFIX, point, player);
        });
  }

  private void cardSlot(Entity player) {
    DoorTile door = level.door("door_control");
    if (door.isOpen()) {
      MorphCoProps.popup(player, "world.titles.entrance", "world.entrance.card-slot-open");
      return;
    }
    door.open();
    level.observe(MorphCoState.CARD_USED);
    Game.audio().playGlobal(SoundSpec.builder("kenney_ui_select_001"));
    Game.audio().playGlobal(SoundSpec.builder("qubodup_DoorOpen"));
    MorphCoTracking.interaction("card-slot", "open-door", player);
    MorphCoProps.popup(player, "world.titles.entrance", "world.entrance.card-slot");
  }

  private void productionSign(Entity player) {
    if (state.done(MorphCoMilestone.CAUSES_FOUND)) {
      MorphCoProps.popup(player, "world.corridor.sign-title", "world.control.production-sign");
    } else {
      MorphCoProps.popup(player, "world.corridor.sign-title", "world.control.production-locked");
    }
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
      MorphCoProps.popup(player, CONSOLE, "world.control.console-" + button + "-result");
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
    MorphCoProps.popup(player, CONSOLE, "world.control.console-alarm-off");
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
      MorphCoProps.popup(player, "pc.title", "pc.too-loud");
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
