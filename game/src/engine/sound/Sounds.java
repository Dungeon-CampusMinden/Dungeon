package engine.sound;

import engine.Game;
import engine.utils.settings.ClientSettings;

/** Utility class for playing sounds with various options. */
public class Sounds {

  /**
   * Plays local UI feedback immediately, without entities or network messages.
   *
   * @param sound the sound to play
   */
  public static void playUi(ISound sound) {
    playUi(sound, 1);
  }

  /**
   * Plays local UI feedback with a custom pitch.
   *
   * @param sound the sound to play
   * @param pitch playback pitch
   */
  public static void playUi(ISound sound, float pitch) {
    playUi(sound, pitch, 1);
  }

  /**
   * Plays local UI feedback using the master and effects volume settings. Works before gameplay
   * starts; the application updates and disposes the shared player.
   *
   * @param sound the sound to play
   * @param pitch playback pitch
   * @param volumeModifier additional volume multiplier
   */
  public static void playUi(ISound sound, float pitch, float volumeModifier) {
    long instanceId = Game.audio().newLocalInstanceId();
    float volume = getEffectsVolume() * sound.volume() * volumeModifier;
    Game.soundPlayer()
        .playWithInstance(instanceId, sound.soundName(), volume, false, pitch, 0, null);
  }

  private static float getEffectsVolume() {
    float master = (float) ClientSettings.masterVolume() / 100;
    float effects = (float) ClientSettings.effectsVolume() / 100;
    return master * effects;
  }

  /**
   * Play a random sound from the provided list.
   *
   * @param sounds the candidate sounds to choose from
   * @return the id of the sound instance if playback started, or -1 if it failed to play
   */
  public static long random(ISound... sounds) {
    return random(1, sounds);
  }

  /**
   * Play a random sound from the provided list with a pitch.
   *
   * @param pitch the playback pitch to apply
   * @param sounds the candidate sounds to choose from
   * @return the id of the sound instance if playback started, or -1 if it failed to play
   */
  public static long random(float pitch, ISound... sounds) {
    int index = (int) (Math.random() * sounds.length);
    return play(sounds[index], pitch);
  }

  /**
   * Play this sound with its default volume.
   *
   * @param sound the sound to play
   * @return an Optional holding the play handle if playback started
   */
  public static long play(ISound sound) {
    return play(sound, 1);
  }

  /**
   * Play this sound with a custom pitch.
   *
   * @param sound the sound to play
   * @param pitch the playback pitch to apply
   * @return an Optional holding the play handle if playback started
   */
  public static long play(ISound sound, float pitch) {
    return play(sound, pitch, 1);
  }

  /**
   * Play this sound with a custom pitch and volume.
   *
   * @param sound the sound to play
   * @param pitch the playback pitch to apply
   * @param volumeModifier the playback volume modifier to apply
   * @return the id of the sound instance if playback started, or -1 if it failed to play
   */
  public static long play(ISound sound, float pitch, float volumeModifier) {
    float volume = getEffectsVolume() * sound.volume() * volumeModifier;
    return Game.audio()
        .playGlobal(new SoundSpec.Builder(sound.soundName()).volume(volume).pitch(pitch));
  }
}
