package rooms.systemRecovery.util.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;

/** Tests the complete state flow registered for the real System Recovery terminal setup. */
public class TerminalInterpreterStateFlowTest {

  private static final int FINAL_STATE = 17;

  private final TerminalInterpreter interpreter = TerminalInterpreter.instance();
  private String source;

  /** Registers the same terminal states as the preview/client setup before each test. */
  @BeforeEach
  public void setup() {
    interpreter.reset();
    TerminalInterpreterSetup.setupPreviewStates();
    source = "";
  }

  /** All riddles can be solved in the intended order using one submitted chunk per step. */
  @Test
  public void completesStateFlowWithCorrectStepChunks() {
    submitOk(energyArrayCreation(), 1);
    submitOk(energyAssignments(), 2);
    submitOk(moduleArrayCreation(), 3);
    submitOk(moduleAssignments(), 4);
    submitOk(moduleNullAssignment(), 5);
    submitOk(moduleLengthAccess(), 6);
    submitOk(moduleCountLoop(), 7);
    submitOk(packageArrayCreation(), 8);
    submitOk(packageCollectLoop(), 9);
    submitOk(dataArchiveArrays(), 10);
    submitOk(storageArrayCreation(), 11);
    submitOk(storageAssignments(), 12);
    submitOk(storageAccess(), 13);
    submitOk(mapSearchLoop(), 14);
    submitOk(bubbleSortLoop(), 15);
    submitOk(centralModuleCountLoop(), 16);
    submitOk(centralMapSearchLoop(), FINAL_STATE);
  }

  /** All riddles can be solved when the editor is cleared after each successful submit. */
  @Test
  public void completesStateFlowWhenPreviousSourceIsRemovedAfterEachStep() {
    submitChunkOk(energyArrayCreation(), 1);
    submitChunkOk(energyAssignments(), 2);
    submitChunkOk(moduleArrayCreation(), 3);
    submitChunkOk(moduleAssignments(), 4);
    submitChunkOk(moduleNullAssignment(), 5);
    submitChunkOk(moduleLengthAccess(), 6);
    submitChunkOk(moduleCountLoop(), 7);
    submitChunkOk(packageArrayCreation(), 8);
    submitChunkOk(packageCollectLoop(), 9);
    submitChunkOk(dataArchiveArrays(), 10);
    submitChunkOk(storageArrayCreation(), 11);
    submitChunkOk(storageAssignments(), 12);
    submitChunkOk(storageAccess(), 13);
    submitChunkOk(mapSearchLoop(), 14);
    submitChunkOk(bubbleSortLoop(), 15);
    submitChunkOk(centralModuleCountLoop(), 16);
    submitChunkOk(centralMapSearchLoop(), FINAL_STATE);
  }

  /** Repeating already solved chunks does not advance the state and still allows finishing. */
  @Test
  public void repeatedPreviousCodeDoesNotBreakCompletingStateFlow() {
    submitOk(energyArrayCreation(), 1);
    submitFailsWithAdditionalCode(energyArrayCreation(), 1);
    submitOk(energyAssignments(), 2);
    submitFailsWithAdditionalCode(energyAssignments(), 2);

    submitOk(moduleArrayCreation(), 3);
    submitOk(moduleAssignments(), 4);
    submitFailsWithAdditionalCode(moduleAssignments(), 4);
    submitOk(moduleNullAssignment(), 5);
    submitOk(moduleLengthAccess(), 6);
    submitOk(moduleCountLoop(), 7);
    submitOk(packageArrayCreation(), 8);
    submitOk(packageCollectLoop(), 9);
    submitOk(dataArchiveArrays(), 10);
    submitOk(storageArrayCreation(), 11);
    submitOk(storageAssignments(), 12);
    submitOk(storageAccess(), 13);
    submitOk(mapSearchLoop(), 14);
    submitOk(bubbleSortLoop(), 15);
    submitOk(centralModuleCountLoop(), 16);
    submitOk(centralMapSearchLoop(), FINAL_STATE);
  }

  /** Skipping ahead fails without advancing, then the flow can still be completed correctly. */
  @Test
  public void skippedTasksDoNotAdvanceAndCanBeSolvedAfterwards() {
    submitOk(energyArrayCreation(), 1);
    submitFailsWithAdditionalCode(moduleArrayCreation(), 1);
    submitOk(energyAssignments(), 2);

    submitOk(moduleArrayCreation(), 3);
    submitFailsWithAdditionalCode(moduleNullAssignment(), 3);
    submitOk(moduleAssignments(), 4);
    submitOk(moduleNullAssignment(), 5);
    submitOk(moduleLengthAccess(), 6);

    submitFailsWithAdditionalCode(packageArrayCreation(), 6);
    submitOk(moduleCountLoop(), 7);
    submitOk(packageArrayCreation(), 8);
    submitOk(packageCollectLoop(), 9);
    submitOk(dataArchiveArrays(), 10);
    submitOk(storageArrayCreation(), 11);

    submitFailsWithAdditionalCode(mapSearchLoop(), 11);
    submitOk(storageAssignments(), 12);
    submitOk(storageAccess(), 13);
    submitOk(mapSearchLoop(), 14);

    submitOk(bubbleSortLoop(), 15);
    submitFailsWithAdditionalCode(centralMapSearchLoop(), 15);
    submitOk(centralModuleCountLoop(), 16);
    submitOk(centralMapSearchLoop(), FINAL_STATE);
  }

  private void submitOk(String addition, int expectedState) {
    source += addition;
    assertTrue(interpreter.interpret(source), addition);
    assertEquals(expectedState, interpreter.currentState(), addition);
  }

  private void submitChunkOk(String chunk, int expectedState) {
    assertTrue(interpreter.interpret(chunk), chunk);
    assertEquals(expectedState, interpreter.currentState(), chunk);
  }

  private void submitFailsWithAdditionalCode(String addition, int expectedState) {
    String previousSource = source;
    source += addition;
    assertFalse(interpreter.interpret(source), addition);
    assertEquals(expectedState, interpreter.currentState(), addition);
    source = previousSource;
  }

  private static String energyArrayCreation() {
    return """
        int[] energie = new int[5];
        """;
  }

  private static String energyAssignments() {
    return """
        energie[4] = 60;
        energie[2] = 80;
        energie[0] = 40;
        energie[3] = 30;
        energie[1] = 10;
        """;
  }

  private static String moduleArrayCreation() {
    return """
        String[] module = new String[5];
        """;
  }

  private static String moduleAssignments() {
    return """
        module[0] = "CPU";
        module[1] = "RAM";
        module[2] = "GPU";
        module[3] = "SSD";
        module[4] = "NETWORK";
        """;
  }

  private static String moduleNullAssignment() {
    return """
        module[2] = null;
        """;
  }

  private static String moduleLengthAccess() {
    return """
        module.length;
        """;
  }

  private static String moduleCountLoop() {
    return """
        int count = 0;
        for (String entry : module) {
            if (entry != null) {
                count++;
            }
        }
        """;
  }

  private static String packageArrayCreation() {
    return """
        int[] pakete = {15, 40, 20, 60, 30};
        """;
  }

  private static String packageCollectLoop() {
    return """
        for (int index = 0; index < pakete.length; index++) {
            roboter.collect(pakete[index]);
        }
        """;
  }

  private static String dataArchiveArrays() {
    return """
        int[] energie = {20, 50, 80};
        String[] module = {"CPU", "GPU", "RAM"};
        boolean[] aktiv = {true, false, true};
        """;
  }

  private static String storageArrayCreation() {
    return """
        int[][] lager = new int[3][4];
        """;
  }

  private static String storageAssignments() {
    return """
        lager[0][2] = 1;
        lager[1][3] = 2;
        lager[2][1] = 3;
        """;
  }

  private static String storageAccess() {
    return """
        lager[1][3];
        """;
  }

  private static String mapSearchLoop() {
    return """
        for (int row = 0; row < map.length; row++) {
            for (int column = 0; column < map[row].length; column++) {
                if (map[row][column] == 1) {
                    roboter.collect();
                }
            }
        }
        """;
  }

  private static String bubbleSortLoop() {
    return """
        for (int i = 0; i < array.length - 1; i++) {
            for (int j = 0; j < array.length - 1 - i; j++) {
                if (array[j] > array[j + 1]) {
                    int temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }
        """;
  }

  private static String centralModuleCountLoop() {
    return """
        int count = 0;
        for (String module : modules) {
            if (module != null) {
                count++;
            }
        }
        """;
  }

  private static String centralMapSearchLoop() {
    return """
        for (int row = 0; row < map.length; row++) {
            for (int column = 0; column < map[row].length; column++) {
                if (map[row][column] == 1) {
                    roboter.collect();
                }
            }
        }
        """;
  }
}
