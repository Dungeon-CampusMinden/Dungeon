package rooms.systemRecovery.modules.interpreter.scenario;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests the terminal scenario for the inventory scanner. */
public class InventoryScannerScenarioTest extends TerminalScenarioTestSupport {

  /** The inventory scanner accepts counting all non-null modules. */
  @Test
  public void inventoryScannerScenarioIsSupported_riddle3() {
    interpreter.register(
        0,
        requirement(
            "int\\s+count\\s*=\\s*0",
            "for\\s*\\(\\s*String\\s+m\\s*:\\s*module\\s*\\)\\s*\\{",
            "if\\s*\\(\\s*m\\s*!=\\s*null\\s*\\)\\s*\\{",
            "count\\s*\\+\\+"));

    String source =
        """
        int count = 0;

        for (String m : module) {
            if (m != null) {
                count++;
            }
        }
        """;

    assertTrue(interpreter.interpret(source));
  }
}
