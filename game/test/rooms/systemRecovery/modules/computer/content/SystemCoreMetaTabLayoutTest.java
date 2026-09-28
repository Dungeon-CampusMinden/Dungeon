package rooms.systemRecovery.modules.computer.content;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Checks the final input page's width constraints at common window sizes. */
class SystemCoreMetaTabLayoutTest {

  @Test
  void centersTheFormWithinAWideComputerWindow() {
    assertEquals(1_080f, SystemCoreMetaTab.contentWidth(1_500f));
  }

  @Test
  void keepsTheFormReadableInASmallerWindow() {
    assertEquals(704f, SystemCoreMetaTab.contentWidth(800f));
    assertEquals(320f, SystemCoreMetaTab.contentWidth(300f));
  }
}
