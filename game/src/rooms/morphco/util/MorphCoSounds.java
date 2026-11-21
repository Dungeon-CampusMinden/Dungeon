package rooms.morphco.util;

import engine.sound.CoreSounds;
import engine.sound.ISound;

/**
 * Sounds of Morph & Co. The dialogs play theirs on the client of the player who uses them, the
 * level plays the others from the server.
 */
public enum MorphCoSounds implements ISound {
  /** Crumpled slips rustling in the waste paper basket of F-04. */
  RUSTLE("qubodup_rustle05", .6f),
  /** A page of the hamster book being turned. */
  PAGE_FLIP("flipping_through_book", 1f),
  /** Something goes into the player's pocket. */
  TAKE("kenney_ui_drop_001", .5f),
  /** A machine rejects a wrong answer. */
  WRONG("kenney_ui_error_001", .5f),
  /** The lock of the hatch rejects the number. */
  HATCH_LOCKED("kenney_ui_error_002", .5f),
  /** The assembly station in A-12 knows no circumfix of the two parts. */
  NO_CIRCUMFIX("kenney_ui_error_003", .5f),
  /** The assembly station in A-12 builds a circumfix. */
  ASSEMBLED(CoreSounds.TRASH_MINIGAME_WIN.soundName(), .5f),
  /** The snack machine swallows the banknote. */
  NOTE_EATEN("kenney_ui_drop_002", .5f),
  /** A button on the console in the control room. */
  CONSOLE_BUTTON(CoreSounds.SETTINGS_TOGGLE_CLICK.soundName(), .5f),
  /** A digit key of the phone; its pitch rises with the digit. */
  KEY_BEEP("retro_beep_01", .5f);

  private final String soundName;
  private final float volume;

  MorphCoSounds(String soundName, float volume) {
    this.soundName = soundName;
    this.volume = volume;
  }

  @Override
  public String soundName() {
    return soundName;
  }

  @Override
  public float volume() {
    return volume;
  }
}
