package rooms.systemRecovery.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Regression tests for accepted array values displayed in Memory Watch. */
class SystemRecoveryMemoryWatchTest {

  @Test
  void recordsDeclaredAndReferencedArraysWithTheirDefaultContents() {
    SystemRecoveryMemoryWatch memoryWatch = new SystemRecoveryMemoryWatch();

    memoryWatch.recordAcceptedSource(
        """
        int[] energieSpeicher = new int[5];
        String[] modulListe = new String[5];
        for (String module : modulListe) {
            if (module != null) {
                int count = 0;
            }
        }
        for (int row = 0; row < map.length; row++) {
            map[row][0] = 1;
        }
        """);

    assertArrayEquals(new String[] {"energieSpeicher", "modulListe"}, memoryWatch.arrayNames());
    assertArrayEquals(
        new String[] {
          "energieSpeicher\tint[]\t[0, 0, 0, 0, 0]",
          "modulListe\tString[]\t[null, null, null, null, null]"
        },
        memoryWatch.arrayEntries());
  }

  @Test
  void appliesLiteralInitializersAndLaterElementAssignments() {
    SystemRecoveryMemoryWatch memoryWatch = new SystemRecoveryMemoryWatch();

    memoryWatch.recordAcceptedSource("int[] energie = new int[]{40, 10, 80};");
    memoryWatch.recordAcceptedSource(
        "String[] module = new String[3]; module[0] = \"CPU\"; module[1] = \"RAM\";");
    memoryWatch.recordAcceptedSource("module[2] = null;");
    memoryWatch.recordAcceptedSource("boolean[] aktiv = {true, false, true};");

    assertArrayEquals(
        new String[] {
          "energie\tint[]\t[40, 10, 80]",
          "module\tString[]\t[\"CPU\", \"RAM\", null]",
          "aktiv\tboolean[]\t[true, false, true]"
        },
        memoryWatch.arrayEntries());
  }

  @Test
  void appliesNumericAssignmentsToTwoDimensionalArrays() {
    SystemRecoveryMemoryWatch memoryWatch = new SystemRecoveryMemoryWatch();

    memoryWatch.recordAcceptedSource("int[][] lager = new int[3][4];");
    memoryWatch.recordAcceptedSource("lager[0][2] = 1; lager[1][3] = 2; lager[2][1] = 3;");

    assertArrayEquals(
        new String[] {"lager\tint[][]\t[[0, 0, 1, 0], [0, 0, 0, 2], [0, 3, 0, 0]]"},
        memoryWatch.arrayEntries());
  }

  @Test
  void restoresAndUpdatesAClientSnapshot() {
    SystemRecoveryMemoryWatch server = new SystemRecoveryMemoryWatch();
    server.recordAcceptedSource("String[] module = new String[2]; module[0] = \"CPU\";");
    SystemRecoveryMemoryWatch client = new SystemRecoveryMemoryWatch();

    client.restoreEntries(server.arrayEntries());
    client.recordAcceptedSource("module[1] = \"RAM\";");

    assertArrayEquals(new String[] {"module\tString[]\t[\"CPU\", \"RAM\"]"}, client.arrayEntries());
  }

  @Test
  void supportsLegacyEntriesAndIgnoresEmptySources() {
    SystemRecoveryMemoryWatch memoryWatch = new SystemRecoveryMemoryWatch();

    memoryWatch.recordAcceptedSource("");
    memoryWatch.recordAcceptedSource("array[0] = 1; array[0] = 2;");

    assertArrayEquals(new String[0], memoryWatch.arrayNames());
    assertEquals("array", SystemRecoveryMemoryWatch.parseEntry("array\tint[]").name());
    assertEquals("int[]", SystemRecoveryMemoryWatch.parseEntry("array\tint[]").type());
    assertEquals("?", SystemRecoveryMemoryWatch.parseEntry("array\tint[]").contents());
  }
}
