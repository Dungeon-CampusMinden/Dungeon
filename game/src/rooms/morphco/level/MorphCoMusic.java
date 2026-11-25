package rooms.morphco.level;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.TimeUtils;
import engine.Game;
import engine.System;
import engine.utils.settings.ClientSettings;

/**
 * Plays the background music on this client once its hero carries the {@link IntroSeenComponent}.
 * The music fades in, loops and follows the music and master volume settings.
 *
 * <p>After the intro, the sound fades in over the time the intro takes to fade out, in the same
 * steps on server and client.
 */
public final class MorphCoMusic extends System {

  private static final String MUSIC = "rooms/morphCo/factory_music.ogg";
  // The settings start the music slider low; the gain makes the track about as loud as the machine
  // noises.
  private static final float MUSIC_GAIN = 4f;
  private static final long FADE_MS = 1500;
  private static final int FADE_STEPS = 15;

  private Music music;
  private long startedAt;
  private float fade;

  /** Creates the music system. */
  public MorphCoMusic() {
    super(AuthoritativeSide.CLIENT);
  }

  /**
   * Returns how far a fade-in has come.
   *
   * @param startedAt time the fade-in started, in {@link TimeUtils#millis()}
   * @return volume factor from 0 to 1, in steps
   */
  static float fadeIn(long startedAt) {
    int step = (int) ((TimeUtils.millis() - startedAt) * FADE_STEPS / FADE_MS);
    return Math.min(1f, (float) step / FADE_STEPS);
  }

  @Override
  public void execute() {
    if (music == null) {
      if (Game.player().filter(hero -> hero.isPresent(IntroSeenComponent.class)).isEmpty()) return;
      music = Gdx.audio.newMusic(Gdx.files.internal(MUSIC));
      music.setLooping(true);
      ClientSettings.addVolumeListener((key, value) -> updateVolume());
      startedAt = TimeUtils.millis();
      updateVolume();
      music.play();
    }
    if (fade < 1f) updateVolume();
  }

  private void updateVolume() {
    fade = fadeIn(startedAt);
    float volume = ClientSettings.musicVolume() / 100f * ClientSettings.masterVolume() / 100f;
    music.setVolume(Math.min(1f, volume * MUSIC_GAIN) * fade);
  }
}
