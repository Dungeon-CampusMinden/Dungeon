package rooms.systemRecovery.modules.interpreter.scenario;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests the terminal scenario for the defective module storage. */
public class ModuleStorageScenarioTest extends TerminalScenarioTestSupport {

  /** The module scenario accepts declaration, initialization, removal, and length access. */
  @Test
  public void moduleStorageScenarioIsSupported_riddle2() {
    interpreter.register(
        0, requirement("String\\s*\\[\\s*]\\s*module\\s*=\\s*new\\s+String\\s*\\[\\s*5\\s*]"));
    interpreter.register(
        1,
        requirement(
            assignment("module", 0, "\"CPU\""),
            assignment("module", 1, "\"RAM\""),
            assignment("module", 2, "\"GPU\""),
            assignment("module", 3, "\"SSD\""),
            assignment("module", 4, "\"NETWORK\"")));
    interpreter.register(2, requirement(assignment("module", 2, "null")));
    interpreter.register(3, requirement("module\\s*\\.\\s*length"));

    String source = "String[] module = new String[5];";
    assertTrue(interpreter.interpret(source));

    source +=
        "module[0] = \"CPU\";"
            + "module[1] = \"RAM\";"
            + "module[2] = \"GPU\";"
            + "module[3] = \"SSD\";"
            + "module[4] = \"NETWORK\";";
    assertTrue(interpreter.interpret(source));

    source += "module[2] = null;";
    assertTrue(interpreter.interpret(source));

    source += "module.length;";
    assertTrue(interpreter.interpret(source));
  }
}
