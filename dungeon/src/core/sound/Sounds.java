package core.sound;

import core.Game;
import core.utils.settings.ClientSettings;

/**
 * Utility class for playing sounds with various options.
 */
public class Sounds {

  private static float getEffectsVolume(){
    float master = (float)ClientSettings.masterVolume() / 100;
    float effects = (float)ClientSettings.effectsVolume() / 100;
    return master * effects;
  }

  /**
   * Play a random sound from the provided list.
   *
   * @param sounds the candidate sounds to choose from
   */
  public static long random(ISound... sounds) {
    return random(1, sounds);
  }

  /**
   * Play a random sound from the provided list with a pitch.
   *
   * @param pitch the playback pitch to apply
   * @param sounds the candidate sounds to choose from
   */
  public static long random(float pitch, ISound... sounds) {
    int index = (int) (Math.random() * sounds.length);
    return play(sounds[index], pitch);
  }

  /**
   * Play this sound with its default volume.
   *
   * @return an Optional holding the play handle if playback started
   */
  public static long play(ISound sound) {
    return play(sound, 1);
  }

  /**
   * Play this sound with a custom pitch.
   *
   * @param pitch the playback pitch to apply
   * @return an Optional holding the play handle if playback started
   */
  public static long play(ISound sound, float pitch) {
    return play(sound, pitch, 1);
  }

  /**
   * Play this sound with a custom pitch and volume.
   *
   * @param pitch the playback pitch to apply
   * @param volumeModifier the playback volume modifier to apply
   * @return the id of the sound instance if playback started, or -1 if it failed to play
   */
  public static long play(ISound sound, float pitch, float volumeModifier) {
    float volume = getEffectsVolume() * sound.volume() * volumeModifier;
    return Game.audio().playGlobal(new SoundSpec.Builder(sound.soundName()).volume(volume).pitch(pitch));
  }

  /**
   * Play this sound with its default volume.
   *
   * @return an Optional holding the play handle if playback started
   */
  public static long playLocal(ISound sound) {
    return playLocal(sound, 1);
  }

  /**
   * Play this sound with a custom pitch.
   *
   * @param pitch the playback pitch to apply
   * @return an Optional holding the play handle if playback started
   */
  public static long playLocal(ISound sound, float pitch) {
    return playLocal(sound, pitch, 1);
  }

  /**
   * Play this sound with a custom pitch and volume.
   *
   * @param pitch the playback pitch to apply
   * @param volumeModifier the playback volumeModifier to apply
   * @return the id of the sound instance if playback started, or -1 if it failed to play
   */
  public static long playLocal(ISound sound, float pitch, float volumeModifier) {
    //TODO play only for the local player
    float volume = getEffectsVolume() * sound.volume() * volumeModifier;
    return Game.audio().playGlobal(new SoundSpec.Builder(sound.soundName()).volume(volume).pitch(pitch));
  }
}
