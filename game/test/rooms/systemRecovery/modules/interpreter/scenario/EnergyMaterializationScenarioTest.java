package rooms.systemRecovery.modules.interpreter.scenario;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests the terminal scenario for the materialization chamber. */
public class EnergyMaterializationScenarioTest extends TerminalScenarioTestSupport {

  /** The energy scenario accepts its declaration followed by all five assignments. */
  @Test
  public void energyMaterializationScenarioIsSupported_riddle1() {
    interpreter.register(
        0, requirement("int\\s*\\[\\s*]\\s*energie\\s*=\\s*new\\s+int\\s*\\[\\s*5\\s*]"));
    interpreter.register(
        1,
        requirement(
            assignment("energie", 0, "40"),
            assignment("energie", 1, "10"),
            assignment("energie", 2, "80"),
            assignment("energie", 3, "30"),
            assignment("energie", 4, "60")));

    String source = "int[] energie = new int[5];";
    assertTrue(interpreter.interpret(source));

    source +=
        "energie[0] = 40;"
            + "energie[1] = 10;"
            + "energie[2] = 80;"
            + "energie[3] = 30;"
            + "energie[4] = 60;";
    assertTrue(interpreter.interpret(source));
  }
}
