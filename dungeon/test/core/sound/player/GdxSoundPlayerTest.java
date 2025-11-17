package core.sound.player;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import core.sound.SoundAsset;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests for the {@link GdxSoundPlayer} class. */
public class GdxSoundPlayerTest {

  private AssetManager mockAssetManager;
  private Sound mockSound;
  private GdxSoundPlayer player;

  @BeforeEach
  void setup() throws Exception {
    mockAssetManager = mock(AssetManager.class);
    mockSound = mock(Sound.class);
    when(mockAssetManager.isLoaded(anyString())).thenReturn(true);
    when(mockAssetManager.get(anyString(), eq(Sound.class))).thenReturn(mockSound);
    when(mockSound.play(anyFloat(), anyFloat(), anyFloat())).thenReturn(1L); // Mock sound ID

    player = spy(new GdxSoundPlayer(mockAssetManager));

    // Manually add test assets using reflection since scanning doesn't find files in test
    setupAssets();
  }

  private void setupAssets() throws NoSuchFieldException, IllegalAccessException {
    Field assetsField = GdxSoundPlayer.class.getDeclaredField("assets");
    assetsField.setAccessible(true);
    @SuppressWarnings("unchecked")
    List<SoundAsset> assets = (List<SoundAsset>) assetsField.get(player);
    assets.add(new SoundAsset("test", "sounds/test.wav", 1962L));
    assets.add(new SoundAsset("loop", "sounds/loop.wav", 3000L));

    Field soundsField = GdxSoundPlayer.class.getDeclaredField("sounds");
    soundsField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Sound> sounds = (Map<String, Sound>) soundsField.get(player);
    sounds.put("test", mockSound);
    sounds.put("loop", mockSound);
  }

  @Test
  void testPlayValidSound() {
    // Play a sound with valid parameters
    Optional<IPlayHandle> handle =
        player.playWithInstance(1L, "test", 0.7f, false, 1.0f, 0.0f, null);

    // Verify the handle is present and the sound was played
    assertTrue(handle.isPresent());
    assertTrue(handle.get().isPlaying());
    verify(mockSound).play(0.7f, 1.0f, 0.0f);
  }

  @Test
  void testPlayNonExistentSound() {
    // Try to play a sound that doesn't exist
    Optional<IPlayHandle> handle =
        player.playWithInstance(2L, "nonexistent", 0.5f, false, 1.0f, 0.0f, null);

    // Verify no handle is returned
    assertFalse(handle.isPresent());
    // Verify sound.play() was never called
    verify(mockSound, never()).play(anyFloat(), anyFloat(), anyFloat());
  }

  @Test
  void testPlayLoopingSound() {
    // Play a sound with looping enabled
    Optional<IPlayHandle> handle =
        player.playWithInstance(3L, "loop", 0.8f, true, 1.0f, 0.0f, null);

    // Verify the handle is present and playing
    assertTrue(handle.isPresent());
    assertTrue(handle.get().isPlaying());

    // Verify sound.play() was called
    verify(mockSound).play(0.8f, 1.0f, 0.0f);
    // Verify looping was set to true
    verify(mockSound).setLooping(1L, true);
  }

  @Test
  void testUpdateFinishesSound() throws Exception {
    // Create a callback to verify it gets called
    Runnable callback = mock(Runnable.class);

    // Play a non-looping sound with known duration (1962ms for "test" sound)
    Optional<IPlayHandle> handle =
        player.playWithInstance(4L, "test", 0.5f, false, 1.0f, 0.0f, callback);

    assertTrue(handle.isPresent());
    assertTrue(handle.get().isPlaying());

    // Access activeHandles to verify cleanup
    Field activeHandlesField = GdxSoundPlayer.class.getDeclaredField("activeHandles");
    activeHandlesField.setAccessible(true);
    @SuppressWarnings("unchecked")
    List<AbstractPlayHandle> activeHandles =
        (List<AbstractPlayHandle>) activeHandlesField.get(player);

    assertEquals(1, activeHandles.size());

    // Simulate time passing beyond the sound duration (1962ms + 100ms buffer = 2.1 seconds)
    player.update(2.1f);

    // Verify the callback was called
    verify(callback).run();

    // Verify the handle was removed from active handles
    assertEquals(0, activeHandles.size());
  }

  @Test
  void testUpdatePreservesLoopingSounds() throws Exception {
    // Play a looping sound
    Optional<IPlayHandle> handle =
        player.playWithInstance(5L, "loop", 0.6f, true, 1.0f, 0.0f, null);

    assertTrue(handle.isPresent());
    assertTrue(handle.get().isPlaying());

    // Access activeHandles to verify it stays active
    Field activeHandlesField = GdxSoundPlayer.class.getDeclaredField("activeHandles");
    activeHandlesField.setAccessible(true);
    @SuppressWarnings("unchecked")
    List<AbstractPlayHandle> activeHandles =
        (List<AbstractPlayHandle>) activeHandlesField.get(player);

    assertEquals(1, activeHandles.size());

    // Update multiple times with significant time passage
    player.update(5.0f);
    player.update(5.0f);
    player.update(5.0f);

    // Verify the looping sound is still active
    assertEquals(1, activeHandles.size());
    assertTrue(handle.get().isPlaying());
  }

  @Test
  void testDispose() {
    player.dispose();
    verify(mockAssetManager).dispose();
  }
}
