package rooms.systemRecovery.petrinet;

import java.util.Arrays;
import java.util.Optional;
import rooms.systemRecovery.util.interpreter.TerminalStep;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzle;

/**
 * Ordered player-facing learning tasks tracked by the System Recovery Petri net.
 *
 * <p>Each task except {@link #COMPLETE} owns one stable hint key. Terminal-backed tasks declare
 * their interpreter mapping here, so callback code does not need a second state switch.
 */
public enum SystemRecoveryLearningStep {
  ENERGY_ARRAY("energy-array", "riddle1", "energy-array", TerminalStep.ENERGY_ARRAY, 0),
  ENERGY_VALUES("energy-values", "riddle1", "energy-array", TerminalStep.ENERGY_VALUES),
  ENERGY_INSERT_BATTERY("energy-insert-battery", "riddle1", "energy-array"),
  MODULE_ARRAY("module-array", "riddle2", "module-storage", TerminalStep.MODULE_ARRAY, 2),
  MODULE_VALUES("module-values", "riddle2", "module-storage", TerminalStep.MODULE_VALUES),
  MODULE_REMOVE_GPU(
      "module-remove-gpu", "riddle2", "module-storage", TerminalStep.MODULE_REMOVE_GPU),
  MODULE_LENGTH("module-length", "riddle2", "module-storage", TerminalStep.MODULE_LENGTH),
  ROOM2_DOOR_CODE("room2-door-code", "riddle2", "module-storage"),
  INVENTORY_COUNT(
      "inventory-count", "riddle3", "inventory-scanner", TerminalStep.INVENTORY_COUNT, 6),
  ROOM3_DOOR_CODE("room3-door-code", "riddle3", "inventory-scanner"),
  TRANSPORT_ARRAY(
      "transport-array", "riddle4", "transport-storage", TerminalStep.TRANSPORT_ARRAY, 7),
  TRANSPORT_COLLECT_LOOP(
      "transport-collect-loop", "riddle4", "transport-storage", TerminalStep.TRANSPORT_COLLECT),
  DATA_STORAGE_DOOR_OPEN("data-storage-door-open", "riddle4", "transport-storage"),
  MANUAL_SORTING("manual-sorting", "riddle5", "manual-sorting", 9),
  BUBBLE_SORT_CONDITION("bubble-sort-condition", "riddle6", "bubble-sort", 9),
  BUBBLE_SORT_MACHINE("bubble-sort-machine", "riddle6", "bubble-sort"),
  ARCHIVE_ACCESS("archive-access", "riddle7", "data-archive", 9),
  ARCHIVE_ARRAYS("archive-arrays", "riddle7", "data-archive", TerminalStep.ARCHIVE_ARRAYS),
  STORAGE_ARRAY(
      "storage-array", "riddle8", "two-dimensional-storage", TerminalStep.STORAGE_ARRAY, 10),
  STORAGE_VALUES(
      "storage-values", "riddle8", "two-dimensional-storage", TerminalStep.STORAGE_VALUES),
  SEARCH_PROGRAM("search-program", "riddle9", "search-robot", TerminalStep.SEARCH_PROGRAM, 12),
  SEARCH_ROBOT_RUN("search-robot-run", "riddle9", "search-robot"),
  SYSTEM_CORE_ACCESS("system-core-access", "riddle10", "system-core", 12),
  CORE_SORT("core-sort", "riddle10", "system-core", TerminalStep.CENTRAL_SORT),
  CORE_COUNT("core-count", "riddle10", "system-core", TerminalStep.CENTRAL_COUNT),
  CORE_SEARCH("core-search", "riddle10", "system-core", TerminalStep.CENTRAL_SEARCH),
  CORE_SEARCH_ROBOT("core-search-robot", "riddle10", "system-core"),
  CORE_META("core-meta", "riddle10", "system-core", TerminalStep.SYSTEM_CORE_META),
  COMPLETE("complete", null, null);

  private final String hintKey;
  private final String riddleKey;
  private final String puzzleId;
  private final TerminalStep terminalStep;
  private final int acceptedTerminalInputCount;

  SystemRecoveryLearningStep(String hintKey, String riddleKey, String puzzleId) {
    this(hintKey, riddleKey, puzzleId, null, -1);
  }

  SystemRecoveryLearningStep(
      String hintKey, String riddleKey, String puzzleId, int acceptedTerminalInputCount) {
    this(hintKey, riddleKey, puzzleId, null, acceptedTerminalInputCount);
  }

  SystemRecoveryLearningStep(
      String hintKey, String riddleKey, String puzzleId, TerminalStep terminalStep) {
    this(hintKey, riddleKey, puzzleId, terminalStep, -1);
  }

  SystemRecoveryLearningStep(
      String hintKey,
      String riddleKey,
      String puzzleId,
      TerminalStep terminalStep,
      int acceptedTerminalInputCount) {
    this.hintKey = hintKey;
    this.riddleKey = riddleKey;
    this.puzzleId = puzzleId;
    this.terminalStep = terminalStep;
    this.acceptedTerminalInputCount = acceptedTerminalInputCount;
  }

  /**
   * @return stable lowercase key for hints, tracking, questlog, and debug output
   */
  public String hintKey() {
    return hintKey;
  }

  /**
   * @return questlog tab key associated with this task, or {@code null} for {@link #COMPLETE}
   */
  public String riddleKey() {
    return riddleKey;
  }

  /**
   * @return stable puzzle identifier used by Dungeon tracking
   */
  public Optional<String> puzzleId() {
    return Optional.ofNullable(puzzleId);
  }

  /**
   * @return owning puzzle, or empty for {@link #COMPLETE}
   */
  public Optional<SystemRecoveryPuzzle> puzzle() {
    if (puzzleId == null) return Optional.empty();
    return Arrays.stream(SystemRecoveryPuzzle.values())
        .filter(puzzle -> puzzle.id().equals(puzzleId))
        .findFirst();
  }

  /**
   * @return interpreter step that completes this task, or empty for physical tasks
   */
  public Optional<TerminalStep> terminalStep() {
    return Optional.ofNullable(terminalStep);
  }

  /**
   * Returns the number of accepted terminal inputs that must exist at this main-riddle checkpoint.
   *
   * @return accepted input count, or {@code -1} for non-checkpoint steps
   */
  public int acceptedTerminalInputCount() {
    return acceptedTerminalInputCount;
  }

  /**
   * @return whether this enum value represents one of the 28 hint-bearing tasks
   */
  public boolean isLearningStep() {
    return this != COMPLETE;
  }

  /**
   * Resolves the unique learning task completed by an accepted terminal or chip-editor input.
   *
   * @param terminalStep accepted interpreter step
   * @return matching task, or empty if there is no mapping
   */
  public static Optional<SystemRecoveryLearningStep> fromTerminalStep(TerminalStep terminalStep) {
    return Arrays.stream(values()).filter(step -> step.terminalStep == terminalStep).findFirst();
  }
}
