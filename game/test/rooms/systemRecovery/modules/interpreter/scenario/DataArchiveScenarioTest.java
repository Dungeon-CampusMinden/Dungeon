package rooms.systemRecovery.modules.interpreter.scenario;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests the terminal scenario for the data archive. */
public class DataArchiveScenarioTest extends TerminalScenarioTestSupport {

  /** The data archive accepts the three differently typed array declarations. */
  @Test
  public void dataArchiveScenarioIsSupported_riddle7() {
    interpreter.register(
        0,
        requirement(
            "int\\s*\\[\\s*]\\s*energie\\s*=\\s*\\{\\s*20\\s*,\\s*50\\s*,\\s*80\\s*}",
            "String\\s*\\[\\s*]\\s*module\\s*=\\s*\\{\\s*\"CPU\"\\s*,\\s*"
                + "\"GPU\"\\s*,\\s*\"RAM\"\\s*}",
            "boolean\\s*\\[\\s*]\\s*aktiv\\s*=\\s*\\{\\s*true\\s*,\\s*false\\s*,"
                + "\\s*true\\s*}"));

    String source =
        """
        int[] energie = {20, 50, 80};
        String[] module = {"CPU", "GPU", "RAM"};
        boolean[] aktiv = {true, false, true};
        """;

    assertTrue(interpreter.interpret(source));
  }
}
