package core.components;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Tests for the {@link SoundComponent} class. */
public class SoundComponentTest {

  @Test
  void testSoundComponentCreation() {
    String soundId = "fireball";
    float baseVolume = 0.9f;
    boolean looping = false;
    float maxDistance = 15f;
    float attenuationFactor = 0.05f;
    Runnable onFinish = () -> {};

    SoundComponent component =
        new SoundComponent(soundId, baseVolume, looping, maxDistance, attenuationFactor, onFinish);

    assertEquals(soundId, component.soundId());
    assertEquals(baseVolume, component.baseVolume());
    assertEquals(looping, component.looping());
    assertEquals(maxDistance, component.maxDistance());
    assertEquals(attenuationFactor, component.attenuationFactor());
    assertEquals(onFinish, component.onFinish());
  }

  @Test
  void testSoundComponentWithLooping() {
    SoundComponent component = new SoundComponent("bgm", 0.5f, true, 50f, 0.1f, () -> {});

    assertTrue(component.looping());
    assertEquals("bgm", component.soundId());
  }
}
