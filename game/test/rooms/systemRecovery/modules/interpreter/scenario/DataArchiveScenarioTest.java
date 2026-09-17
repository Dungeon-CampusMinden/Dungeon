package rooms.systemRecovery.modules.interpreter.scenario;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;

/** Tests the terminal scenario for the data archive. */
public class DataArchiveScenarioTest extends TerminalScenarioTestSupport {

  /** The real archive setup accepts arbitrary line and value order. */
  @Test
  public void dataArchiveAcceptsUnorderedArrayLiterals_riddle7() {
    TerminalInterpreterSetup.setupPreviewStates();
    advanceToDataArchive();

    String source =
        """
        boolean aktiv[] = {false, true, true};
        String module[] = {"RAM", "CPU", "GPU"};
        int energie[] = {80, 20, 50};
        """;

    assertTrue(interpreter.interpret(source));
    assertEquals(10, interpreter.currentState());
  }

  /** The archive rejects a literal with missing or duplicated values. */
  @Test
  public void dataArchiveRejectsIncompleteArrayContents_riddle7() {
    TerminalInterpreterSetup.setupPreviewStates();
    advanceToDataArchive();

    String source =
        """
        int[] energie = {20, 20, 80};
        String[] module = {"CPU", "GPU", "RAM"};
        boolean[] aktiv = {true, false, true};
        """;

    assertFalse(interpreter.interpret(source));
    assertEquals(9, interpreter.currentState());
  }

  private void advanceToDataArchive() {
    interpreter.synchronizeState(9);
  }
}
