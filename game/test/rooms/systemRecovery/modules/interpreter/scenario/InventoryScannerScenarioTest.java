package rooms.systemRecovery.modules.interpreter.scenario;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;
import rooms.systemRecovery.util.interpreter.TerminalStep;

/** Tests the terminal scenario for the inventory scanner. */
public class InventoryScannerScenarioTest extends TerminalScenarioTestSupport {

  /** The inventory scanner accepts counting all non-null modules. */
  @Test
  public void inventoryScannerScenarioIsSupported_riddle3() {
    interpreter.register(
        0,
        orderedRequirement(
            "int\\s+count\\s*=\\s*0\\s*;",
            "for\\s*\\(\\s*String\\s+m\\s*:\\s*module\\s*\\)\\s*\\{",
            "if\\s*\\(\\s*m\\s*!=\\s*null\\s*\\)\\s*\\{",
            "count\\s*\\+\\+\\s*;"));

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

  /** Valid null checks and increment expressions accept a consistently chosen counter name. */
  @Test
  public void inventoryScannerAcceptsEquivalentJavaCountingForms_riddle3() {
    assertTrue(
        acceptsCountAtState(
            TerminalStep.INVENTORY_COUNT.stateId(),
            """
            int occupied = 0;
            for (String part : module) {
                if (!(part == null)) {
                    occupied = occupied + 1;
                }
            }
            """));

    assertTrue(
        acceptsCountAtState(
            TerminalStep.INVENTORY_COUNT.stateId(),
            """
            int filled = 0;
            for (String part : module) {
                if (part != null) {
                    filled++;
                }
            }
            """));
  }

  /** Counter references must be consistent, and Java references cannot be used as booleans. */
  @Test
  public void inventoryScannerRejectsMismatchedCounterAndIfNotReference_riddle3() {
    assertFalse(
        acceptsCountAtState(
            TerminalStep.INVENTORY_COUNT.stateId(),
            """
            int occupied = 0;
            for (String part : module) {
                if (part != null) {
                    count = count + 1;
                }
            }
            """));

    assertFalse(
        acceptsCountAtState(
            TerminalStep.INVENTORY_COUNT.stateId(),
            """
            int occupied = 0;
            for (String part : module) {
                if (!part) {
                    occupied++;
                }
            }
            """));
  }

  /** The system-core count uses the same flexible counter and null-check rules. */
  @Test
  public void centralCountAcceptsEquivalentJavaCountingForms_riddle10() {
    assertTrue(
        acceptsCountAtState(
            TerminalStep.CENTRAL_COUNT.stateId(),
            """
            int total = 0;
            for (String module : modules) {
                if (!(module == null)) {
                    total = total + 1;
                }
            }
            """));
  }

  private boolean acceptsCountAtState(int state, String source) {
    interpreter.reset();
    TerminalInterpreterSetup.setupPreviewStates();
    interpreter.synchronizeState(state);
    return interpreter.interpret(source);
  }
}
