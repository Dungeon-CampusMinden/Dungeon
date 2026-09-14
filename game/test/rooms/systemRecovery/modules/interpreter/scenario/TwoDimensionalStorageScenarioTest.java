package rooms.systemRecovery.modules.interpreter.scenario;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests the terminal scenario for the two-dimensional storage. */
public class TwoDimensionalStorageScenarioTest extends TerminalScenarioTestSupport {

  /** The storage accepts its declaration followed by unordered indexed assignments. */
  @Test
  public void twoDimensionalStorageScenarioIsSupported_riddle8() {
    interpreter.register(
        0,
        requirement(
            "int\\s*\\[\\s*]\\s*\\[\\s*]\\s*lager\\s*=\\s*new\\s+int\\s*"
                + "\\[\\s*3\\s*]\\s*\\[\\s*4\\s*]"));
    interpreter.register(
        1,
        requirement(
            twoDimensionalAssignment("lager", 0, 2, "1"),
            twoDimensionalAssignment("lager", 1, 3, "2"),
            twoDimensionalAssignment("lager", 2, 1, "3")));
    String source = "int[][] lager = new int[3][4];";
    assertTrue(interpreter.interpret(source));

    source +=
        """

        lager[0][2] = 1;
        lager[1][3] = 2;
        lager[2][1] = 3;
        """;
    assertTrue(interpreter.interpret(source));
  }
}
