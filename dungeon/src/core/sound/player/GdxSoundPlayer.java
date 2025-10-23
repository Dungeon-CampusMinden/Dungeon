package core.sound.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.TimeUtils;
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

  private final AssetManager assetManager;
  private final List<SoundAsset> assets = new ArrayList<>();
  private final Map<String, Sound> sounds = new HashMap<>();
  private final List<AbstractPlayHandle> activeHandles = new ArrayList<>();
  private final List<IAudioParser> parsers = List.of(new WavAudioParser());

  /**
   * Creates a GdxSoundPlayer with the given AssetManager. Scans and loads sound assets from
   * "dungeon/assets/sounds" directory.
   *
   * @param assetManager the AssetManager for loading audio files
   */
  public GdxSoundPlayer(AssetManager assetManager) {
    this.assetManager = assetManager;
    scanAssets();
    loadAssets();
  }

  private void scanAssets() { //TODO: fix path trickery
    FileHandle assetsHandle = Gdx.files.internal("dungeon/assets/sounds");
    LOGGER.info("Scanning sound assets from: {}", assetsHandle.path());
    scanDirectory(assetsHandle);
  }

  private void scanDirectory(FileHandle dir) {
    for (FileHandle file : dir.list()) {
      if (file.isDirectory()) {
        scanDirectory(file);
      } else {
        String ext = file.extension().toLowerCase();
        parsers.stream()
            .filter(parser -> Arrays.asList(parser.supportedExtensions()).contains(ext))
            .findFirst()
            .ifPresentOrElse(
                parser -> {
                  String id = file.nameWithoutExtension();
                  Optional<Long> durationMs = parser.parseDuration(file.file().toPath());
                  LOGGER.debug(
                      "Found sound asset: {} at {} with duration {}",
                      id,
                      file.path(),
                      durationMs.orElse(-1L));
                  assets.add(new SoundAsset(id, file.path(), durationMs));
                },
                () -> LOGGER.warn("No parser found for sound file: {}", file.path()));
      }
    }
  }

  private void loadAssets() {
    LOGGER.info("Loading {} sound assets", assets.size());
    for (SoundAsset asset : assets) {
      assetManager.load(asset.path(), Sound.class);
      sounds.put(asset.id(), null);
    }
    assetManager.finishLoading();
    for (SoundAsset asset : assets) {
      sounds.put(asset.id(), assetManager.get(asset.path(), Sound.class));
    }
    LOGGER.info("Sound assets loaded successfully");
  }

  @Override
  public Optional<IPlayHandle> play(String id, float volume, boolean looping) {
    SoundAsset asset = assets.stream().filter(a -> a.id().equals(id)).findFirst().orElse(null);
    if (asset == null) {
      LOGGER.warn("Sound asset not found: {}", id);
      return Optional.empty();
    }
    Sound sound = sounds.get(id);
    if (sound != null) {
      LOGGER.debug("Playing sound: {} at volume {} looping {}", id, volume, looping);
      SoundPlayHandle handle = new SoundPlayHandle(sound, asset.durationMs().orElse(-1L), volume);
      handle.setLooping(looping);
      activeHandles.add(handle);
      return Optional.of(handle);
    }
    LOGGER.warn("Sound not loaded: {}", id);
    return Optional.empty();
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
  public void dispose() {
    LOGGER.info("Disposing sound player with {} active handles", activeHandles.size());
    for (AbstractPlayHandle handle : activeHandles) {
      handle.stop();
    }
    activeHandles.clear();
    assetManager.dispose();
    sounds.clear();
  }

  private abstract static class AbstractPlayHandle implements IPlayHandle {
    protected boolean finished = false;
    protected Runnable onFinishedCallback;

    @Override
    public void onFinished(Runnable callback) {
      this.onFinishedCallback = callback;
    }

    protected void callFinished() {
      if (onFinishedCallback != null) {
        onFinishedCallback.run();
      }
      finished = true;
    }

    abstract void update(float delta);
  }

  private static class SoundPlayHandle extends AbstractPlayHandle {
    private final long soundId;
    private final Sound sound;
    private final long durationMs;
    private final long startTime;
    private boolean stopped = false;
    private boolean looping = false;

    public SoundPlayHandle(Sound sound, long durationMs, float volume) {
      this.sound = sound;
      this.durationMs = durationMs;
      this.soundId = sound.play(volume);
      this.startTime = TimeUtils.nanoTime();
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

    @Override
    public void pan(float pan, float volume) {
      sound.setPan(soundId, pan, volume);
    }

    @Override
    public boolean isPlaying() {
      if (stopped) return false;
      if (looping) return true;
      if (durationMs != -1) {
        long elapsed = TimeUtils.nanoTime() - startTime;
        return elapsed < durationMs * 1_000_000L;
      }
      return true; // assume playing until stopped
    }

    @Override
    void update(float delta) {
      if (!stopped && !looping && !isPlaying()) {
        callFinished();
      }
    }

    @Override
    public void setLooping(boolean looping) {
      this.looping = looping;
      sound.setLooping(soundId, looping);
    }
  }
}
