package rooms.systemRecovery.util.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.modules.interpreter.CodeLine;
import rooms.systemRecovery.modules.interpreter.TerminalAttempt;
import rooms.systemRecovery.modules.interpreter.TerminalCodeRequirement;
import rooms.systemRecovery.modules.interpreter.TerminalInterpreter;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;

/**
 * Tests the room-specific progress guard without coupling the generic interpreter to Petri code.
 */
class SystemRecoveryTerminalControllerTest {

  private static final String MODULE_ARRAY_SOURCE = "String[] module = new String[5];";

  private final TerminalInterpreter interpreter = TerminalInterpreter.instance();
  private final AtomicInteger successCallbacks = new AtomicInteger();
  private final List<TerminalAttempt> unavailableAttempts = new ArrayList<>();
  private final AtomicReference<SystemRecoveryLearningStep> activeStep =
      new AtomicReference<>(SystemRecoveryLearningStep.ENERGY_INSERT_BATTERY);

  @BeforeEach
  void setUp() {
    interpreter.reset();
    interpreter.synchronizeState(TerminalStep.MODULE_ARRAY.stateId());
    successCallbacks.set(0);
    unavailableAttempts.clear();
    activeStep.set(SystemRecoveryLearningStep.ENERGY_INSERT_BATTERY);
    interpreter.register(
        TerminalStep.MODULE_ARRAY.stateId(),
        new TerminalCodeRequirement(
            new CodeLine[] {
              new CodeLine(
                  Pattern.compile(
                      "String\\s*\\[\\s*]\\s*module\\s*=\\s*new\\s+String\\s*\\[\\s*5\\s*]"))
            },
            false,
            ignored -> successCallbacks.incrementAndGet(),
            ignored -> {}));
  }

  @Test
  void validSourceWaitsForItsMatchingLearningStep() {
    SystemRecoveryTerminalController controller = controller();

    assertFalse(controller.interpret(MODULE_ARRAY_SOURCE, 17));
    assertEquals(TerminalStep.MODULE_ARRAY.stateId(), interpreter.currentState());
    assertEquals(0, successCallbacks.get());
    assertEquals(1, unavailableAttempts.size());

    activeStep.set(SystemRecoveryLearningStep.MODULE_ARRAY);

    assertTrue(controller.interpret(MODULE_ARRAY_SOURCE, 17));
    assertEquals(TerminalStep.MODULE_VALUES.stateId(), interpreter.currentState());
    assertEquals(1, successCallbacks.get());
  }

  private SystemRecoveryTerminalController controller() {
    return new SystemRecoveryTerminalController(
        interpreter,
        () -> java.util.Optional.ofNullable(activeStep.get()),
        unavailableAttempts::add);
  }
}
