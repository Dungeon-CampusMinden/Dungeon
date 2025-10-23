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
    when(mockSound.play(anyFloat())).thenReturn(1L); // Mock sound ID
    player = new GdxSoundPlayer(mockAssetManager);

    // Manually add test assets using reflection since scanning doesn't find files in test
    Field assetsField = GdxSoundPlayer.class.getDeclaredField("assets");
    assetsField.setAccessible(true);
    @SuppressWarnings("unchecked")
    List<SoundAsset> assets = (List<SoundAsset>) assetsField.get(player);
    assets.add(new SoundAsset("test", "sounds/test.wav", 1962L));
    assets.add(new SoundAsset("loop", "sounds/test.wav", 1962L));

    Field soundsField = GdxSoundPlayer.class.getDeclaredField("sounds");
    soundsField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<String, Sound> sounds = (Map<String, Sound>) soundsField.get(player);
    sounds.put("test", mockSound);
    sounds.put("loop", mockSound);
  }

  @Test
  void testPlayValidSound() {
    Optional<IPlayHandle> handle = player.play("test", 0.8f, false);
    assertTrue(handle.isPresent());
    verify(mockSound).play(0.8f);
  }

  @Test
  void testPlayInvalidSound() {
    Optional<IPlayHandle> handle = player.play("nonexistent", 0.8f, false);
    assertFalse(handle.isPresent());
  }

  @Test
  void testPlayLoopingSound() {
    Optional<IPlayHandle> handle = player.play("loop", 0.8f, true);
    assertTrue(handle.isPresent());
    verify(mockSound).play(0.8f);
    verify(mockSound).setLooping(1L, true);
  }

  @Test
  void testUpdate() {
    player.update(0.016f);
    // No assertions needed, just ensure no exceptions
  }

  @Test
  void testDispose() {
    player.dispose();
    verify(mockAssetManager).dispose();
  }
}
