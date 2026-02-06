package core.sound;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class SoundSpecTest {
  @Test
  void targetsDefaultToEmpty() {
    SoundSpec spec = SoundSpec.builder("ping").build();
    assertArrayEquals(new int[0], spec.targetEntityIds());
  }

  @Test
  void targetsDefensivelyCopied() {
    int[] ids = {1, 2};
    SoundSpec spec = SoundSpec.builder("ping").targets(ids).build();

    ids[0] = 99;
    assertArrayEquals(new int[] {1, 2}, spec.targetEntityIds());

    int[] returned = spec.targetEntityIds();
    returned[1] = 77;
    assertArrayEquals(new int[] {1, 2}, spec.targetEntityIds());
  }

  @Test
  void targetsNullBecomesEmpty() {
    SoundSpec spec = SoundSpec.builder("ping").targets((int[]) null).build();
    assertArrayEquals(new int[0], spec.targetEntityIds());
  }
}
