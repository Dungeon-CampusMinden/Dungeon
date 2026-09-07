package rooms.systemRecovery.modules.interpreter.scenario;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests the terminal scenario for the search robot. */
public class SearchRobotScenarioTest extends TerminalScenarioTestSupport {

  /** The search robot accepts the nested traversal and battery condition. */
  @Test
  public void searchRobotScenarioIsSupported_riddle9() {
    interpreter.register(
        0,
        orderedRequirement(
            indexedForLoop("i", "map\\s*\\.\\s*length"),
            indexedForLoop("j", "map\\s*\\[\\s*i\\s*]\\s*\\.\\s*length"),
            "if\\s*\\(\\s*map\\s*\\[\\s*i\\s*]\\s*\\[\\s*j\\s*]\\s*==\\s*1\\s*\\)"
                + "\\s*\\{",
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

  /** The battery condition must be inside the nested map traversal. */
  @Test
  public void searchRobotRejectsConditionOutsideNestedLoops_riddle9() {
    interpreter.register(
        0,
        orderedRequirement(
            indexedForLoop("i", "map\\s*\\.\\s*length"),
            indexedForLoop("j", "map\\s*\\[\\s*i\\s*]\\s*\\.\\s*length"),
            "if\\s*\\(\\s*map\\s*\\[\\s*i\\s*]\\s*\\[\\s*j\\s*]\\s*==\\s*1\\s*\\)"
                + "\\s*\\{",
            "roboter\\s*\\.\\s*collect\\s*\\(\\s*\\)"));

    String source =
        """
        for (int i = 0; i < map.length; i++) {
        }
        for (int j = 0; j < map[i].length; j++) {
        }
        if (map[i][j] == 1) {
        }
        """;

    assertFalse(interpreter.interpret(source));
  }
}
