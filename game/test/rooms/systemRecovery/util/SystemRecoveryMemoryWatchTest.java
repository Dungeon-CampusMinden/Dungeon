package rooms.systemRecovery.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Regression tests for accepted array-name extraction used by the Memory Watch tab. */
class SystemRecoveryMemoryWatchTest {

  @Test
  void recordsDeclaredAndReferencedArrayNamesInFirstSeenOrder() {
    SystemRecoveryMemoryWatch memoryWatch = new SystemRecoveryMemoryWatch();

    memoryWatch.recordAcceptedSource(
        """
        int[] energieSpeicher = new int[5];
        String[] modulListe = new String[5];
        int count = 0;
        for (String module : modulListe) {
            if (module != null) {
                count++;
            }
        }
        for (int row = 0; row < map.length; row++) {
            map[row][0] = 1;
        }
        """);

    assertArrayEquals(
        new String[] {"energieSpeicher", "modulListe", "map"}, memoryWatch.arrayNames());
    assertArrayEquals(
        new String[] {"energieSpeicher\tint[]", "modulListe\tString[]", "map\tint[][]"},
        memoryWatch.arrayEntries());
  }

  @Test
  void ignoresEmptySourcesAndDoesNotDuplicateNames() {
    SystemRecoveryMemoryWatch memoryWatch = new SystemRecoveryMemoryWatch();

    memoryWatch.recordAcceptedSource("");
    memoryWatch.recordAcceptedSource("array[0] = 1; array[0] = 2;");

    assertArrayEquals(new String[] {"array"}, memoryWatch.arrayNames());
    assertEquals("array", SystemRecoveryMemoryWatch.parseEntry("array\tint[]").name());
    assertEquals("int[]", SystemRecoveryMemoryWatch.parseEntry("array\tint[]").type());
  }
}
