package rooms.systemRecovery.modules.interpreter.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;

/** Tests the three subsystem checks in the central data center. */
public class CentralDataCenterScenarioTest extends TerminalScenarioTestSupport {

  /** The production registration accepts all three central-computer steps in sequence. */
  @Test
  public void centralDataCenterProductionStateFlowIsSupported_riddle10() {
    TerminalInterpreterSetup.setupPreviewStates();
    interpreter.synchronizeState(TerminalInterpreterSetup.CENTRAL_SORT_STATE);

    assertTrue(
        interpreter.interpret(
            """
            for (int outer = 0; outer < array.length - 1; outer++) {
                for (int index = 0; index < array.length - 1 - outer; index++) {
                    if (array[index] > array[index + 1]) {
                        int temporary = array[index];
                        array[index] = array[index + 1];
                        array[index + 1] = temporary;
                    }
                }
            }
            """));
    assertEquals(14, interpreter.currentState());

    assertTrue(
        interpreter.interpret(
            """
            int count = 0;
            for (String entry : modules) {
                if (entry != null) {
                    count++;
                }
            }
            """));
    assertEquals(15, interpreter.currentState());

    assertTrue(
        interpreter.interpret(
            """
            for (int row = 0; row < map.length; row++) {
                for (int column = 0; column < map[row].length; column++) {
                    roboter.collect();
                }
            }
            """));
    assertEquals(TerminalInterpreterSetup.CENTRAL_META_STATE, interpreter.currentState());
  }

  /** The central data center accepts the complete bubble-sort implementation. */
  @Test
  public void centralDataCenterBubbleSortIsSupported_riddle10() {
    interpreter.register(
        0,
        orderedRequirement(
            indexedForLoop("i", "array\\s*\\.\\s*length\\s*-\\s*1"),
            "for\\s*\\(\\s*int\\s+j\\s*=\\s*0\\s*;\\s*j\\s*<\\s*array\\s*"
                + "\\.\\s*length\\s*-\\s*1\\s*-\\s*i\\s*;\\s*j\\+\\+\\s*\\)\\s*\\{",
            "if\\s*\\(\\s*array\\s*\\[\\s*j\\s*]\\s*>\\s*array\\s*"
                + "\\[\\s*j\\s*\\+\\s*1\\s*]\\s*\\)\\s*\\{",
            "int\\s+temp\\s*=\\s*array\\s*\\[\\s*j\\s*]",
            "array\\s*\\[\\s*j\\s*]\\s*=\\s*array\\s*\\[\\s*j\\s*\\+\\s*1\\s*]",
            "array\\s*\\[\\s*j\\s*\\+\\s*1\\s*]\\s*=\\s*temp"));

    String source =
        """
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

    assertTrue(interpreter.interpret(source));
  }

  /** Bubble sort lines must be nested in the expected condition body. */
  @Test
  public void centralDataCenterBubbleSortRejectsSwapOutsideCondition_riddle10() {
    interpreter.register(
        0,
        orderedRequirement(
            indexedForLoop("i", "array\\s*\\.\\s*length\\s*-\\s*1"),
            "for\\s*\\(\\s*int\\s+j\\s*=\\s*0\\s*;\\s*j\\s*<\\s*array\\s*"
                + "\\.\\s*length\\s*-\\s*1\\s*-\\s*i\\s*;\\s*j\\+\\+\\s*\\)\\s*\\{",
            "if\\s*\\(\\s*array\\s*\\[\\s*j\\s*]\\s*>\\s*array\\s*"
                + "\\[\\s*j\\s*\\+\\s*1\\s*]\\s*\\)\\s*\\{",
            "int\\s+temp\\s*=\\s*array\\s*\\[\\s*j\\s*]",
            "array\\s*\\[\\s*j\\s*]\\s*=\\s*array\\s*\\[\\s*j\\s*\\+\\s*1\\s*]",
            "array\\s*\\[\\s*j\\s*\\+\\s*1\\s*]\\s*=\\s*temp"));

    String source =
        """
        for (int i = 0; i < array.length - 1; i++) {
            for (int j = 0; j < array.length - 1 - i; j++) {
                if (array[j] > array[j + 1]) {
                }
                int temp = array[j];
                array[j] = array[j + 1];
                array[j + 1] = temp;
            }
        }
        """;

    assertFalse(interpreter.interpret(source));
  }

  /** The central data center accepts counting all non-null modules. */
  @Test
  public void centralDataCenterModuleCountIsSupported_riddle10() {
    interpreter.register(
        0,
        orderedRequirement(
            "int\\s+count\\s*=\\s*0",
            "for\\s*\\(\\s*String\\s+module\\s*:\\s*modules\\s*\\)\\s*\\{",
            "if\\s*\\(\\s*module\\s*!=\\s*null\\s*\\)\\s*\\{",
            "count\\s*\\+\\+"));

    String source =
        """
        int count = 0;

        for (String module : modules) {
            if (module != null) {
                count++;
            }
        }
        """;

    assertTrue(interpreter.interpret(source));
  }

  /** The central data center accepts its nested two-dimensional search. */
  @Test
  public void centralDataCenterMapSearchIsSupported_riddle10() {
    interpreter.register(
        0,
        orderedRequirement(
            indexedForLoop("i", "map\\s*\\.\\s*length"),
            indexedForLoop("j", "map\\s*\\[\\s*i\\s*]\\s*\\.\\s*length"),
            "roboter\\s*\\.\\s*collect\\s*\\(\\s*\\)"));

    String source =
        """
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                roboter.collect();
            }
        }
        """;

    assertTrue(interpreter.interpret(source));
  }

  /** The central search visits every cell instead of filtering by a marker value. */
  @Test
  public void centralDataCenterMapSearchRejectsColorFilter_riddle10() {
    interpreter.register(
        0,
        orderedRequirement(
            indexedForLoop("i", "map\\s*\\.\\s*length"),
            indexedForLoop("j", "map\\s*\\[\\s*i\\s*]\\s*\\.\\s*length"),
            "roboter\\s*\\.\\s*collect\\s*\\(\\s*\\)"));

    String source =
        """
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 1) {
                    roboter.collect();
                }
            }
        }
        """;

    assertFalse(interpreter.interpret(source));
  }
}
