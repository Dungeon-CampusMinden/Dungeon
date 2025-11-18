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

  private final List<AbstractPlayHandle> activeHandles = new ArrayList<>();

  /**
   * No-op play implementation used in headless or test environments.
   *
   * <p>This method does not produce audio. Instead, it:
   *
   * <ul>
   *   <li>Logs the play request at TRACE level.
   *   <li>Returns a mock {@code IPlayHandle} that performs no audio work.
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
   * @return an {@code Optional} containing a mock {@code IPlayHandle} that does nothing
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

  /**
   * Does nothing, as there are no sounds to update.
   *
   * @param delta time delta (ignored)
   */
  @Override
  public void update(float delta) {
    // No-op
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

  private static class MockPlayHandle extends AbstractPlayHandle {

    public MockPlayHandle(long instanceId, Runnable onFinished) {
      super(instanceId);
      this.onFinished(onFinished);
      callFinished(); // Immediately trigger finished callback
    }

    @Override
    void update(float delta) {}

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
