package core.network.messages.s2c;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SoundStopMessageTest {
  @Test
  void storesInstanceId() {
    SoundStopMessage msg = new SoundStopMessage(123L);
    assertEquals(123L, msg.soundInstanceId());
  }
}
