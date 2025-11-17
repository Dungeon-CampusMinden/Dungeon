package core.sound.player;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.backends.lwjgl3.audio.Wav;
import com.badlogic.gdx.files.FileHandle;
import core.sound.*;
import core.sound.parser.IAudioParser;
import core.sound.parser.WavAudioParser;
import core.utils.logging.DungeonLogger;
import java.util.*;

/**
 * Gdx implementation of {@link ISoundPlayer} using libGDX audio APIs. Loads and manages sound
 * assets from the filesystem, supports playback with handles for control. Scans WAV files in
 * "dungeon/assets/sounds" directory, parses metadata, and uses AssetManager for loading. Suitable
 * for client-side audio; for server, use {@link NoSoundPlayer}.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * AssetManager assets = new AssetManager();
 * GdxSoundPlayer player = new GdxSoundPlayer(assets);
 * Optional<IPlayHandle> handle = player.play("footstep", 0.7f, false);
 * // Game loop
 * player.update(Gdx.graphics.getDeltaTime());
 * player.dispose();
 * }</pre>
 *
 * @see ISoundPlayer
 * @see NoSoundPlayer
 * @see SoundAsset
 */
public class GdxSoundPlayer implements ISoundPlayer {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(GdxSoundPlayer.class);
  private static final String ASSETS_DIR = "sounds";

  private final AssetManager assetManager;
  private final List<SoundAsset> assets = new ArrayList<>();
  private final Map<String, Sound> sounds = new HashMap<>();
  private final List<AbstractPlayHandle> activeHandles = new ArrayList<>();
  private final List<IAudioParser> parsers = List.of(new WavAudioParser());

  /**
   * Creates a GdxSoundPlayer with the given AssetManager. Scans sound assets from
   * "dungeon/assets/sounds" directory. Sounds are lazy loaded when first played.
   *
   * @param assetManager the AssetManager for loading audio files
   */
  public GdxSoundPlayer(AssetManager assetManager) {
    this.assetManager = assetManager;
    scanAssets();
  }

  private void scanAssets() {
    // On desktop, internal assets listing is not supported, so use internal_assets.txt
    if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
      FileHandle assetListHandle = Gdx.files.internal("internal_assets.txt");
      if (assetListHandle.exists()) {
        LOGGER.debug("Scanning sound assets from internal_assets.txt (desktop platform)");
        scanFromAssetsList(assetListHandle);
        return;
      } else {
        LOGGER.warn("internal_assets.txt not found on desktop, this may cause issues");
      }
    }

    // On non-desktop platforms or if internal_assets.txt is missing, use directory scanning
    LOGGER.debug("Scanning sound assets from {} directory", ASSETS_DIR);
    scanDirectory(Gdx.files.internal(ASSETS_DIR));
  }

  private void scanFromAssetsList(FileHandle assetListHandle) {
    String[] lines = assetListHandle.readString().split("\n");
    for (String line : lines) {
      String path = line.trim();
      // Only process files in the sounds/ directory
      if (path.startsWith(ASSETS_DIR + "/")) {
        FileHandle file = Gdx.files.internal(path);
        if (file.exists()) {
          processSoundFile(file);
        }
      }
    }
  }

  private void scanDirectory(FileHandle dir) {
    for (FileHandle file : dir.list()) {
      if (file.isDirectory()) {
        scanDirectory(file);
      } else {
        processSoundFile(file);
      }
    }
  }

  private void processSoundFile(FileHandle file) {
    String ext = file.extension().toLowerCase();
    parsers.stream()
        .filter(parser -> Arrays.asList(parser.supportedExtensions()).contains(ext))
        .findFirst()
        .ifPresentOrElse(
            parser -> {
              Optional<Long> durationMs = parser.parseDuration(file.readBytes());
              String id = file.nameWithoutExtension();
              LOGGER.debug(
                  "Found sound asset: {} at {} with duration {}",
                  id,
                  file.path(),
                  durationMs.orElse(-1L));
              if (durationMs.isEmpty()) {
                LOGGER.warn("Duration unknown for sound asset: {}", id);
              }
              assets.add(new SoundAsset(id, file.path(), durationMs));
            },
            () -> LOGGER.warn("No parser found for sound file: {}", file.path()));
  }

  /**
   * Plays a sound with a unique instance ID for tracking and updates.
   *
   * <p>Lazy-loads the sound asset if not already loaded, then creates a play handle registered in
   * the instance map. The handle allows later updates via {@link #update(long, SoundUpdate)}.
   *
   * @param instanceId unique identifier for this sound instance
   * @param soundName sound asset name (filename without extension)
   * @param volume initial volume (0.0=silent, 1.0=full; default: 0.5)
   * @param looping if true, sound loops indefinitely; if false, plays once (default: false)
   * @param pitch playback speed multiplier (1.0=normal; clamped to [0.5, 2.0] on non-desktop)
   * @param pan stereo position (-1.0=left, 0.0=center, 1.0=right; default: 0.0)
   * @param onFinished optional callback to run when playback finishes
   * @return handle for controlling the sound, or empty if asset not found
   * @throws IllegalArgumentException if volume not in [0.0, 1.0]
   */
  @Override
  public Optional<IPlayHandle> playWithInstance(
      long instanceId,
      String soundName,
      float volume,
      boolean looping,
      float pitch,
      float pan,
      Runnable onFinished) {
    if (volume < 0f || volume > 1f) {
      throw new IllegalArgumentException("Volume must be between 0.0 and 1.0");
    }
    SoundAsset asset =
        assets.stream().filter(a -> a.id().equals(soundName)).findFirst().orElse(null);
    if (asset == null) {
      LOGGER.warn("Sound asset not found: {}", soundName);
      return Optional.empty();
    }

    // Lazy load the sound if not already loaded
    if (!sounds.containsKey(soundName)) {
      LOGGER.debug("Lazy loading sound: {}", soundName);
      assetManager.load(asset.path(), Sound.class);
      assetManager.finishLoading();
      sounds.put(soundName, assetManager.get(asset.path(), Sound.class));
    }

    Sound sound = sounds.get(soundName);
    if (sound != null) {
      LOGGER.debug(
          "Playing sound: {} (instance={}) at volume {} looping {}",
          soundName,
          instanceId,
          volume,
          looping);
      SoundPlayHandle handle =
          new SoundPlayHandle(sound, asset.durationMs().orElse(-1L), volume, pitch, pan);
      handle.looping(looping);
      handle.onFinished(onFinished);
      activeHandles.add(handle);
      return Optional.of(handle);
    }
    LOGGER.error("Failed to play sound: {}", soundName);
    return Optional.empty();
  }

  @Override
  public boolean updateSound(long instanceId, SoundUpdate update) {
    return activeHandles.stream()
        .filter(handle -> handle.instanceId() == instanceId)
        .findFirst()
        .map(
            handle -> {
              update.applyTo(handle);
              return true;
            })
        .orElse(false);
  }

  @Override
  public Optional<IPlayHandle> get(long instanceId) {
    return activeHandles.stream()
        .filter(handle -> handle.instanceId() == instanceId)
        .findFirst()
        .map(handle -> handle);
  }

  @Override
  public boolean stopByInstance(long instanceId) {
    return activeHandles.removeIf(
        handle -> {
          if (handle.instanceId() == instanceId) {
            handle.stop();
            return true;
          }
          return false;
        });
  }

  @Override
  public void update(float delta) {
    activeHandles.removeIf(
        handle -> {
          handle.update(delta);
          return !handle.isPlaying() && handle.finished;
        });
  }

  @Override
  public void stopAll() {
    LOGGER.info("Stopping all {} active sounds", activeHandles.size());
    for (AbstractPlayHandle handle : activeHandles) {
      handle.stop();
    }
    activeHandles.clear();
  }

  @Override
  public void dispose() {
    LOGGER.info("Disposing sound player with {} active handles", activeHandles.size());
    for (AbstractPlayHandle handle : activeHandles) {
      handle.stop();
    }
    activeHandles.clear();
    assetManager.dispose();
    sounds.clear();
  }

  private static class SoundPlayHandle extends AbstractPlayHandle {
    private final long soundId;
    private final Sound sound;
    private final long durationMs;
    private long elapsedTime = 0;
    private boolean stopped = false;
    private boolean looping = false;

    /**
     * Creates a SoundPlayHandle for controlling playback of a libGDX Sound.
     *
     * <p>Note: On non-desktop platforms, pitch must be between 0.5 and 2.0, otherwise the given
     * value will be clamped to this range.
     *
     * @param sound The libGDX Sound instance
     * @param durationMs Duration of the sound in milliseconds, or -1 if unknown
     * @param volume Volume level (0.0 to 1.0)
     * @param pitch Pitch level (1.0 is normal)
     * @param pan Pan position (-1.0 left, 0.0 center, 1.0 right)
     * @throws IllegalArgumentException if any parameter is out of range
     */
    public SoundPlayHandle(Sound sound, long durationMs, float volume, float pitch, float pan) {
      if (volume < 0f || volume > 1f) {
        throw new IllegalArgumentException("Volume must be between 0.0 and 1.0");
      }
      if (Gdx.app != null && Gdx.app.getType() != Application.ApplicationType.Desktop) {
        LOGGER.info("Clamping pitch to [0.5, 2.0] on non-desktop platform (value: {})", pitch);
        pitch = Math.clamp(pitch, 0.5f, 2.0f);
      }
      if (pan < -1f || pan > 1f) {
        throw new IllegalArgumentException("Pan must be between -1.0 and 1.0");
      }

      this.sound = sound;
      this.durationMs = durationMs;
      this.soundId = sound.play(volume, pitch, pan);
    }

    @Override
    public void stop() {
      sound.stop(soundId);
      stopped = true;
      callFinished();
    }

    @Override
    public void pause() {
      sound.pause(soundId);
    }

    @Override
    public void resume() {
      sound.resume(soundId);
    }

    @Override
    public void volume(float volume) {
      sound.setVolume(soundId, volume);
    }

    /**
     * Sets the stereo pan and volume.
     *
     * <p>Note: Pan is not supported for stereo sounds in libGDX; this method will log a warning if
     * attempted on stereo audio, and will have no effect.
     *
     * @param pan the pan position (-1.0 left, 0.0 center, 1.0 right)
     * @param volume the volume level (0.0 to 1.0)
     * @throws IllegalArgumentException if pan is out of range
     */
    @Override
    public void pan(float pan, float volume) {
      if (pan < -1f || pan > 1f) {
        throw new IllegalArgumentException("Pan must be between -1.0 (left) and 1.0 (right)");
      }

      if (sound instanceof Wav.Sound wavSound) {
        if (wavSound.getChannels() == 2) {
          LOGGER.warn("Pan not supported for stereo sounds");
          return;
        }
      }
      sound.setPan(soundId, pan, volume);
    }

    /**
     * Sets the pitch of the sound.
     *
     * <p>Note: On non-desktop platforms, pitch must be between 0.5 and 2.0, otherwise the given
     * value will be clamped to this range.
     *
     * @param pitch the pitch level (1.0 is normal)
     */
    @Override
    public void pitch(float pitch) {
      if (Gdx.app != null && Gdx.app.getType() != Application.ApplicationType.Desktop) {
        LOGGER.info("Clamping pitch to [0.5, 2.0] on non-desktop platform (value: {})", pitch);
        pitch = Math.clamp(pitch, 0.5f, 2.0f);
      }

      sound.setPitch(soundId, pitch);
    }

    @Override
    public boolean isPlaying() {
      if (stopped) return false;
      if (looping) return true;
      if (durationMs != -1) {
        return elapsedTime < durationMs;
      }
      return true; // assume playing until stopped
    }

    @Override
    void update(float delta) {
      elapsedTime += (long) (delta * 1000);
      if (!stopped && !looping && !isPlaying()) {
        callFinished();
      }
    }

    @Override
    public void looping(boolean looping) {
      this.looping = looping;
      sound.setLooping(soundId, looping);
    }
  }
}
