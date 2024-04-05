package contrib.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import core.utils.components.path.IPath;
import java.util.HashMap;
import java.util.Map;

/** Class for managing sounds in the game. */
public class SoundPlayer {
  private static final Map<String, Sound> SOUND_CACHE = new HashMap<>();

  private static Sound getSound(final IPath path) {
    if (!SOUND_CACHE.containsKey(path.pathString())) {
      SOUND_CACHE.put(path.pathString(), Gdx.audio.newSound(Gdx.files.internal(path.pathString())));
    }
    return SOUND_CACHE.get(path.pathString());
  }

  /**
   * Plays a sound.
   *
   * <p>The sound will be stopped and disposed if it was already playing. The sound can be looped.
   * The volume is 0.1.
   *
   * @param path the path to the sound.
   * @param loop if the sound should be looped.
   */
  public static void playSound(final IPath path, boolean loop) {
    playSound(path, loop, .1f);
  }

  /**
   * Plays a sound.
   *
   * <p>The sound will be stopped and disposed if it was already playing. The sound can be looped.
   * The volume can be adjusted.
   *
   * @param path the path to the sound.
   * @param loop if the sound should be looped.
   * @param volume the volume of the sound.
   */
  public static void playSound(final IPath path, boolean loop, float volume) {
    playSound(path, loop, volume, 1f, 1f);
  }

  /**
   * Plays a sound.
   *
   * <p>The sound will be stopped and disposed if it was already playing. The sound can be looped.
   * The volume and pitch range can be adjusted.
   *
   * @param path the path to the sound.
   * @param loop if the sound should be looped.
   * @param volume the volume of the sound.
   * @param pitchMin the minimum pitch of the sound.
   * @param pitchMax the maximum pitch of the sound.
   */
  public static void playSound(
      final IPath path, boolean loop, float volume, float pitchMin, float pitchMax) {
    Sound sound = getSound(path);
    long id = sound.play();

    sound.setLooping(id, loop);
    sound.setVolume(id, volume);
    float randomPitch = MathUtils.random(pitchMin, pitchMax);
    sound.setPitch(id, randomPitch);
  }
}
