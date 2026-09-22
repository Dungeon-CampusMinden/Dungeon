package rooms.systemRecovery.petrinet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.util.interpreter.TerminalStep;

/** Contract tests for the ordered learning-step model and its single terminal mapping. */
class SystemRecoveryLearningStepTest {

  @Test
  void listsTwentyEightLearningStepsAndOneTerminalMarkerInOrder() {
    SystemRecoveryLearningStep[] steps = SystemRecoveryLearningStep.values();

    assertEquals(29, steps.length);
    assertEquals(SystemRecoveryLearningStep.ENERGY_ARRAY, steps[0]);
    assertEquals(SystemRecoveryLearningStep.CORE_META, steps[27]);
    assertEquals(SystemRecoveryLearningStep.COMPLETE, steps[28]);
    assertTrue(
        Arrays.stream(steps).filter(SystemRecoveryLearningStep::isLearningStep).count() == 28);
  }

  @Test
  void mapsEveryTerminalStepToExactlyOneLearningStep() {
    Set<TerminalStep> mapped =
        Arrays.stream(SystemRecoveryLearningStep.values())
            .flatMap(step -> step.terminalStep().stream())
            .collect(Collectors.toSet());

    assertEquals(EnumSet.allOf(TerminalStep.class), mapped);
    for (TerminalStep terminalStep : TerminalStep.values()) {
      assertEquals(
          terminalStep,
          SystemRecoveryLearningStep.fromTerminalStep(terminalStep)
              .orElseThrow()
              .terminalStep()
              .orElseThrow());
    }
  }

  @Test
  void keepsStableDistinctHintKeysForEveryLearningStep() {
    Set<String> keys =
        Arrays.stream(SystemRecoveryLearningStep.values())
            .filter(SystemRecoveryLearningStep::isLearningStep)
            .map(SystemRecoveryLearningStep::hintKey)
            .collect(Collectors.toSet());

    assertEquals(28, keys.size());
    assertFalse(SystemRecoveryLearningStep.COMPLETE.isLearningStep());
    assertTrue(SystemRecoveryLearningStep.COMPLETE.terminalStep().isEmpty());
  }

  @Test
  void storesTerminalHistoryBoundaryOnlyOnMainPuzzleCheckpoints() {
    assertEquals(0, SystemRecoveryLearningStep.ENERGY_ARRAY.acceptedTerminalInputCount());
    assertEquals(2, SystemRecoveryLearningStep.MODULE_ARRAY.acceptedTerminalInputCount());
    assertEquals(12, SystemRecoveryLearningStep.SYSTEM_CORE_ACCESS.acceptedTerminalInputCount());
    assertEquals(-1, SystemRecoveryLearningStep.ENERGY_VALUES.acceptedTerminalInputCount());
    assertEquals(-1, SystemRecoveryLearningStep.COMPLETE.acceptedTerminalInputCount());
  }
}
