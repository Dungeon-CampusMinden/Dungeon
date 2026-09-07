package rooms.systemRecovery.modules.interpreter.scenario;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests the terminal scenario for the transport storage. */
public class TransportStorageScenarioTest extends TerminalScenarioTestSupport {

  /** The transport storage accepts its package array followed by the robot loop. */
  @Test
  public void transportStorageScenarioIsSupported_riddle4() {
    interpreter.register(
        0,
        requirement(
            "int\\s*\\[\\s*]\\s*pakete\\s*=\\s*\\{\\s*15\\s*,\\s*40\\s*,"
                + "\\s*20\\s*,\\s*60\\s*,\\s*30\\s*}"));
    interpreter.register(
        1,
        requirement(
            "for\\s*\\(\\s*int\\s+i\\s*=\\s*0\\s*;\\s*i\\s*<\\s*pakete\\s*"
                + "\\.\\s*length\\s*;\\s*i\\+\\+\\s*\\)\\s*\\{",
            "roboter\\s*\\.\\s*aufnehmen\\s*\\(\\s*pakete\\s*\\[\\s*i\\s*]\\s*\\)"));

    String source = "int[] pakete = {15, 40, 20, 60, 30};";
    assertTrue(interpreter.interpret(source));

    source +=
        """

        for (int i = 0; i < pakete.length; i++) {
            roboter.aufnehmen(pakete[i]);
        }
        """;
    assertTrue(interpreter.interpret(source));
  }
}
