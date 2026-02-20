package core.network.messages.c2s;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Tests for {@link InputMessage}. */
class InputMessageTest {

  /** Verifies custom factory defaults. */
  @Test
  void customFactoryUsesDefaults() {
    InputMessage message = InputMessage.custom("escapeRoom:hint_log.open");

    assertEquals(InputMessage.Action.CUSTOM, message.action());
    InputMessage.Custom custom = message.payloadAs(InputMessage.Custom.class);
    assertEquals("escapeRoom:hint_log.open", custom.commandId());
    assertArrayEquals(new byte[0], custom.payload());
    assertEquals(1, custom.schemaVersion());
  }

  /** Verifies command id validation for custom messages. */
  @Test
  void customRejectsInvalidCommandId() {
    assertThrows(
        IllegalArgumentException.class,
        () -> InputMessage.custom("invalid-command-id", new byte[0]));
  }

  /** Verifies payload size validation for custom messages. */
  @Test
  void customRejectsTooLargePayload() {
    byte[] tooLargePayload = new byte[8_193];
    assertThrows(
        IllegalArgumentException.class,
        () -> InputMessage.custom("escapeRoom:hint_log.open", tooLargePayload));
  }
}
