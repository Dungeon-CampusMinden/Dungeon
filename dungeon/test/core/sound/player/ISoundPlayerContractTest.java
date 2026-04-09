package core.sound.player;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/** Backend-neutral contract tests for {@link ISoundPlayer} and {@link PlayHandle}. */
public class ISoundPlayerContractTest {

  private static final float EPSILON = 0.0001f;

  @Test
  void testPlayWithInstanceReturnsHandleAndStoresIt() {
    RecordingSoundPlayer player = new RecordingSoundPlayer();

    Optional<PlayHandle> handle =
      player.playWithInstance(1L, "test", 0.7f, false, 1.0f, 0.0f, null);

    assertTrue(handle.isPresent());
    assertEquals(1L, handle.orElseThrow().instanceId());
    assertTrue(player.get(1L).isPresent());
  }

  @Test
  void testUpdateSoundAppliesOnlyProvidedValues() {
    RecordingSoundPlayer player = new RecordingSoundPlayer();

    RecordingPlayHandle handle =
      (RecordingPlayHandle)
        player.playWithInstance(2L, "test", 0.7f, false, 1.25f, 0.0f, null).orElseThrow();

    ISoundPlayer.SoundUpdate update =
      ISoundPlayer.SoundUpdate.builder().paused(true).looping(true).build();

    assertTrue(player.updateSound(2L, update));

    assertEquals(0.7f, handle.lastVolume, EPSILON);
    assertEquals(1.25f, handle.lastPitch, EPSILON);
    assertTrue(handle.paused);
    assertTrue(handle.looping);
  }

  @Test
  void testUpdateSoundCanApplyPanAndVolumeTogether() {
    RecordingSoundPlayer player = new RecordingSoundPlayer();

    RecordingPlayHandle handle =
      (RecordingPlayHandle)
        player.playWithInstance(3L, "test", 0.8f, false, 1.0f, 0.0f, null).orElseThrow();

    ISoundPlayer.SoundUpdate update =
      ISoundPlayer.SoundUpdate.builder().pan(-0.5f, 0.4f).build();

    assertTrue(player.updateSound(3L, update));

    assertEquals(-0.5f, handle.lastPan, EPSILON);
    assertEquals(0.4f, handle.lastVolume, EPSILON);
  }

  @Test
  void testStopByInstanceStopsHandleAndRemovesIt() {
    RecordingSoundPlayer player = new RecordingSoundPlayer();
    AtomicInteger callbackCalls = new AtomicInteger();

    player.playWithInstance(4L, "test", 0.6f, false, 1.0f, 0.0f, callbackCalls::incrementAndGet);

    assertTrue(player.stopByInstance(4L));
    assertEquals(1, callbackCalls.get());
    assertTrue(player.get(4L).isEmpty());
  }

  @Test
  void testUpdateRemovesFinishedHandles() {
    RecordingSoundPlayer player = new RecordingSoundPlayer();

    RecordingPlayHandle handle =
      (RecordingPlayHandle)
        player.playWithInstance(5L, "test", 0.5f, false, 1.0f, 0.0f, null).orElseThrow();

    handle.stop();
    player.update(0.016f);

    assertTrue(player.get(5L).isEmpty());
  }

  @Test
  void testPlayHandleCallsOnFinishedWhenStopped() {
    RecordingPlayHandle handle = new RecordingPlayHandle(6L);
    AtomicInteger callbackCalls = new AtomicInteger();

    handle.onFinished(callbackCalls::incrementAndGet);
    handle.stop();

    assertTrue(handle.isFinished());
    assertEquals(1, callbackCalls.get());
  }

  @Test
  void testPlayHandleCallsLateOnFinishedImmediatelyWhenAlreadyFinished() {
    RecordingPlayHandle handle = new RecordingPlayHandle(7L);
    AtomicInteger callbackCalls = new AtomicInteger();

    handle.stop();
    handle.onFinished(callbackCalls::incrementAndGet);

    assertTrue(handle.isFinished());
    assertEquals(1, callbackCalls.get());
  }

  @Test
  void testUnknownInstanceOperationsReturnFalseOrEmpty() {
    RecordingSoundPlayer player = new RecordingSoundPlayer();

    assertTrue(player.get(999L).isEmpty());
    assertFalse(
      player.updateSound(
        999L, ISoundPlayer.SoundUpdate.builder().volume(0.3f).build()));
    assertFalse(player.stopByInstance(999L));
  }

  /**
   * Small backend-neutral test double for {@link ISoundPlayer}.
   *
   * <p>This is intentionally not a real engine backend. It exists only to validate the shared audio
   * contract in the core test path.
   */
  private static final class RecordingSoundPlayer implements ISoundPlayer {

    private final Map<Long, RecordingPlayHandle> activeHandles = new HashMap<>();

    @Override
    public Optional<PlayHandle> playWithInstance(
      long instanceId,
      String soundName,
      float volume,
      boolean looping,
      float pitch,
      float pan,
      Runnable onFinished) {

      if (soundName == null || soundName.isBlank()) {
        return Optional.empty();
      }

      RecordingPlayHandle handle = new RecordingPlayHandle(instanceId);
      handle.volume(volume);
      handle.pitch(pitch);
      handle.looping(looping);

      if (pan != 0.0f) {
        handle.pan(pan, volume);
      }

      if (onFinished != null) {
        handle.onFinished(onFinished);
      }

      activeHandles.put(instanceId, handle);
      return Optional.of(handle);
    }

    @Override
    public boolean updateSound(long instanceId, SoundUpdate update) {
      RecordingPlayHandle handle = activeHandles.get(instanceId);
      if (handle == null) {
        return false;
      }

      update.applyTo(handle);
      return true;
    }

    @Override
    public Optional<PlayHandle> get(long instanceId) {
      return Optional.ofNullable(activeHandles.get(instanceId));
    }

    @Override
    public boolean stopByInstance(long instanceId) {
      RecordingPlayHandle handle = activeHandles.remove(instanceId);
      if (handle == null) {
        return false;
      }

      handle.stop();
      return true;
    }

    @Override
    public void update(float delta) {
      activeHandles.entrySet().removeIf(
        entry -> {
          entry.getValue().update(delta);
          return entry.getValue().isFinished();
        });
    }

    @Override
    public void stopAll() {
      activeHandles.values().forEach(PlayHandle::stop);
      activeHandles.clear();
    }

    @Override
    public void dispose() {
      stopAll();
    }
  }

  /**
   * Backend-neutral test handle that records applied state changes.
   *
   * <p>Used to verify the shared {@link PlayHandle} contract without pulling in libGDX or
   * LITIENGINE code.
   */
  private static final class RecordingPlayHandle extends PlayHandle {

    private float lastVolume = 1.0f;
    private float lastPan = 0.0f;
    private float lastPitch = 1.0f;
    private boolean paused = false;
    private boolean looping = false;
    private boolean playing = true;

    private RecordingPlayHandle(long instanceId) {
      super(instanceId);
    }

    @Override
    public void stop() {
      if (isFinished()) {
        return;
      }

      this.playing = false;
      this.paused = false;
      callFinished();
    }

    @Override
    public void pause() {
      this.paused = true;
      this.playing = false;
    }

    @Override
    public void resume() {
      if (isFinished()) {
        return;
      }

      this.paused = false;
      this.playing = true;
    }

    @Override
    public void volume(float volume) {
      this.lastVolume = volume;
    }

    @Override
    public void pan(float pan, float volume) {
      this.lastPan = pan;
      this.lastVolume = volume;
    }

    @Override
    public void pitch(float pitch) {
      this.lastPitch = pitch;
    }

    @Override
    public boolean isPlaying() {
      return this.playing && !this.paused && !isFinished();
    }

    @Override
    public void looping(boolean looping) {
      this.looping = looping;
    }

    @Override
    public void update(float delta) {
      // No automatic completion in this contract test handle.
      // Lifecycle transitions are triggered explicitly by the tests.
    }
  }
}
