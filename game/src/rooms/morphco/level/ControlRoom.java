package rooms.morphco.level;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.network.messages.s2c.DialogFeedbackMessage;
import engine.sound.CoreSounds;
import engine.sound.Sounds;
import feature.components.UIComponent;
import feature.emote.Emote;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogFeedbackRouter;
import feature.interaction.cables.CablePuzzle;
import feature.interaction.cables.Cables;
import feature.interaction.cables.CablesComponent;
import feature.interaction.cardreader.CardReaderComponent;
import feature.interaction.cardreader.CardReaderFactory;
import feature.interaction.phone.PhoneCall;
import feature.interaction.phone.PhoneComponent;
import feature.interaction.phone.PhoneFactory;
import feature.interaction.phone.PhoneLine;
import feature.systems.EventScheduler;
import feature.utils.EntityUtils;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;
import rooms.morphco.petrinet.MorphCoMilestone;
import rooms.morphco.puzzles.MorphemeQuestions;
import rooms.morphco.ui.MorphCoDialogs;
import rooms.morphco.util.MorphCoAchievements;
import rooms.morphco.util.MorphCoSounds;
import rooms.morphco.util.MorphCoText;
import rooms.morphco.util.MorphCoTracking;

/**
 * The entrance and the control room: locker with the card, card slot, alarm console, phone with the
 * hint hotline, fuse box and the production control system, which restarts the production at the
 * end.
 *
 * <p>The door to the corridor opens once the fuse box is wired: whoever plugs in the last cable
 * opens it. The computer tells the players which machines are broken, but the door does not wait
 * for it. Console, phone and computer run on the backup battery, so they work without the cables.
 * The wired fuse box also switches on the emergency lighting.
 */
final class ControlRoom {

  /** Number of the mailbox with the message of the morning. */
  static final String MAILBOX = "99";

  // Rosi's mobile number on the note, a gag and the longest number the phone takes. Under her
  // extension 17 from the shift plan nobody answers either.
  private static final String ROSI_MOBILE = "01721717471108151717";
  private static final String CONSOLE = "world.control.console-title";

  // The cables of the fuse box from top to bottom; a cable and its socket share label and color.
  // The sockets sit in the order control room, emergency lighting, F-04, corridor, A-12.
  static final CablePuzzle FUSE_BOX =
      new CablePuzzle(
          List.of(
              new CablePuzzle.Cable(fuseBoxLabel("f04"), Color.valueOf("e0483e"), 2),
              new CablePuzzle.Cable(fuseBoxLabel("a12"), Color.valueOf("3d7be0"), 4),
              new CablePuzzle.Cable(fuseBoxLabel("corridor"), Color.valueOf("f7c31d"), 3),
              new CablePuzzle.Cable(fuseBoxLabel("control"), Color.valueOf("eef2f4"), 0),
              new CablePuzzle.Cable(fuseBoxLabel("emergency"), Color.valueOf("63e283"), 1)),
          Stream.of("control", "emergency", "f04", "corridor", "a12")
              .map(ControlRoom::fuseBoxLabel)
              .toList());

  private final MorphCoLevel level;
  private final MorphCoState state;
  private final HintHotline hotline;
  private Entity alarmLight;
  private Entity console;
  private Entity phoneEmote;
  private Entity emergencyLight;

  ControlRoom(MorphCoLevel level) {
    this.level = level;
    this.state = level.state();
    this.hotline = new HintHotline(level);
  }

  /** Creates the objects of the entrance and the control room and sets the doors. */
  void spawn() {
    if (!state.has(MorphCoState.CARD_USED)) level.door("door_control").close();
    if (!state.corridorOpen()) level.door("door_production").close();
    level.door("door_exit").close();

    MorphCoObjects.onInteract("lockers-1", (self, player) -> locker(player));
    MorphCoObjects.on("card-slot", this::setUpCardReader);
    MorphCoObjects.onInteract(
        "warning-sign", (self, player) -> MorphCoProps.note(player, "world.entrance.warning-sign"));
    MorphCoObjects.onInteract(
        "exit-door", (self, player) -> MorphCoProps.note(player, "world.entrance.exit"));
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
    MorphCoObjects.onInteract("pc", (self, player) -> computer(player));
    MorphCoObjects.on(
        "phone",
        entity -> {
          entity.add(phone());
          MorphCoProps.interact(
              entity,
              (self, player) ->
                  PhoneFactory.use(
                      self,
                      player,
                      state.has(MorphCoState.MESSAGE_HEARD)
                          ? Map.of()
                          : Map.of(MorphCoDialogs.MESSAGE_WAITING, MAILBOX)));
          if (state.has(MorphCoState.MESSAGE_HEARD)) return;
          if (phoneEmote != null) Game.remove(phoneEmote);
          // Stays until the message has been heard.
          phoneEmote = MorphCoProps.emote(entity, Emote.EXCLAMATION, Integer.MAX_VALUE);
        });
    MorphCoObjects.onInteract(
        "control-table", (self, player) -> MorphCoProps.note(player, "world.control.table"));
    MorphCoObjects.onInteract(
        "coffee", (self, player) -> MorphCoProps.note(player, "world.control.coffee"));
    MorphCoObjects.onInteract(
        "plant-control",
        (self, player) -> {
          MorphCoProps.note(player, "world.control.plant");
          level.seen(MorphCoState.PLANT_PREFIX, "control", player);
        });
    MorphCoObjects.on(
        "fuse-box",
        entity -> {
          CablesComponent cables = new CablesComponent(FUSE_BOX, true);
          cables.connected(state.has(MorphCoState.FUSE_BOX_WIRED));
          cables.onConnected(player -> wired(entity, player));
          // The engine reports only who plugged a cable into another cable's socket.
          cables.onWrongPlug(player ->
              MorphCoTracking.attempt(
                  MorphCoTracking.Puzzle.FUSE_BOX, "fuse-box", "plug", "wrong", false, player));
          entity.add(cables);
          MorphCoProps.interact(entity, this::fuseBox);
          if (state.has(MorphCoState.FUSE_BOX_WIRED)) switchOnEmergencyLight(entity);
        });
    MorphCoObjects.onInteract(
        "cctv", (self, player) -> MorphCoProps.note(player, "world.control.cctv"));
    poster("poster_control1", "poster_luggage", "world.control.poster1");
    poster("poster_control2", "poster_summer", "world.control.poster2");
    poster("poster_control3", "poster_safety", "world.control.poster3");
    poster("poster_control4", "poster_clock", "world.control.poster4");
  }

  private void poster(String name, String art, String textKey) {
    MorphCoObjects.onInteract(
        name,
        (self, player) -> {
          MorphCoProps.picture(player, art, textKey);
          level.seen(MorphCoState.POSTER_PREFIX, name, player);
        });
  }

  private void setUpCardReader(Entity entity) {
    CardReaderComponent reader = new CardReaderComponent(CardReaderFactory.DEFAULT_PACE);
    reader.onAccepted(this::openControlRoom);
    entity.add(reader);
    MorphCoProps.interact(entity, this::cardSlot);
  }

  // The room has one card; the door stays open after the first swipe, so it lets every player in.
  private void locker(Entity player) {
    if (!level.observe(MorphCoState.CARD_TAKEN)) {
      MorphCoProps.note(player, "world.entrance.locker-empty");
      return;
    }
    level.save();
    MorphCoTracking.interaction("lockers-1", "take-card", player);
    MorphCoProps.note(player, "world.entrance.locker");
  }

  private void cardSlot(Entity reader, Entity player) {
    if (level.door("door_control").isOpen()) {
      MorphCoProps.note(player, "world.entrance.card-slot-open");
      return;
    }
    if (!state.has(MorphCoState.CARD_TAKEN)) {
      MorphCoProps.note(player, "world.entrance.card-missing");
      return;
    }
    CardReaderFactory.use(reader, player);
  }

  // The reader accepts a single swipe and has already closed for every player.
  private void openControlRoom(Entity player) {
    level.door("door_control").open();
    if (level.observe(MorphCoState.CARD_USED)) level.save();
    Sounds.play(CoreSounds.DOOR_OPEN);
    MorphCoTracking.interaction("card-slot", "open-door", player);
    MorphCoTracking.started(MorphCoTracking.Puzzle.FUSE_BOX);
  }

  private void console(Entity player) {
    if (state.done(MorphCoMilestone.ALARM_OFF)) {
      MorphCoProps.popup(player, CONSOLE, "world.control.console-done");
      return;
    }
    UIComponent ui =
        DialogFactory.show(
            DialogContext.builder().type(MorphCoDialogs.CONSOLE).build(), player.id());
    // The close-up stays open and shows the answers; the player reacts once it is closed.
    AtomicReference<Emote> reaction = new AtomicReference<>();
    ui.registerCallback(
        MorphCoDialogs.SUBMIT,
        payload -> {
          if (payload instanceof DialogResponseMessage.StringValue(String button)) {
            reaction.set(pressButton(button, ui, player));
          }
        });
    ui.registerCallback(
        DialogContextKeys.ON_CLOSE,
        payload -> {
          if (reaction.get() != null) MorphCoProps.emote(player, reaction.get());
        });
  }

  private Emote pressButton(String button, UIComponent ui, Entity player) {
    Sounds.play(MorphCoSounds.CONSOLE_BUTTON);
    boolean alarm = "alarm".equals(button);
    MorphCoTracking.attempt(
        MorphCoTracking.Puzzle.ALARM, "console", "choice", button, alarm, player);
    if (!alarm && state.done(MorphCoMilestone.ALARM_OFF)) {
      answer(ui, player, "world.control.console-quiet", true);
      return Emote.FACE_HAPPY;
    }
    if (!alarm) {
      boolean lit = "light".equals(button) && state.has(MorphCoState.FUSE_BOX_WIRED);
      answer(
          ui,
          player,
          lit ? "world.control.console-light-on" : "world.control.console-" + button + "-result",
          false);
      return Emote.ANGER;
    }
    if (!level.complete(MorphCoMilestone.ALARM_OFF)) {
      answer(ui, player, "world.control.console-done", true);
      return Emote.FACE_HAPPY;
    }
    // Close-ups of other players still show the alarm; clicking the console again shows it is off.
    String dialogId = ui.dialogContext().dialogId();
    UIUtils.closeDialogs(MorphCoDialogs.CONSOLE, context -> !context.dialogId().equals(dialogId));
    level.stopAlarm();
    MorphCoProps.switchOn(console);
    if (alarmLight != null) Game.remove(alarmLight);
    alarmLight = null;
    MorphCoTracking.solved(MorphCoTracking.Puzzle.ALARM);
    MorphCoTracking.started(MorphCoTracking.Puzzle.CAUSES);
    MorphCoAchievements.ALARM_OFF.unlock();
    answer(ui, player, "world.control.console-alarm-off", true);
    return Emote.FACE_HAPPY;
  }

  // Only the close-up of the player who pressed shows the answer; silenced tells it the alarm is
  // off.
  private static void answer(UIComponent ui, Entity player, String textKey, boolean silenced) {
    DialogFeedbackRouter.send(
        player, new DialogFeedbackMessage(ui.dialogContext().dialogId(), textKey, silenced));
  }

  private void fuseBox(Entity box, Entity player) {
    if (state.has(MorphCoState.FUSE_BOX_WIRED)) {
      MorphCoProps.popup(player, "world.control.fuse-box-title", "world.control.fuse-box-wired");
      return;
    }
    // Cables closes the other players' boxes once all cables are in.
    Cables.show(
        box,
        player,
        MorphCoText.key("world.control.fuse-box-title"),
        MorphCoText.key("world.control.fuse-box-help"));
  }

  private static String fuseBoxLabel(String part) {
    return MorphCoText.key("world.control.fuse-box-" + part);
  }

  private void wired(Entity box, Entity player) {
    if (!level.observe(MorphCoState.FUSE_BOX_WIRED)) return;
    MorphCoTracking.interaction("fuse-box", "wired", player);
    MorphCoTracking.attempt(
        MorphCoTracking.Puzzle.FUSE_BOX, "fuse-box", "wiring", "connected", true, player);
    MorphCoTracking.solved(MorphCoTracking.Puzzle.FUSE_BOX);
    level.save();
    switchOnEmergencyLight(box);
    MorphCoAchievements.FUSE_BOX.unlock(player);
    level.door("door_production").open();
    Sounds.play(CoreSounds.DOOR_OPEN);
    MorphCoTracking.started(MorphCoTracking.Puzzle.HATCH);
    MorphCoTracking.started(MorphCoTracking.Puzzle.WORD_CLASSES);
  }

  // The fuse box turns green, and the entity at it tells every client that the emergency lighting
  // is on.
  private void switchOnEmergencyLight(Entity box) {
    MorphCoProps.switchOn(box);
    if (emergencyLight != null) Game.remove(emergencyLight);
    emergencyLight =
        MorphCoProps.buildTexture(
            MorphCoLighting.EMERGENCY_LIGHT,
            EntityUtils.getPosition(box),
            MorphCoProps.ART + "hotspot.png",
            0.5f,
            null);
    Game.add(emergencyLight);
  }

  // The note on the phone lists the mailbox, the hotline, the security service and Rosi's mobile.
  // Rosi's extension is on the shift plan on the table; the error codes of F-04 and A-12 come from
  // the production control system and get an announcement. Every line answers in the phone.
  private PhoneComponent phone() {
    PhoneComponent phone = new PhoneComponent(ROSI_MOBILE.length());
    phone.addLine(new PhoneLine(MAILBOX, "ui.phone.MAILBOX", true), this::mailbox);
    phone.addLine(new PhoneLine("42", "ui.phone.HOTLINE", true), hotline::call);
    phone.addLine(
        new PhoneLine("11", "ui.phone.SECURITY", true),
        call ->
            nobodyAnswers(
                call, MorphCoState.CALLED_SECURITY, "world.control.phone-security-result"));
    Consumer<PhoneCall> rosi =
        call -> nobodyAnswers(call, MorphCoState.CALLED_ROSI, "world.control.phone-rosi-result");
    phone.addLine(new PhoneLine(ROSI_MOBILE, "ui.phone.ROSI", true), rosi);
    phone.addLine(new PhoneLine("17", "", false), rosi);
    phone.addLine(new PhoneLine(SnackQuest.JANITOR, "", false), level.snackQuest()::call);
    for (String code : List.of("133", "204")) {
      phone.addLine(
          new PhoneLine(code, "", false),
          call -> call.say(MorphCoText.key("world.control.phone-error-code", code)));
    }
    return phone;
  }

  private void mailbox(PhoneCall call) {
    boolean fresh = level.observe(MorphCoState.MESSAGE_HEARD);
    if (phoneEmote != null) Game.remove(phoneEmote);
    phoneEmote = null;
    call.say(
        MorphCoText.key(fresh ? "world.control.phone-message" : "world.control.phone-no-message"));
  }

  private void nobodyAnswers(PhoneCall call, String flag, String textKey) {
    boolean first = level.observe(flag);
    call.say(MorphCoText.key(textKey));
    if (first) nobodyHome(call.caller());
  }

  // Only the first call of a line can complete the pair, so the achievement pops up once.
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
      MorphCoTracking.solved(MorphCoTracking.Puzzle.CAUSES);
    }
    boolean f04 = state.done(MorphCoMilestone.F04_REPAIRED);
    boolean a12 = state.done(MorphCoMilestone.A12_REPAIRED);
    if (f04 && a12) MorphCoTracking.started(MorphCoTracking.Puzzle.RESTART);
    DialogContext context =
        DialogContext.builder()
            .type(MorphCoDialogs.COMPUTER)
            .put(MorphCoDialogs.F04_OK, f04)
            .put(MorphCoDialogs.A12_OK, a12)
            .put(MorphCoDialogs.A12_EMPTY, state.waitsForCircumfixes())
            .put(MorphCoDialogs.SHAFTS_OK, state.has(MorphCoState.SHAFTS_FILLED))
            .put(MorphCoDialogs.POWER_OK, state.has(MorphCoState.FUSE_BOX_WIRED))
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
          // The ending waits until the systems have reported back on the screen.
          if (correct)
            EventScheduler.scheduleAction(
                () -> level.finish(player), MorphCoDialogs.RESTART_MILLIS);
        });
  }
}
