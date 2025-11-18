package core.sound.player;

import core.utils.logging.DungeonLogger;
import java.util.*;

/**
 * No-operation implementation of {@link ISoundPlayer} for environments without audio support.
 * Ignores all playback requests, suitable for headless servers or testing. Logs play requests for
 * debugging; can be extended to send network messages in multiplayer setups.
 *
 * <p>Ignores all playback requests and logs them at DEBUG level.
 *
 * <p>Useful for non-audio environments such as dedicated servers or junit tests.
 *
 * @see ISoundPlayer
 * @see GdxSoundPlayer
 */
public class NoSoundPlayer implements ISoundPlayer {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(NoSoundPlayer.class);

  private final List<PlayHandle> activeHandles = new ArrayList<>();

  /**
   * No-op play implementation used in headless or test environments.
   *
   * <p>This method does not produce audio. Instead, it:
   *
   * <ul>
   *   <li>Logs the play request at TRACE level.
   *   <li>Returns a mock {@code PlayHandle} that performs no audio work.
   *   <li>If an on-finished callback was registered via {@link
   *       core.sound.AudioApi#registerOnFinished(long, Runnable)}, that callback will be invoked
   *       immediately.
   * </ul>
   *
   * <p>No audio assets are loaded or validated here; the {@code soundName} is only retained for
   * logging.
   *
   * @param instanceId unique identifier for this sound instance (ignored)
   * @param soundName logical name of the sound asset (logged only)
   * @param volume initial playback volume (ignored)
   * @param looping whether playback should loop (ignored)
   * @param pitch playback pitch (ignored)
   * @param pan stereo pan (ignored)
   * @return an {@code Optional} containing a mock {@code PlayHandle} that does nothing
   */
  @Override
  public Optional<PlayHandle> playWithInstance(
      long instanceId,
      String soundName,
      float volume,
      boolean looping,
      float pitch,
      float pan,
      Runnable onFinished) {
    LOGGER.trace(
        "NoSoundPlayer: Ignoring playWithInstance for {} (instance={}) at volume {} looping {} pitch {} pan {}",
        soundName,
        instanceId,
        volume,
        looping,
        pitch,
        pan);

    MockPlayHandle handle = new MockPlayHandle(instanceId, onFinished);
    activeHandles.add(handle);
    return Optional.of(handle);
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
  public Optional<PlayHandle> get(long instanceId) {
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

  /**
   * Updates the sound player state, invoking updates on all active mock play handles and deleting
   * them immediately since they do not actually play sound.
   *
   * @param delta time delta (ignored)
   */
  @Override
  public void update(float delta) {
    activeHandles.removeIf(
        handle -> {
          handle.update(delta);
          return true;
        });
  }

  @Override
  public void stopAll() {
    LOGGER.debug("NoSoundPlayer: Ignoring stopAll request");
  }

  /** Logs disposal and performs no cleanup. */
  @Override
  public void dispose() {
    LOGGER.info("NoSoundPlayer: Disposing (no-op)");
  }

  private static class MockPlayHandle extends PlayHandle {

    public MockPlayHandle(long instanceId, Runnable onFinished) {
      super(instanceId);
      this.onFinished(onFinished);
    }

    @Override
    public void update(float delta) {
      callFinished(); // Immediately trigger finished callback upon first update
    }

    @Override
    public void stop() {}

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void volume(float volume) {}

    @Override
    public void pan(float pan, float volume) {}

    @Override
    public void pitch(float pitch) {}

    @Override
    public boolean isPlaying() {
      return false;
    }

    @Override
    public void looping(boolean looping) {}
  }
}
