package core.network.messages.c2s;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SoundFinishedMessageTest {
  @Test
  void storesInstanceId() {
    SoundFinishedMessage msg = new SoundFinishedMessage(12345L);
    assertEquals(12345L, msg.soundInstanceId());
  }

  @Test
  void supportsZeroInstanceId() {
    SoundFinishedMessage msg = new SoundFinishedMessage(0L);
    assertEquals(0L, msg.soundInstanceId());
  }

  @Test
  void supportsNegativeInstanceId() {
    SoundFinishedMessage msg = new SoundFinishedMessage(-1L);
    assertEquals(-1L, msg.soundInstanceId());
  }
}
