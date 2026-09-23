package rooms.systemRecovery.util.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzle;

/** Tests the single source of truth for System Recovery terminal steps. */
class TerminalStepTest {

  @Test
  void allStateIdsAreUniqueAndResolvable() {
    Set<Integer> stateIds = new HashSet<>();

    for (TerminalStep step : TerminalStep.values()) {
      assertTrue(stateIds.add(step.stateId()), "duplicate state ID: " + step.stateId());
      assertEquals(step, TerminalStep.fromStateId(step.stateId()).orElseThrow());
      assertNotNull(step.puzzle());
    }
  }

  @Test
  void specialInputStepsAreNotNormalTerminalSteps() {
    assertEquals(TerminalStep.InputMode.CHIP_EDITOR, TerminalStep.SEARCH_PROGRAM.inputMode());
    assertEquals(TerminalStep.InputMode.FORM, TerminalStep.SYSTEM_CORE_META.inputMode());
    assertEquals(TerminalStep.InputMode.TERMINAL, TerminalStep.ENERGY_ARRAY.inputMode());
  }

  @Test
  void stepsExposeThePuzzleTheyBelongTo() {
    assertEquals(SystemRecoveryPuzzle.ENERGY, TerminalStep.ENERGY_VALUES.puzzle());
    assertEquals(
        SystemRecoveryPuzzle.TWO_DIMENSIONAL_STORAGE, TerminalStep.STORAGE_VALUES.puzzle());
    assertEquals(SystemRecoveryPuzzle.SYSTEM_CORE, TerminalStep.SYSTEM_CORE_META.puzzle());
  }

  @Test
  void unknownStateIdDoesNotFallBackToAnotherStep() {
    assertTrue(TerminalStep.fromStateId(999).isEmpty());
  }
}
