package rooms.systemRecovery.modules.computer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SystemRecoveryComputerDialogLayoutTest {

  @Test
  void outerPaddingShrinksToRemainUsableOnSmallWindows() {
    assertEquals(28.8f, SystemRecoveryComputerDialog.outerPaddingForSize(640, 480), 0.01f);
    assertEquals(36f, SystemRecoveryComputerDialog.outerPaddingForSize(800, 600), 0.01f);
    assertEquals(46.08f, SystemRecoveryComputerDialog.outerPaddingForSize(1024, 768), 0.01f);
    assertEquals(43.2f, SystemRecoveryComputerDialog.outerPaddingForSize(1280, 720), 0.01f);
    assertEquals(64.8f, SystemRecoveryComputerDialog.outerPaddingForSize(1920, 1080), 0.01f);
  }

  @Test
  void paddingHasAMinimumForVerySmallWindows() {
    float padding = SystemRecoveryComputerDialog.outerPaddingForSize(240, 160);

    assertEquals(12f, padding);
    assertTrue(padding * 2 < 160);
  }
}
