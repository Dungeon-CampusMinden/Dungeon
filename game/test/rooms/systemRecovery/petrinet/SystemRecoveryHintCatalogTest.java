package rooms.systemRecovery.petrinet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import feature.hints.Hint;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Checks the staged hint contract attached to every learning step. */
class SystemRecoveryHintCatalogTest {

  private static final String[] STAGES = {"orientation", "approach", "near", "solution"};

  @Test
  void everyLearningStepHasFourSpecificHintsAndStableStageIds() {
    Set<String> allHintIds = new HashSet<>();

    for (SystemRecoveryLearningStep step : SystemRecoveryLearningStep.values()) {
      if (!step.isLearningStep()) continue;

      Hint[] hints = SystemRecoveryHintCatalog.hints(step);
      assertEquals(STAGES.length, hints.length, step.name());
      Set<String> texts = new HashSet<>();
      for (int index = 0; index < STAGES.length; index++) {
        Hint hint = hints[index];
        assertNotNull(hint.title(), step.name());
        assertFalse(hint.title().isBlank(), step.name());
        assertNotNull(hint.text(), step.name());
        assertFalse(hint.text().isBlank(), step.name());
        assertTrue(texts.add(hint.text()), step.name() + " repeats a hint stage");
        assertEquals(index == STAGES.length - 1, hint.solution(), step.name());

        String id = SystemRecoveryHintCatalog.hintId(step, hint);
        assertEquals(step.hintKey() + ":" + STAGES[index], id);
        assertTrue(allHintIds.add(step.puzzleId().orElseThrow() + ":" + id), id);
      }
    }
    assertEquals(28 * STAGES.length, allHintIds.size());
  }

  @Test
  void hintIdRejectsAnOfferThatDoesNotBelongToTheStep() {
    Hint otherStepHint =
        SystemRecoveryHintCatalog.hints(SystemRecoveryLearningStep.ENERGY_ARRAY)[0];

    assertNotEquals(
        SystemRecoveryLearningStep.MODULE_ARRAY, SystemRecoveryLearningStep.ENERGY_ARRAY);
    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class,
        () ->
            SystemRecoveryHintCatalog.hintId(
                SystemRecoveryLearningStep.MODULE_ARRAY, otherStepHint));
  }
}
