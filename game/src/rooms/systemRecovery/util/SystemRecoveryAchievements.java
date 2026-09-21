package rooms.systemRecovery.util;

import feature.achievements.AchievementManager;
import rooms.systemRecovery.modules.interpreter.TerminalAttempt;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzle;

/** Achievement definitions and authoritative trigger hooks for the System Recovery room. */
public final class SystemRecoveryAchievements {

  /** Internal achievement definition file. */
  public static final String DEFINITION_PATH = "systemRecovery-achievement.json";

  /** Runtime file containing locally recorded unlocks. */
  public static final String STATUS_PATH = "system-recovery-achievement-unlock.json";

  public static final String INITIALIZATION_COMPLETE = "system_initialization";
  public static final String MODULAR_THINKING = "modular_thinking";
  public static final String COUNT_WHAT_EXISTS = "count_what_exists";
  public static final String EVERY_PACKAGE_COUNTS = "every_package_counts";
  public static final String SORTED_IS_SORTED = "sorted_is_sorted";
  public static final String ARCHIVE_ACCESS = "archive_access";
  public static final String TWO_DIMENSIONAL_THINKING = "two_dimensional_thinking";
  public static final String LOCATOR_ESTABLISHED = "locator_established";
  public static final String SEARCH_COMPLETE = "search_complete";
  public static final String THREE_RESULTS_ONE_CORE = "three_results_one_core";
  public static final String FREE_VARIABLE = "free_variable";
  public static final String DIRECT_FIRST_TRY = "direct_first_try";
  public static final String NO_FINISHED_SOLUTION = "no_finished_solution";
  public static final String EVERY_CELL_COUNTS = "every_cell_counts";
  public static final String LOOP_UNDERSTANDING = "loop_understanding";
  public static final String CLEAN_UPLOAD = "clean_upload";
  public static final String NO_LOST_INDEX = "no_lost_index";
  public static final String INTENTIONAL_FAILURE = "intentional_failure";
  public static final String SYNTAX_PERSONALITY = "syntax_personality";
  public static final String JUST_ASKING = "just_asking";
  public static final String ECHO_STILL_THERE = "echo_still_there";
  public static final String NOT_FOR_CONSUMPTION = "not_for_consumption";
  public static final String AXIOM_RELAX = "axiom_relax";
  public static final String NOT_A_BUG = "not_a_bug";
  public static final String DEBUG_IS_NOT_GAMEPLAY = "debug_is_not_gameplay";

  private static SystemRecoveryAchievementTracker tracker =
      new SystemRecoveryAchievementTracker(SystemRecoveryAchievements::unlock);

  private SystemRecoveryAchievements() {}

  /** Registers definitions for both the authoritative server and connected clients. */
  public static void register() {
    AchievementManager.registerAchievements(DEFINITION_PATH, STATUS_PATH);
  }

  /**
   * Clears only run-local conditions; persistent unlocked achievements remain untouched.
   *
   * @param debugRun whether the current room was started with debug controls enabled
   */
  public static synchronized void resetRun(boolean debugRun) {
    tracker.reset(debugRun);
  }

  /**
   * Records an accepted or rejected terminal attempt.
   *
   * @param attempt submitted terminal source and state
   * @param correct whether the authoritative interpreter accepted the source
   */
  public static synchronized void terminalAttempt(TerminalAttempt attempt, boolean correct) {
    tracker.terminalAttempt(attempt, correct);
  }

  /**
   * Records an unsuccessful physical interaction.
   *
   * @param puzzle puzzle receiving the interaction
   * @param input interaction payload
   * @param correct whether the authoritative interaction succeeded
   * @param playerId acting player ID
   */
  public static synchronized void physicalAttempt(
      SystemRecoveryPuzzle puzzle, String input, boolean correct, int playerId) {
    tracker.physicalAttempt(puzzle, input, correct, playerId);
  }

  /**
   * Records one accepted telephone hint.
   *
   * @param puzzle puzzle whose hint was accepted
   * @param hintId stable hint identifier
   */
  public static synchronized void hintUsed(SystemRecoveryPuzzle puzzle, String hintId) {
    tracker.hintUsed(puzzle, hintId);
  }

  /**
   * Records the first completion of a puzzle.
   *
   * @param puzzle completed puzzle
   */
  public static synchronized void puzzleSolved(SystemRecoveryPuzzle puzzle) {
    tracker.puzzleSolved(puzzle);
  }

  /**
   * Records a chip-program upload attempt.
   *
   * @param chipType stable chip type
   * @param correct whether the program was accepted and written
   */
  public static synchronized void chipUploadAttempt(String chipType, boolean correct) {
    tracker.chipUploadAttempt(chipType, correct);
  }

  /**
   * Records a completed progress-net step.
   *
   * @param step completed learning step
   */
  public static synchronized void learningStepCompleted(SystemRecoveryLearningStep step) {
    tracker.learningStepCompleted(step);
  }

  /**
   * Sends a globally shared unlock popup for one achievement ID.
   *
   * @param achievementId stable achievement ID
   */
  static void unlock(String achievementId) {
    AchievementManager.instance().pop(achievementId);
  }
}
