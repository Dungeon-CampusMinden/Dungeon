package rooms.systemRecovery.modules.interpreter.scenario;

import java.util.Arrays;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import rooms.systemRecovery.modules.interpreter.CodeLine;
import rooms.systemRecovery.modules.interpreter.TerminalCodeRequirement;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;

/** Shared interpreter setup and pattern helpers for terminal scenario tests. */
abstract class TerminalScenarioTestSupport {

  protected final TerminalInterpreter interpreter = TerminalInterpreter.instance();

  /** Resets the singleton before registering an isolated scenario. */
  @BeforeEach
  void resetInterpreter() {
    interpreter.reset();
  }

  protected static TerminalCodeRequirement requirement(String... regexes) {
    return requirement(false, regexes);
  }

  protected static TerminalCodeRequirement orderedRequirement(String... regexes) {
    return requirement(true, regexes);
  }

  private static TerminalCodeRequirement requirement(boolean requiresOrder, String... regexes) {
    CodeLine[] codeLines =
        Arrays.stream(regexes)
            .map(regex -> new CodeLine(Pattern.compile(regex)))
            .toArray(CodeLine[]::new);
    return new TerminalCodeRequirement(codeLines, requiresOrder, null, null);
  }

  protected static String assignment(String variable, int index, String value) {
    return variable + "\\s*\\[\\s*" + index + "\\s*]\\s*=\\s*" + value;
  }

  protected static String twoDimensionalAssignment(
      String variable, int firstIndex, int secondIndex, String value) {
    return variable
        + "\\s*\\[\\s*"
        + firstIndex
        + "\\s*]\\s*\\[\\s*"
        + secondIndex
        + "\\s*]\\s*=\\s*"
        + value;
  }

  protected static String indexedForLoop(String variable, String upperBound) {
    return "for\\s*\\(\\s*int\\s+"
        + variable
        + "\\s*=\\s*0\\s*;\\s*"
        + variable
        + "\\s*<\\s*"
        + upperBound
        + "\\s*;\\s*"
        + variable
        + "\\+\\+\\s*\\)\\s*\\{";
  }
}
