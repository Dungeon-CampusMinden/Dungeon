package core.sound;

/** Interface for sound effects, providing necessary information for playback. */
public interface ISound {

  /**
   * Returns the name of the sound file to be played.
   *
   * @return A string representing the sound file name
   */
  String soundName();

  /**
   * Returns the volume level for the sound.
   *
   * @return A float representing the volume level
   */
  float volume();
}
