package rooms.systemRecovery.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.modules.interpreter.TerminalAttempt;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.util.interpreter.TerminalStep;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzle;

/** Verifies run-local System Recovery achievement rules without requiring the game runtime. */
class SystemRecoveryAchievementTrackerTest {

  private List<String> unlocked;
  private SystemRecoveryAchievementTracker tracker;

  @BeforeEach
  void setUp() {
    unlocked = new ArrayList<>();
    tracker = new SystemRecoveryAchievementTracker(unlocked::add);
    tracker.reset(false);
  }

  @Test
  void firstCorrectEnergySubmissionUnlocksFirstTry() {
    tracker.terminalAttempt(
        new TerminalAttempt(TerminalStep.ENERGY_ARRAY.stateId(), "int[] energie = new int[5];", 1),
        true);

    assertEquals(List.of(SystemRecoveryAchievements.DIRECT_FIRST_TRY), unlocked);
  }

  @Test
  void wrongTerminalThresholdsAreEachEmittedOnce() {
    TerminalAttempt wrong = new TerminalAttempt(TerminalStep.ENERGY_ARRAY.stateId(), "wrong", 1);
    for (int index = 0; index < 10; index++) {
      tracker.terminalAttempt(wrong, false);
    }

    assertTrue(unlocked.contains(SystemRecoveryAchievements.INTENTIONAL_FAILURE));
    assertTrue(unlocked.contains(SystemRecoveryAchievements.SYNTAX_PERSONALITY));
    assertEquals(2, unlocked.size());
  }

  @Test
  void solutionHintPreventsNoHintAchievementButTracksHintMilestones() {
    tracker.hintUsed(SystemRecoveryPuzzle.ENERGY, "energy-array:solution");
    tracker.hintUsed(SystemRecoveryPuzzle.MODULE_STORAGE, "module-array:near");
    tracker.hintUsed(SystemRecoveryPuzzle.INVENTORY_SCANNER, "inventory-count:orientation");
    tracker.puzzleSolved(SystemRecoveryPuzzle.ENERGY);

    assertTrue(unlocked.contains(SystemRecoveryAchievements.JUST_ASKING));
    assertTrue(unlocked.contains(SystemRecoveryAchievements.ECHO_STILL_THERE));
    assertTrue(!unlocked.contains(SystemRecoveryAchievements.NO_FINISHED_SOLUTION));
  }

  @Test
  void loopAndMatrixAchievementsRequireTheirSpecificConditions() {
    tracker.puzzleSolved(SystemRecoveryPuzzle.INVENTORY_SCANNER);
    tracker.puzzleSolved(SystemRecoveryPuzzle.TRANSPORT_STORAGE);
    tracker.puzzleSolved(SystemRecoveryPuzzle.TWO_DIMENSIONAL_STORAGE);

    assertTrue(unlocked.contains(SystemRecoveryAchievements.LOOP_UNDERSTANDING));
    assertTrue(unlocked.contains(SystemRecoveryAchievements.NO_LOST_INDEX));
    assertTrue(unlocked.contains(SystemRecoveryAchievements.EVERY_PACKAGE_COUNTS));
  }

  @Test
  void failedUploadDisqualifiesCleanUploadButSuccessfulUploadStillCounts() {
    tracker.chipUploadAttempt("sort", false);
    tracker.chipUploadAttempt("sort", true);
    tracker.chipUploadAttempt("search", true);

    assertTrue(unlocked.contains(SystemRecoveryAchievements.NOT_FOR_CONSUMPTION));
    assertTrue(!unlocked.contains(SystemRecoveryAchievements.CLEAN_UPLOAD));
  }

  @Test
  void finalCompletionStillGrantsAchievementsInDebugRun() {
    tracker.puzzleSolved(SystemRecoveryPuzzle.SYSTEM_CORE);
    assertTrue(unlocked.contains(SystemRecoveryAchievements.FREE_VARIABLE));
    assertTrue(unlocked.contains(SystemRecoveryAchievements.DEBUG_IS_NOT_GAMEPLAY));

    unlocked.clear();
    tracker.reset(true);
    tracker.puzzleSolved(SystemRecoveryPuzzle.SYSTEM_CORE);
    assertTrue(unlocked.contains(SystemRecoveryAchievements.FREE_VARIABLE));
    assertTrue(!unlocked.contains(SystemRecoveryAchievements.DEBUG_IS_NOT_GAMEPLAY));
  }

  @Test
  void progressSpecificAchievementsFollowLearningSteps() {
    tracker.learningStepCompleted(SystemRecoveryLearningStep.ARCHIVE_ACCESS);
    tracker.learningStepCompleted(SystemRecoveryLearningStep.SEARCH_PROGRAM);
    tracker.learningStepCompleted(SystemRecoveryLearningStep.SYSTEM_CORE_ACCESS);

    assertTrue(unlocked.contains(SystemRecoveryAchievements.ARCHIVE_ACCESS));
    assertTrue(unlocked.contains(SystemRecoveryAchievements.LOCATOR_ESTABLISHED));
    assertTrue(unlocked.contains(SystemRecoveryAchievements.AXIOM_RELAX));
  }
}
