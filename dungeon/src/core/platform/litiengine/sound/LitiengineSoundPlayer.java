package core.platform.litiengine.sound;

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

public final class LitiengineSoundPlayer implements ISoundPlayer {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(LitiengineSoundPlayer.class);

  private static final String[] SOUND_DIRS = {
    "dungeon/assets/sounds/",
    "sounds/",
    "assets/sounds/"
  };
  private static final String[] EXT = {".wav", ".ogg", ".mp3"};

  private final Map<Long, LitienginePlayHandle> active = new ConcurrentHashMap<>();

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
      LOGGER.warn("LITIENGINE audio subsystem not available yet (Game.audio() == null).");
      return Optional.empty();
    }

    // LITIENGINE currently doesn't support pitch/pan in the same way libGDX does.
    if (pitch != 1f) LOGGER.debug("Ignoring pitch={} (not supported by LITIENGINE SoundEngine)", pitch);
    if (pan != 0f) LOGGER.debug("Ignoring pan={} (not supported by LITIENGINE SoundEngine)", pan);

    String resolved = resolvePath(soundName);
    if (resolved == null) {
      LOGGER.warn("Sound '{}' could not be resolved (no matching resource found).", soundName);
      return Optional.empty();
    }

    try {
      Sound s = Resources.sounds().get(resolved);
      int maxDistance = Game.audio().getMaxDistance();
      SFXPlayback playback = Game.audio().playSound(s, looping, maxDistance, volume);

      LitienginePlayHandle handle = new LitienginePlayHandle(instanceId, s, playback, volume, looping);
      if (onFinished != null) handle.onFinished(onFinished);

      active.put(instanceId, handle);
      return Optional.of(handle);
    } catch (Exception e) {
      LOGGER.warn("Failed to play sound '{}' via LITIENGINE: {}", soundName, e.getMessage(), e);
      return Optional.empty();
    }
  }

  private String resolvePath(String soundName) {
    // allow already-resolved paths
    if (soundName.contains("/") && (soundName.endsWith(".wav") || soundName.endsWith(".ogg") || soundName.endsWith(".mp3"))) {
      return soundName;
    }

    for (String dir : SOUND_DIRS) {
      for (String ext : EXT) {
        String candidate = dir + soundName + ext;
        try {
          // Resources.getLocation returns null if not found. It's safe to probe.
          if (Resources.getLocation(candidate) != null) {
            return candidate;
          }
        } catch (Exception ignored) {
        }
      }
    }
    return null;
  }

  @Override
  public boolean updateSound(long instanceId, SoundUpdate update) {
    LitienginePlayHandle handle = active.get(instanceId);
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
    LitienginePlayHandle handle = active.remove(instanceId);
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

  private static final class LitienginePlayHandle extends PlayHandle {
    private final Sound sound;
    private SFXPlayback playback;
    private float volume;
    private boolean looping;

    LitienginePlayHandle(long id, Sound sound, SFXPlayback playback, float volume, boolean looping) {
      super(id);
      this.sound = sound;
      this.playback = playback;
      this.volume = volume;
      this.looping = looping;

      // hook finish + cancel
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
      // not supported in LITIENGINE in the same way -> ignore
    }

    @Override
    public void pitch(float pitch) {
      // not supported -> ignore
    }

    @Override
    public boolean isPlaying() {
      return playback != null && playback.isPlaying();
    }

    @Override
    public void looping(boolean looping) {
      // LITIENGINE can't toggle looping mid-play easily -> restart if changed
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
