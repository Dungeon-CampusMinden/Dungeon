package core.components;

import static org.junit.jupiter.api.Assertions.*;

import core.sound.SoundSpec;
import org.junit.jupiter.api.Test;

/** Tests for the {@link SoundComponent} class. */
public class SoundComponentTest {

  @Test
  void testSoundComponentCreation() {
    // Test default constructor
    SoundComponent component = new SoundComponent();
    assertNotNull(component);
    assertTrue(component.sounds().isEmpty());

    // Test constructor with initial sound spec
    SoundSpec spec = SoundSpec.builder("test_sound").instanceId(1L).volume(0.7f).build();

    SoundComponent componentWithSound = new SoundComponent(spec);
    assertNotNull(componentWithSound);
    assertEquals(1, componentWithSound.sounds().size());
    assertEquals(spec, componentWithSound.sounds().get(0));

    // Test constructor with null spec
    SoundComponent componentWithNull = new SoundComponent(null);
    assertNotNull(componentWithNull);
    assertTrue(componentWithNull.sounds().isEmpty());
  }

  @Test
  void testSoundComponentWithLooping() {
    SoundComponent component = new SoundComponent();

    // Add a looping sound
    SoundSpec loopingSpec =
        SoundSpec.builder("ambient_sound").instanceId(2L).looping(true).volume(0.5f).build();

    component.add(loopingSpec);

    assertEquals(1, component.sounds().size());
    assertTrue(component.sounds().get(0).looping);
    assertEquals("ambient_sound", component.sounds().get(0).soundName);
  }

  @Test
  void testAddSound() {
    SoundComponent component = new SoundComponent();

    // Add a sound
    SoundSpec spec1 = SoundSpec.builder("sound1").instanceId(3L).build();
    component.add(spec1);
    assertEquals(1, component.sounds().size());

    // Add another sound
    SoundSpec spec2 = SoundSpec.builder("sound2").instanceId(4L).build();
    component.add(spec2);
    assertEquals(2, component.sounds().size());

    // Try to add null (should be ignored)
    component.add(null);
    assertEquals(2, component.sounds().size());
  }

  @Test
  void testRemoveByInstance() {
    SoundComponent component = new SoundComponent();

    SoundSpec spec1 = SoundSpec.builder("sound1").instanceId(5L).build();
    SoundSpec spec2 = SoundSpec.builder("sound2").instanceId(6L).build();
    SoundSpec spec3 = SoundSpec.builder("sound3").instanceId(7L).build();

    component.add(spec1);
    component.add(spec2);
    component.add(spec3);
    assertEquals(3, component.sounds().size());

    // Remove by instance ID
    component.removeByInstance(6L);
    assertEquals(2, component.sounds().size());
    assertFalse(component.sounds().contains(spec2));
    assertTrue(component.sounds().contains(spec1));
    assertTrue(component.sounds().contains(spec3));

    // Remove non-existent instance (should have no effect)
    component.removeByInstance(999L);
    assertEquals(2, component.sounds().size());
  }

  @Test
  void testClear() {
    SoundComponent component = new SoundComponent();

    component.add(SoundSpec.builder("sound1").instanceId(8L).build());
    component.add(SoundSpec.builder("sound2").instanceId(9L).build());
    assertEquals(2, component.sounds().size());

    component.clear();
    assertTrue(component.sounds().isEmpty());
  }

  @Test
  void testReplaceAll() {
    SoundComponent component = new SoundComponent();

    // Add initial sounds
    component.add(SoundSpec.builder("sound1").instanceId(10L).build());
    component.add(SoundSpec.builder("sound2").instanceId(11L).build());
    assertEquals(2, component.sounds().size());

    // Replace with new list
    SoundSpec newSpec1 = SoundSpec.builder("new_sound1").instanceId(12L).build();
    SoundSpec newSpec2 = SoundSpec.builder("new_sound2").instanceId(13L).build();
    SoundSpec newSpec3 = SoundSpec.builder("new_sound3").instanceId(14L).build();

    component.replaceAll(java.util.List.of(newSpec1, newSpec2, newSpec3));
    assertEquals(3, component.sounds().size());
    assertTrue(component.sounds().contains(newSpec1));
    assertTrue(component.sounds().contains(newSpec2));
    assertTrue(component.sounds().contains(newSpec3));

    // Replace with null (should clear)
    component.replaceAll(null);
    assertTrue(component.sounds().isEmpty());

    // Replace with empty list
    component.add(SoundSpec.builder("temp").instanceId(15L).build());
    component.replaceAll(java.util.List.of());
    assertTrue(component.sounds().isEmpty());
  }

  @Test
  void testImmutableView() {
    SoundComponent component = new SoundComponent();
    SoundSpec spec = SoundSpec.builder("sound").instanceId(16L).build();
    component.add(spec);

    // Get the unmodifiable list
    var soundList = component.sounds();

    // Attempting to modify should throw UnsupportedOperationException
    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          soundList.add(SoundSpec.builder("illegal").instanceId(17L).build());
        });

    assertThrows(
        UnsupportedOperationException.class,
        () -> {
          soundList.remove(0);
        });

    assertThrows(UnsupportedOperationException.class, soundList::clear);
  }

  @Test
  void testSoundSpecProperties() {
    SoundComponent component = new SoundComponent();

    // Create a spec with various properties
    SoundSpec spec =
        SoundSpec.builder("complex_sound")
            .instanceId(18L)
            .volume(0.8f)
            .pitch(1.2f)
            .pan(-0.5f)
            .looping(true)
            .maxDistance(50f)
            .attenuation(0.8f)
            .build();

    component.add(spec);

    SoundSpec retrieved = component.sounds().get(0);
    assertEquals(18L, retrieved.instanceId);
    assertEquals("complex_sound", retrieved.soundName);
    assertEquals(0.8f, retrieved.baseVolume, 0.001f);
    assertEquals(1.2f, retrieved.pitch, 0.001f);
    assertEquals(-0.5f, retrieved.pan, 0.001f);
    assertTrue(retrieved.looping);
    assertEquals(50f, retrieved.maxDistance, 0.001f);
    assertEquals(0.8f, retrieved.attenuationFactor, 0.001f);
  }
}
