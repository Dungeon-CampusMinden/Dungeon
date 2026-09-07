package rooms.systemRecovery.modules.interpreter.scenario;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests the three terminal steps in the central data center. */
public class CentralDataCenterScenarioTest extends TerminalScenarioTestSupport {

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
            "if\\s*\\(\\s*map\\s*\\[\\s*i\\s*]\\s*\\[\\s*j\\s*]\\s*==\\s*1\\s*\\)" + "\\s*\\{",
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

    assertTrue(interpreter.interpret(source));
  }
}
