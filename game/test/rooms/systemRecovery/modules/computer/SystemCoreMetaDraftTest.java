package rooms.systemRecovery.modules.computer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.modules.computer.content.SystemCoreMetaDraft;

/** Verifies that unfinished final System Core input survives dialog reconstruction. */
class SystemCoreMetaDraftTest {

  @BeforeEach
  void resetDraft() {
    SystemCoreMetaDraft.clear();
  }

  /** Each field is restored from the client-local draft after the dialog is closed. */
  @Test
  void preservesUnfinishedValues() {
    SystemCoreMetaDraft.energyValue(0, "42");
    SystemCoreMetaDraft.energyValue(4, "23");
    SystemCoreMetaDraft.moduleCount("3");
    SystemCoreMetaDraft.scannedModuleCount("5");

    assertEquals("42", SystemCoreMetaDraft.energyValue(0));
    assertEquals("23", SystemCoreMetaDraft.energyValue(4));
    assertEquals("3", SystemCoreMetaDraft.moduleCount());
    assertEquals("5", SystemCoreMetaDraft.scannedModuleCount());
  }

  /** The explicit clear action removes all unfinished values. */
  @Test
  void clearRemovesUnfinishedValues() {
    SystemCoreMetaDraft.energyValue(2, "8");
    SystemCoreMetaDraft.moduleCount("3");
    SystemCoreMetaDraft.scannedModuleCount("5");

    SystemCoreMetaDraft.clear();

    assertEquals("", SystemCoreMetaDraft.energyValue(2));
    assertEquals("", SystemCoreMetaDraft.moduleCount());
    assertEquals("", SystemCoreMetaDraft.scannedModuleCount());
  }
}
