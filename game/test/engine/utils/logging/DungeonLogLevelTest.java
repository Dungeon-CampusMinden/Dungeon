package engine.utils.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.logging.Level;
import org.junit.jupiter.api.Test;

/** Tests for {@link DungeonLogLevel}. */
class DungeonLogLevelTest {

  @Test
  void usesTheStandardJavaLevelsForAllInfoAndWarn() {
    assertEquals(Level.ALL, DungeonLogLevel.ALL.toJulLevel());
    assertEquals(Level.INFO, DungeonLogLevel.INFO.toJulLevel());
    assertEquals(Level.WARNING, DungeonLogLevel.WARN.toJulLevel());
  }

  @Test
  void assignsASeverityToEachCustomLevel() {
    assertEquals(100, DungeonLogLevel.TRACE.value());
    assertEquals(200, DungeonLogLevel.DEBUG.value());
    assertEquals(950, DungeonLogLevel.ERROR.value());
    assertEquals(1100, DungeonLogLevel.FATAL.value());
  }

  @Test
  void aWarningThresholdAllowsWarningsAndErrors() {
    assertFalse(DungeonLogLevel.INFO.isEnabled(DungeonLogLevel.WARN));
    assertTrue(DungeonLogLevel.WARN.isEnabled(DungeonLogLevel.WARN));
    assertTrue(DungeonLogLevel.ERROR.isEnabled(DungeonLogLevel.WARN));
  }
}
