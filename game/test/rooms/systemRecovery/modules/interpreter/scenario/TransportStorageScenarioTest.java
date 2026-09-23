package rooms.systemRecovery.modules.interpreter.scenario;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import rooms.systemRecovery.util.interpreter.TerminalInterpreterSetup;
import rooms.systemRecovery.util.interpreter.TerminalStep;

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
        orderedRequirement(
            "for\\s*\\(\\s*int\\s+i\\s*=\\s*0\\s*;\\s*i\\s*<\\s*pakete\\s*"
                + "\\.\\s*length\\s*;\\s*i\\+\\+\\s*\\)\\s*\\{",
            "roboter\\s*\\.\\s*collect\\s*\\(\\s*\\)"));

    String source = "int[] pakete = {15, 40, 20, 60, 30};";
    assertTrue(interpreter.interpret(source));

    source +=
        """

        for (int i = 0; i < pakete.length; i++) {
            roboter.collect();
        }
        """;
    assertTrue(interpreter.interpret(source));
  }

  /** The room configuration carries the chosen array name into the package loop. */
  @Test
  public void transportStorageReusesAPlayerChosenArrayName_riddle4() {
    TerminalInterpreterSetup.setupPreviewStates();
    interpreter.synchronizeState(TerminalStep.TRANSPORT_ARRAY.stateId());

    assertTrue(interpreter.interpret("int[] packets = {15, 40, 20, 60, 30};"));
    assertTrue(
        interpreter.interpret(
            """
            for (int index = 0; index < packets.length; index++) {
                roboter.collect();
            }
            """));
  }

  /** A package loop using a different name than the declaration is rejected. */
  @Test
  public void transportStorageRejectsAnInconsistentArrayName_riddle4() {
    TerminalInterpreterSetup.setupPreviewStates();
    interpreter.synchronizeState(TerminalStep.TRANSPORT_ARRAY.stateId());

    assertTrue(interpreter.interpret("int[] packets = {15, 40, 20, 60, 30};"));
    assertFalse(
        interpreter.interpret(
            """
            for (int index = 0; index < pakete.length; index++) {
                roboter.collect();
            }
            """));
  }

  /** The robot command must be inside the loop body. */
  @Test
  public void transportStorageRejectsRobotCommandOutsideLoop_riddle4() {
    interpreter.register(
        0,
        orderedRequirement(
            "for\\s*\\(\\s*int\\s+i\\s*=\\s*0\\s*;\\s*i\\s*<\\s*pakete\\s*"
                + "\\.\\s*length\\s*;\\s*i\\+\\+\\s*\\)\\s*\\{",
            "roboter\\s*\\.\\s*collect\\s*\\(\\s*\\)"));

    String source =
        """
        for (int i = 0; i < pakete.length; i++) {
        }
        roboter.collect();
        """;

    assertFalse(interpreter.interpret(source));
  }
}
