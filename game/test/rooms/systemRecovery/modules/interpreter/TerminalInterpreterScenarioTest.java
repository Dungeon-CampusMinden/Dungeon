package rooms.systemRecovery.modules.interpreter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests representative student solutions across complete terminal scenarios. */
public class TerminalInterpreterScenarioTest {

  private final TerminalInterpreter interpreter = TerminalInterpreter.instance();

  /** Resets the singleton before registering an isolated scenario. */
  @BeforeEach
  public void setup() {
    interpreter.reset();
  }

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

  private static TerminalCodeRequirement requirement(String... regexes) {
    CodeLine[] codeLines =
        java.util.Arrays.stream(regexes)
            .map(regex -> new CodeLine(Pattern.compile(regex)))
            .toArray(CodeLine[]::new);
    return new TerminalCodeRequirement(codeLines, null, null);
  }

  private static String assignment(String variable, int index, String value) {
    return variable + "\\s*\\[\\s*" + index + "\\s*]\\s*=\\s*" + value;
  }
}
