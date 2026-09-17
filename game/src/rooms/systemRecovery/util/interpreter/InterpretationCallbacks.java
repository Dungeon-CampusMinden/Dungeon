package rooms.systemRecovery.util.interpreter;

import engine.Game;
import engine.sound.SoundSpec;
import feature.hud.DialogUtils;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.modules.interpreter.TerminalAttempt;
import rooms.systemRecovery.util.SystemRecoveryText;
import rooms.systemRecovery.util.tracking.SystemRecoveryPuzzleEvents;

/**
 * Maps accepted terminal steps to their room effects in gameplay order.
 *
 * <p>This class owns feedback, not puzzle state or entities. Implemented effects live with their
 * setup in {@code rooms.systemRecovery.riddles}; the level forwards each call to its owning riddle.
 * The generic interpreter package must not depend on this class.
 */
public final class InterpretationCallbacks {

  private static final String SUCCESS_SOUND = "retro_event_correct";
  private static final String FAILURE_SOUND = "retro_event_wrong";

  private InterpretationCallbacks() {}

  /**
   * Handles riddle 1 step 1: initialize the energy array.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleOneStepOneEnergyArrayInitialized(TerminalAttempt attempt) {
    showCorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.ENERGY_ARRAY, attempt);
  }

  /**
   * Handles riddle 1 step 2: set all energy values.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleOneStepTwoEnergyValuesSet(TerminalAttempt attempt) {
    showCorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.ENERGY_VALUES, attempt);
  }

  /**
   * Handles riddle 2 step 1: initialize the module array.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleTwoStepOneModuleArrayInitialized(TerminalAttempt attempt) {
    showCorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.MODULE_ARRAY, attempt);
  }

  /**
   * Handles riddle 2 step 2: assign all modules.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleTwoStepTwoModulesAssigned(TerminalAttempt attempt) {
    showCorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.MODULE_VALUES, attempt);
  }

  /**
   * Handles riddle 2 step 3: remove the GPU module.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleTwoStepThreeGpuRemoved(TerminalAttempt attempt) {
    showCorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.MODULE_REMOVE_GPU, attempt);
  }

  /**
   * Handles riddle 2 step 4: read the module array length.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleTwoStepFourModuleLengthRead(TerminalAttempt attempt) {
    showCorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.MODULE_LENGTH, attempt);
  }

  /**
   * Handles riddle 3 step 1: count all non-null modules.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleThreeStepOneInventoryScannerCompleted(TerminalAttempt attempt) {
    showCorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.INVENTORY_COUNT, attempt);
  }

  /**
   * Handles riddle 4 step 1: create the packages array.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleFourStepOnePackagesCreated(TerminalAttempt attempt) {
    showCorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.TRANSPORT_ARRAY, attempt);
  }

  /**
   * Handles riddle 4 step 2: let the conveyor scanner collect packages in array order.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleFourStepTwoPackagesCollected(TerminalAttempt attempt) {
    showCorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.TRANSPORT_COLLECT, attempt);
  }

  /**
   * Handles riddle 7 step 1: create all archive arrays.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleSevenStepOneDataArchiveLoaded(TerminalAttempt attempt) {
    showCorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.ARCHIVE_ARRAYS, attempt);
  }

  /**
   * Handles riddle 8 step 1: create the two-dimensional storage array.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleEightStepOneStorageCreated(TerminalAttempt attempt) {
    showCorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.STORAGE_ARRAY, attempt);
  }

  /**
   * Handles riddle 8 step 2: fill the two-dimensional storage array.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleEightStepTwoStorageFilled(TerminalAttempt attempt) {
    showCorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.STORAGE_VALUES, attempt);
  }

  /**
   * Handles riddle 10 step 1: bubble sort the central array.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleTenStepOneBubbleSortCompleted(TerminalAttempt attempt) {
    showCorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.CENTRAL_SORT, attempt);
  }

  /**
   * Handles riddle 10 step 2: count all non-null modules.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleTenStepTwoModulesCounted(TerminalAttempt attempt) {
    showCorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.CENTRAL_COUNT, attempt);
  }

  /**
   * Handles riddle 10 step 3: search the map and collect batteries.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleTenStepThreeBatteriesCollected(TerminalAttempt attempt) {
    showCorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.CENTRAL_SEARCH, attempt);
  }

  /**
   * Handles riddle 10 meta step: combine the three results shown by the central display.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleTenMetaCombinationCompleted(TerminalAttempt attempt) {
    showCorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.SYSTEM_CORE_META, attempt);
  }

  /**
   * Handles any terminal input that does not match the current requirement.
   *
   * @param attempt rejected terminal attempt
   */
  public static void onIncorrectTerminalInput(TerminalAttempt attempt) {
    showIncorrectTerminalInputDialog(attempt);
    SystemRecoveryLevel.triggerEchoCallForIncorrectInput();
  }

  /**
   * Shows feedback for a correct terminal input.
   *
   * @param attempt accepted terminal attempt
   */
  public static void showCorrectTerminalInputDialog(TerminalAttempt attempt) {
    track(attempt, true);
    DialogUtils.showTextPopup(
        SystemRecoveryText.key("computer.feedback-correct"),
        SystemRecoveryText.key("computer.terminal"),
        attempt.playerId());
    Game.audio().playGlobal(SoundSpec.builder(SUCCESS_SOUND));
  }

  /**
   * Shows feedback for an incorrect terminal input.
   *
   * @param attempt rejected terminal attempt
   */
  public static void showIncorrectTerminalInputDialog(TerminalAttempt attempt) {
    track(attempt, false);
    DialogUtils.showTextPopup(
        SystemRecoveryText.key("computer.feedback-incorrect"),
        SystemRecoveryText.key("computer.terminal"),
        attempt.playerId());
    Game.audio().playGlobal(SoundSpec.builder(FAILURE_SOUND));
  }

  /**
   * Writes the current terminal state and complete submitted source to the room outbox.
   *
   * @param attempt terminal attempt containing state, source and player context
   * @param correct whether the source was accepted
   */
  private static void track(TerminalAttempt attempt, boolean correct) {
    SystemRecoveryPuzzleEvents.terminalAttempt(
        attempt.state(), attempt.source(), correct, attempt.playerId());
  }
}
