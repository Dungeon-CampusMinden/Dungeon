package core.network.messages.s2c;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.sound.SoundSpec;
import org.junit.jupiter.api.Test;

class SoundPlayMessageTest {
  @Test
  void storesFields() {
    SoundSpec spec =
        SoundSpec.builder("boom")
            .instanceId(10L)
            .volume(0.6f)
            .pitch(1.1f)
            .pan(-0.2f)
            .looping(true)
            .maxDistance(30f)
            .attenuation(0.3f)
            .build();
    SoundPlayMessage msg = new SoundPlayMessage(42, spec);

    assertEquals(42, msg.entityId());
    assertEquals(10L, msg.soundSpec().instanceId());
    assertEquals("boom", msg.soundSpec().soundName());
    assertEquals(0.6f, msg.soundSpec().baseVolume(), 0.0001f);
    assertEquals(1.1f, msg.soundSpec().pitch(), 0.0001f);
    assertEquals(-0.2f, msg.soundSpec().pan(), 0.0001f);
    assertTrue(msg.soundSpec().looping());
    assertEquals(30f, msg.soundSpec().maxDistance(), 0.0001f);
    assertEquals(0.3f, msg.soundSpec().attenuationFactor(), 0.0001f);
  }
}
