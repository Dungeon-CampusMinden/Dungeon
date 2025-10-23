package core.sound;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Tests for the {@link SoundAsset} record. */
public class SoundAssetTest {

  @Test
  void testSoundAssetCreation() {
    String id = "test";
    String path = "sounds/test.wav";
    long durationMs = 1000L;

    SoundAsset asset = new SoundAsset(id, path, durationMs);

    assertEquals(id, asset.id());
    assertEquals(path, asset.path());
    assertTrue(asset.durationMs().isPresent());
    assertEquals(durationMs, asset.durationMs().get());
  }

  @Test
  void testSoundAssetWithEmptyDuration() {
    SoundAsset asset = new SoundAsset("test", "sounds/test.wav");

    assertEquals("test", asset.id());
    assertEquals("sounds/test.wav", asset.path());
    assertFalse(asset.durationMs().isPresent());
  }
}
