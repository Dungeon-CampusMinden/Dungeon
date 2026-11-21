package rooms.morphco.level;

import engine.Entity;
import engine.Game;
import engine.sound.CoreSounds;
import engine.sound.Sounds;
import engine.utils.IVoidFunction;
import feature.emote.Emote;
import feature.hud.dialogs.ChoiceOption;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.phone.PhoneCall;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import rooms.morphco.util.MorphCoAchievements;
import rooms.morphco.util.MorphCoSounds;
import rooms.morphco.util.MorphCoText;

/**
 * The hidden side quest around the chocolate bar in the snack machine at the west end of the
 * corridor, together with the kitchen and the storage room it unlocks.
 *
 * <p>The bar is there, the money is not. The shift lead would lend some, but her locker key is lost
 * in the storage room, which the janitor unlocks by phone. With the key back she opens the kitchen,
 * where a banknote lies in the coffee kitty. The machine eats the note and the bar gets stuck.
 * Every step is a flag of the whole team in the savegame; nothing on the main path waits for it.
 */
final class SnackQuest {

  /** Extension of the janitor on the phone in the control room; the storage room door names it. */
  static final String JANITOR = "66";

  /** Value of the shift lead's choice option that brings up the quest. */
  static final String OPTION = "snack";

  private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

  private final MorphCoLevel level;
  private final MorphCoState state;
  private Entity machine;

  SnackQuest(MorphCoLevel level) {
    this.level = level;
    this.state = level.state();
  }

  /**
   * Locks the doors the quest has not opened yet and places the machine, the key, the note and the
   * coffee maker.
   */
  void spawn() {
    if (!state.has(MorphCoState.KITCHEN_OPEN)) level.door("door_kitchen").close();
    if (!state.has(MorphCoState.STORAGE_OPEN)) level.door("door_storage").close();
    MorphCoObjects.onInteract(
        "sign_kitchen",
        (self, player) ->
            doorSign(
                player,
                MorphCoState.KITCHEN_OPEN,
                "world.corridor.sign-kitchen",
                "world.corridor.kitchen"));
    MorphCoObjects.onInteract(
        "sign_storage",
        (self, player) ->
            doorSign(
                player,
                MorphCoState.STORAGE_OPEN,
                "world.corridor.sign-storage",
                "world.corridor.storage",
                JANITOR));
    MorphCoObjects.on(
        "snack-machine",
        entity -> {
          machine = entity;
          MorphCoProps.interact(entity, (self, player) -> machine(player));
        });
    pickUp("locker-key", MorphCoState.LOCKER_KEY, "world.snack.key");
    pickUp("banknote", MorphCoState.BANKNOTE, "world.snack.banknote");
    // A joke without a flag: the coffee maker reads the server's clock at every interaction.
    MorphCoObjects.onInteract(
        "coffee-maker",
        (self, player) ->
            MorphCoProps.note(player, "world.snack.coffee", LocalTime.now().format(CLOCK)));
  }

  /**
   * Returns what the player can bring up with the shift lead besides asking for a hint.
   *
   * @return the money question while the quest waits for it, the found key while she lacks it
   */
  Optional<ChoiceOption> shiftLeadOption() {
    if (state.has(MorphCoState.LOCKER_KEY) && !state.has(MorphCoState.KITCHEN_OPEN)) {
      return Optional.of(ChoiceOption.of(MorphCoText.key("story.snack-key-option"), OPTION));
    }
    if (state.has(MorphCoState.SNACK_SEEN) && !state.has(MorphCoState.SNACK_ASKED)) {
      return Optional.of(ChoiceOption.of(MorphCoText.key("story.snack-money-option"), OPTION));
    }
    return Optional.empty();
  }

  /**
   * Lets the shift lead answer the option of {@link #shiftLeadOption()}: she sends the player for
   * her locker key, or takes it back and unlocks the kitchen. If another player already gave her
   * the key, only {@code onDone} runs.
   *
   * @param player player who talks to her
   * @param onDone runs after her answer
   */
  void answerShiftLead(Entity player, IVoidFunction onDone) {
    boolean key = state.has(MorphCoState.LOCKER_KEY);
    if (key && !level.observe(MorphCoState.KITCHEN_OPEN)) {
      // Another player handed the key back while this menu was open; she thanks only once.
      onDone.execute();
      return;
    }
    if (key) {
      unlock("door_kitchen");
    } else if (level.observe(MorphCoState.SNACK_ASKED)) {
      level.save();
    }
    DialogFactory.showDialogDialog(
        MorphCoText.shiftLead(key ? "snack-key" : "snack-money"), onDone, player.id());
  }

  /**
   * Answers a call of the janitor. He unlocks the storage room only once the shift lead has sent
   * the player for her key; before that he asks who sent the caller, so the quest keeps its order
   * and the extension on the door does not lead into an empty room.
   *
   * @param call the call on the phone in the control room
   */
  void call(PhoneCall call) {
    if (state.has(MorphCoState.STORAGE_OPEN)) {
      call.say(MorphCoText.key("world.snack.janitor-again"));
    } else if (state.has(MorphCoState.SNACK_ASKED)) {
      level.observe(MorphCoState.STORAGE_OPEN);
      unlock("door_storage");
      call.say(MorphCoText.key("world.snack.janitor"));
    } else {
      call.say(MorphCoText.key("world.snack.janitor-refuses"));
    }
  }

  private void unlock(String door) {
    level.door(door).open();
    Sounds.play(CoreSounds.DOOR_OPEN);
    level.save();
  }

  private void machine(Entity player) {
    if (state.has(MorphCoState.BANKNOTE) && level.observe(MorphCoState.SNACK_STUCK)) {
      level.save();
      Sounds.play(MorphCoSounds.NOTE_EATEN);
      MorphCoProps.emote(machine, Emote.CROSS);
      MorphCoProps.noteReaction(player, Emote.FACE_SAD, "world.snack.machine-eats");
      MorphCoAchievements.SNACK.unlock(player);
    } else if (state.has(MorphCoState.SNACK_STUCK)) {
      MorphCoProps.note(player, "world.snack.machine-stuck");
    } else if (level.observe(MorphCoState.SNACK_SEEN)) {
      level.save();
      MorphCoProps.noteReaction(player, Emote.CASH, "world.snack.machine");
    } else {
      MorphCoProps.note(player, "world.snack.machine-no-money");
    }
  }

  // The door signs say whether the room is locked; the storage room names the janitor's extension.
  private void doorSign(
      Entity player, String openFlag, String signKey, String lockedKey, Object... values) {
    if (state.has(openFlag)) {
      MorphCoProps.note(player, signKey);
    } else {
      MorphCoProps.note(player, lockedKey, values);
    }
  }

  // Key and note belong to the team: whoever is first takes it from the level for everyone.
  private void pickUp(String name, String flag, String textKey) {
    MorphCoObjects.on(
        name,
        entity -> {
          if (state.has(flag)) {
            Game.remove(entity);
            return;
          }
          MorphCoProps.interact(
              entity,
              (self, player) -> {
                if (!level.observe(flag)) return;
                Game.remove(self);
                level.save();
                Sounds.playFor(MorphCoSounds.TAKE, player);
                MorphCoProps.note(player, textKey);
              });
        });
  }
}
