package core.network.messages.s2c;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SoundPlayMessageTest {
  @Test
  void storesFields() {
    SoundPlayMessage msg =
        new SoundPlayMessage(10L, 42, "boom", 0.6f, 1.1f, -0.2f, true, 30f, 0.3f);

    assertEquals(10L, msg.soundInstanceId());
    assertEquals(42, msg.entityId());
    assertEquals("boom", msg.soundName());
    assertEquals(0.6f, msg.volume(), 0.0001f);
    assertEquals(1.1f, msg.pitch(), 0.0001f);
    assertEquals(-0.2f, msg.pan(), 0.0001f);
    assertTrue(msg.looping());
    assertEquals(30f, msg.maxDistance(), 0.0001f);
    assertEquals(0.3f, msg.attenuationFactor(), 0.0001f);
  }
}
