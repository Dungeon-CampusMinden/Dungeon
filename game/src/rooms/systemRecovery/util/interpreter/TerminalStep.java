package rooms.systemRecovery.util.interpreter;

import java.util.Arrays;
import java.util.Optional;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzle;

/**
 * Named definition of every step understood by the shared System Recovery terminal.
 *
 * <p>The numeric ID remains part of the network and save-state contract. Keeping it beside the
 * owning puzzle and input mode prevents different subsystems from maintaining their own magic state
 * tables.
 */
public enum TerminalStep {
  ENERGY_ARRAY(0, SystemRecoveryPuzzle.ENERGY, "energy-array", InputMode.TERMINAL),
  ENERGY_VALUES(1, SystemRecoveryPuzzle.ENERGY, "energy-values", InputMode.TERMINAL),
  MODULE_ARRAY(2, SystemRecoveryPuzzle.MODULE_STORAGE, "module-array", InputMode.TERMINAL),
  MODULE_VALUES(3, SystemRecoveryPuzzle.MODULE_STORAGE, "module-values", InputMode.TERMINAL),
  MODULE_REMOVE_GPU(4, SystemRecoveryPuzzle.MODULE_STORAGE, "remove-gpu", InputMode.TERMINAL),
  MODULE_LENGTH(5, SystemRecoveryPuzzle.MODULE_STORAGE, "module-length", InputMode.TERMINAL),
  INVENTORY_COUNT(6, SystemRecoveryPuzzle.INVENTORY_SCANNER, "scanner-code", InputMode.TERMINAL),
  TRANSPORT_ARRAY(7, SystemRecoveryPuzzle.TRANSPORT_STORAGE, "packages-array", InputMode.TERMINAL),
  TRANSPORT_COLLECT(8, SystemRecoveryPuzzle.TRANSPORT_STORAGE, "packages-loop", InputMode.TERMINAL),
  ARCHIVE_ARRAYS(9, SystemRecoveryPuzzle.DATA_ARCHIVE, "archive-arrays", InputMode.TERMINAL),
  STORAGE_ARRAY(
      10, SystemRecoveryPuzzle.TWO_DIMENSIONAL_STORAGE, "storage-array", InputMode.TERMINAL),
  STORAGE_VALUES(
      11, SystemRecoveryPuzzle.TWO_DIMENSIONAL_STORAGE, "storage-values", InputMode.TERMINAL),
  SEARCH_PROGRAM(12, SystemRecoveryPuzzle.SEARCH_ROBOT, "search-program", InputMode.CHIP_EDITOR),
  CENTRAL_SORT(13, SystemRecoveryPuzzle.SYSTEM_CORE, "central-sort", InputMode.TERMINAL),
  CENTRAL_COUNT(14, SystemRecoveryPuzzle.SYSTEM_CORE, "central-count", InputMode.TERMINAL),
  CENTRAL_SEARCH(15, SystemRecoveryPuzzle.SYSTEM_CORE, "central-search", InputMode.TERMINAL),
  SYSTEM_CORE_META(16, SystemRecoveryPuzzle.SYSTEM_CORE, "central-meta", InputMode.FORM);

  private final int stateId;
  private final SystemRecoveryPuzzle puzzle;
  private final String descriptionKey;
  private final InputMode inputMode;

  TerminalStep(
      int stateId, SystemRecoveryPuzzle puzzle, String descriptionKey, InputMode inputMode) {
    this.stateId = stateId;
    this.puzzle = puzzle;
    this.descriptionKey = descriptionKey;
    this.inputMode = inputMode;
  }

  /**
   * @return stable state ID used by the interpreter and network snapshots
   */
  public int stateId() {
    return stateId;
  }

  /**
   * @return puzzle that owns this terminal step
   */
  public SystemRecoveryPuzzle puzzle() {
    return puzzle;
  }

  /**
   * @return stable description key for debug output and tooling
   */
  public String descriptionKey() {
    return descriptionKey;
  }

  /**
   * @return UI input mode responsible for this step
   */
  public InputMode inputMode() {
    return inputMode;
  }

  /**
   * Resolves a network state ID without falling back to a different puzzle step.
   *
   * @param stateId state ID received from the interpreter or a snapshot
   * @return matching step, or empty for an unknown ID
   */
  public static Optional<TerminalStep> fromStateId(int stateId) {
    return Arrays.stream(values()).filter(step -> step.stateId == stateId).findFirst();
  }

  /** Input surface through which a terminal step is completed. */
  public enum InputMode {
    TERMINAL,
    CHIP_EDITOR,
    FORM
  }
}
