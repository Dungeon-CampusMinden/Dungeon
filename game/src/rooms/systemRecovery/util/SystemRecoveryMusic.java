package rooms.systemRecovery.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import engine.Game;
import engine.utils.settings.ClientSettings;

/** Owns the client-local System Recovery background track and its synchronized alarm layer. */
public final class SystemRecoveryMusic {
  private static final String BACKGROUND_MUSIC = "sounds/system_recovery_bgm.wav";
  private static final String ALARM_LAYER_MUSIC = "sounds/system_recovery_alarm_layer.wav";
  private static final String ALARM_SIREN_MUSIC = "sounds/system_recovery_alarm_siren.wav";
  private static final float BACKGROUND_MUSIC_GAIN = 2.5f;
  private static final float ALARM_LAYER_MIX = 0.38f;
  private static final float ALARM_SIREN_MIX = 0.62f;

  private static Music backgroundMusic;
  private static Music alarmLayerMusic;
  private static Music alarmSirenMusic;
  private static boolean alarmActive;

  private SystemRecoveryMusic() {}

  /** Starts the normal music track and follows the user's music and master volume settings. */
  public static synchronized void setup() {
    if (backgroundMusic != null) {
      backgroundMusic.stop();
      backgroundMusic.dispose();
    }
    if (alarmLayerMusic != null) {
      alarmLayerMusic.stop();
      alarmLayerMusic.dispose();
      alarmLayerMusic = null;
    }
    if (alarmSirenMusic != null) {
      alarmSirenMusic.stop();
      alarmSirenMusic.dispose();
      alarmSirenMusic = null;
    }

    backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    updateVolumes();
    backgroundMusic.play();
    if (alarmActive) syncAlarmLayer(true);

    ClientSettings.setOnVolumeChange((key, value) -> updateVolumes());
  }

  /**
   * Starts or stops the alarm music layer and siren using the authoritative alarm state.
   *
   * @param active whether the system-core alarm is active
   */
  public static synchronized void syncAlarmLayer(boolean active) {
    alarmActive = active;
    if (!mayPlayLocalMusic()) return;
    if (!active) {
      if (alarmLayerMusic != null && alarmLayerMusic.isPlaying()) alarmLayerMusic.stop();
      if (alarmSirenMusic != null && alarmSirenMusic.isPlaying()) alarmSirenMusic.stop();
      return;
    }

    if (alarmLayerMusic == null) {
      alarmLayerMusic = Gdx.audio.newMusic(Gdx.files.internal(ALARM_LAYER_MUSIC));
      alarmLayerMusic.setLooping(true);
    }
    alarmLayerMusic.setVolume(alarmLayerVolume());
    if (!alarmLayerMusic.isPlaying()) alarmLayerMusic.play();

    if (alarmSirenMusic == null) {
      alarmSirenMusic = Gdx.audio.newMusic(Gdx.files.internal(ALARM_SIREN_MUSIC));
      alarmSirenMusic.setLooping(true);
    }
    alarmSirenMusic.setVolume(alarmSirenVolume());
    if (!alarmSirenMusic.isPlaying()) alarmSirenMusic.play();
  }

  /** Stops client-local alarm audio before disconnect or reconnect. */
  public static synchronized void resetClientAudio() {
    if (alarmLayerMusic != null && alarmLayerMusic.isPlaying()) alarmLayerMusic.stop();
    if (alarmSirenMusic != null && alarmSirenMusic.isPlaying()) alarmSirenMusic.stop();
  }

  private static synchronized void updateVolumes() {
    float userVolume = ClientSettings.musicVolume() / 100f * ClientSettings.masterVolume() / 100f;
    if (backgroundMusic != null) {
      backgroundMusic.setVolume(Math.min(1f, userVolume * BACKGROUND_MUSIC_GAIN));
    }
    if (alarmLayerMusic != null) {
      alarmLayerMusic.setVolume(alarmLayerVolume());
    }
    if (alarmSirenMusic != null) {
      alarmSirenMusic.setVolume(alarmSirenVolume());
    }
  }

  private static float alarmLayerVolume() {
    return Math.min(1f, backgroundMusicVolume() * ALARM_LAYER_MIX);
  }

  private static float alarmSirenVolume() {
    return Math.min(1f, backgroundMusicVolume() * ALARM_SIREN_MIX);
  }

  private static float backgroundMusicVolume() {
    float userVolume = ClientSettings.musicVolume() / 100f * ClientSettings.masterVolume() / 100f;
    return Math.min(1f, userVolume * BACKGROUND_MUSIC_GAIN);
  }

  private static boolean mayPlayLocalMusic() {
    return !Game.isHeadless();
  }
}
