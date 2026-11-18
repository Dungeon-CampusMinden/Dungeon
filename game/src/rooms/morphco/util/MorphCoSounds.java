package rooms.morphco.util;

import engine.sound.ISound;

/** Sounds of the Morph & Co. dialogs, played only for the player who uses the dialog. */
public enum MorphCoSounds implements ISound {
  /** Crumpled slips rustling in the waste paper basket of F-04. */
  RUSTLE("qubodup_rustle05", .6f),
  /** A page of the hamster book being turned. */
  PAGE_FLIP("flipping_through_book", 1f);

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
