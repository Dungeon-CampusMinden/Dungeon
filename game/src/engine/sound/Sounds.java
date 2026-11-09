package engine.sound;

import engine.Game;
import engine.utils.settings.ClientSettings;

/** Utility class for playing sounds with various options. */
public class Sounds {

  /**
   * Plays local UI feedback immediately, without entities or network messages.
   *
   * @param sound the sound to play
   * @return the allocated local sound instance ID
   */
  public static long playUi(ISound sound) {
    return playUi(sound, 1);
  }

  /**
   * Plays local UI feedback with a custom pitch.
   *
   * @param sound the sound to play
   * @param pitch playback pitch
   * @return the allocated local sound instance ID
   */
  public static long playUi(ISound sound, float pitch) {
    return playUi(sound, pitch, 1);
  }

  /**
   * Plays local UI feedback using the master and effects volume settings. Works before gameplay
   * starts; the application updates and disposes the shared player.
   *
   * @param sound the sound to play
   * @param pitch playback pitch
   * @param volumeModifier additional volume multiplier
   * @return the allocated local sound instance ID; stop it via {@link
   *     engine.sound.player.ISoundPlayer#stopByInstance(long)}
   */
  public static long playUi(ISound sound, float pitch, float volumeModifier) {
    long instanceId = Game.audio().newLocalInstanceId();
    float volume = effectsVolumeFactor() * sound.volume() * volumeModifier;
    Game.soundPlayer()
        .playWithInstance(instanceId, sound.soundName(), volume, false, pitch, 0, null);
    return instanceId;
  }

  /**
   * Returns the factor that scales sound effect volumes: master volume times effects volume.
   *
   * @return the product of master and effects volume, in [0.0, 1.0]
   */
  public static float effectsVolumeFactor() {
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
   * Play this sound with a custom pitch and volume. The volume settings are applied by {@link
   * engine.systems.SoundSystem} on each client.
   *
   * @param sound the sound to play
   * @param pitch the playback pitch to apply
   * @param volumeModifier the playback volume modifier to apply
   * @return the id of the sound instance if playback started, or -1 if it failed to play
   */
  public static long play(ISound sound, float pitch, float volumeModifier) {
    float volume = sound.volume() * volumeModifier;
    return Game.audio()
        .playGlobal(new SoundSpec.Builder(sound.soundName()).volume(volume).pitch(pitch));
  }
}
