package core.platform.client.audio;

import core.sound.player.ISoundPlayer;
import core.sound.player.PlayHandle;
import core.utils.logging.DungeonLogger;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.SFXPlayback;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.litiengine.sound.SoundEvent;
import de.gurkenlabs.litiengine.sound.SoundPlaybackListener;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side implementation of the sound player interface.
 *
 * <p>This class provides sound playback capabilities, managing active sound instances and
 * applying updates such as volume, looping, and pause/resume functionality. It wraps the
 * underlying audio engine and handles sound resource resolution from multiple possible
 * directories.
 *
 * <p>Note: Pan and pitch adjustments are not supported by the current audio engine and
 * will be logged as ignored when requested.
 */
public final class ClientSoundPlayer implements ISoundPlayer {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(ClientSoundPlayer.class);

  private static final String[] SOUND_DIRS = {
    "sounds/",
    "assets/sounds/",
    "dungeon/assets/sounds/"
  };
  private static final String[] EXT = {".wav", ".ogg", ".mp3"};

  private final Map<Long, ClientPlayHandle> active = new ConcurrentHashMap<>();

  @Override
  public Optional<PlayHandle> playWithInstance(
    long instanceId,
    String soundName,
    float volume,
    boolean looping,
    float pitch,
    float pan,
    Runnable onFinished) {

    if (volume < 0f || volume > 1f) throw new IllegalArgumentException("Volume must be in [0,1].");
    if (soundName == null || soundName.isBlank()) return Optional.empty();

    if (Game.audio() == null) {
      LOGGER.warn("Audio subsystem not available yet (Game.audio() == null).");
      return Optional.empty();
    }

    // Audio engine doesn't support pitch/pan adjustments.
    if (pitch != 1f) LOGGER.debug("Ignoring pitch={} (not supported by audio engine)", pitch);
    if (pan != 0f) LOGGER.debug("Ignoring pan={} (not supported by audio engine)", pan);

    String resolved = resolvePath(soundName);
    if (resolved == null) {
      LOGGER.warn("Sound '{}' could not be resolved (no matching resource found).", soundName);
      return Optional.empty();
    }

    try {
      Sound s = Resources.sounds().get(resolved);
      int maxDistance = Game.audio().getMaxDistance();
      SFXPlayback playback = Game.audio().playSound(s, looping, maxDistance, volume);

      if (playback == null) {
        LOGGER.warn("Failed to play sound '{}' (resolved='{}'): playback is null", soundName, resolved);
        return Optional.empty();
      }

      ClientPlayHandle handle = new ClientPlayHandle(instanceId, s, playback, volume, looping);
      if (onFinished != null) handle.onFinished(onFinished);

      active.put(instanceId, handle);
      return Optional.of(handle);
    } catch (Exception e) {
      LOGGER.warn("Failed to play sound '{}' via audio engine: {}", soundName, e.getMessage(), e);
      return Optional.empty();
    }
  }

  private String resolvePath(String soundName) {
    // already explicit path with extension
    String s = soundName.replace('\\', '/').trim();
    if (s.contains("/") && (s.endsWith(".wav") || s.endsWith(".ogg") || s.endsWith(".mp3"))) {
      return canOpenResource(s) ? s : null;
    }

    for (String dir : SOUND_DIRS) {
      for (String ext : EXT) {
        String candidate = dir + s + ext;
        if (canOpenResource(candidate)) return candidate;
      }
    }
    return null;
  }

  private static boolean canOpenResource(String candidate) {
    try {
      var url = Resources.getLocation(candidate);
      if (url == null) return false;
      try (var in = url.openStream()) {
        return in != null;
      }
    } catch (Exception ignored) {
      return false;
    }
  }

  @Override
  public boolean updateSound(long instanceId, SoundUpdate update) {
    ClientPlayHandle handle = active.get(instanceId);
    if (handle == null) return false;
    update.applyTo(handle);
    return true;
  }

  @Override
  public Optional<PlayHandle> get(long instanceId) {
    return Optional.ofNullable(active.get(instanceId));
  }

  @Override
  public boolean stopByInstance(long instanceId) {
    ClientPlayHandle handle = active.remove(instanceId);
    if (handle == null) return false;
    handle.stop();
    return true;
  }

  @Override
  public void update(float delta) {
    active.values().removeIf(h -> {
      h.update(delta);
      return h.isFinished() && !h.isPlaying();
    });
  }

  @Override
  public void stopAll() {
    for (PlayHandle h : active.values()) {
      h.stop();
    }
    active.clear();
  }

  @Override
  public void dispose() {
    stopAll();
  }

  /**
   * A client-side implementation of {@link PlayHandle} that manages the lifecycle and controls
   * of a sound playback instance.
   *
   * <p>This implementation works in conjunction with {@link Sound} and {@link SFXPlayback} to
   * control playback parameters such as volume and looping, as well as to handle events like
   * sound completion or cancellation.
   */
  private static final class ClientPlayHandle extends PlayHandle {
    private final Sound sound;
    private SFXPlayback playback;
    private float volume;
    private boolean looping;

    /**
     * Constructs a new client play handle for managing a sound playback instance.
     *
     * @param id the unique identifier for this play handle
     * @param sound the Sound object being played
     * @param playback the SFXPlayback instance managing the actual playback
     * @param volume the initial playback volume
     * @param looping whether the sound should loop
     */
    ClientPlayHandle(long id, Sound sound, SFXPlayback playback, float volume, boolean looping) {
      super(id);
      this.sound = sound;
      this.playback = playback;
      this.volume = volume;
      this.looping = looping;

      // Hook finish and cancel events
      this.playback.addSoundPlaybackListener(new SoundPlaybackListener() {
        @Override
        public void finished(SoundEvent event) {
          finishOnce();
        }

        @Override
        public void cancelled(SoundEvent event) {
          finishOnce();
        }
      });
    }

    private void finishOnce() {
      if (finished) return;
      callFinished();
    }

    @Override
    public void stop() {
      if (playback != null) playback.cancel();
      finishOnce();
    }

    @Override
    public void pause() {
      if (playback != null) playback.pausePlayback();
    }

    @Override
    public void resume() {
      if (playback != null) playback.resumePlayback();
    }

    @Override
    public void volume(float volume) {
      this.volume = volume;
      if (playback != null) playback.setVolume(volume);
    }

    @Override
    public void pan(float pan, float volume) {
      // not supported by the audio engine
    }

    @Override
    public void pitch(float pitch) {
      // not supported by the audio engine
    }

    @Override
    public boolean isPlaying() {
      return playback != null && playback.isPlaying();
    }

    @Override
    public void looping(boolean looping) {
      // Audio engine can't toggle looping mid-play easily, so restart if changed
      if (this.looping == looping) return;
      this.looping = looping;

      if (playback != null) playback.cancel();

      int maxDistance = Game.audio().getMaxDistance();
      this.playback = Game.audio().playSound(sound, looping, maxDistance, volume);
    }

    @Override
    public void update(float delta) {
      if (!finished && playback != null && !playback.isPlaying()) {
        finishOnce();
      }
    }
  }
}
