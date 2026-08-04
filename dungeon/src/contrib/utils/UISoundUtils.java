package contrib.utils;

import core.Game;
import core.game.PreRunConfiguration;
import core.sound.ISound;
import core.sound.SoundSpec;
import core.utils.settings.ClientSettings;
import java.util.Optional;

/** Utilities for short, non-positional UI sound effects. */
public final class UISoundUtils {

  private static final float DEFAULT_PITCH = 1f;
  private static final float DEFAULT_PAN = 0f;

  private UISoundUtils() {}

  /**
   * Plays a UI sound with its configured default volume.
   *
   * @param sound sound to play
   * @return sound instance id, or -1 if playback failed
   */
  public static long play(ISound sound) {
    return play(sound.soundName(), sound.volume(), DEFAULT_PITCH);
  }

  /**
   * Plays a UI sound directly on the local client, or sends it from a multiplayer server.
   *
   * @param soundName sound asset id
   * @param volume base volume
   * @param pitch playback pitch
   * @return sound instance id, or -1 if playback failed
   */
  public static long play(String soundName, float volume, float pitch) {
    float effectiveVolume =
        (ClientSettings.masterVolume() / 100f) * (ClientSettings.effectsVolume() / 100f) * volume;
    if (PreRunConfiguration.multiplayerEnabled() && PreRunConfiguration.isNetworkServer()) {
      return Game.audio()
          .playGlobal(new SoundSpec.Builder(soundName).volume(effectiveVolume).pitch(pitch));
    }
    long instanceId = Game.audio().newInstanceId();
    Optional<?> handle =
        Game.soundPlayer()
            .playWithInstance(
                instanceId, soundName, effectiveVolume, false, pitch, DEFAULT_PAN, null);
    return handle.isPresent() ? instanceId : -1;
  }
}
