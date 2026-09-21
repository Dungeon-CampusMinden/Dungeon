package rooms.systemRecovery.util.interpreter;

import engine.Game;
import engine.sound.SoundSpec;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.modules.interpreter.TerminalAttempt;
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
    SystemRecoveryLevel.recordInitialTerminalAttempt(true);
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.ENERGY_ARRAY, attempt);
    showCorrectTerminalFeedback(attempt);
  }

  /**
   * Handles riddle 1 step 2: set all energy values.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleOneStepTwoEnergyValuesSet(TerminalAttempt attempt) {
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.ENERGY_VALUES, attempt);
    showCorrectTerminalFeedback(attempt);
  }

  /**
   * Handles riddle 2 step 1: initialize the module array.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleTwoStepOneModuleArrayInitialized(TerminalAttempt attempt) {
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.MODULE_ARRAY, attempt);
    showCorrectTerminalFeedback(attempt);
  }

  /**
   * Handles riddle 2 step 2: assign all modules.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleTwoStepTwoModulesAssigned(TerminalAttempt attempt) {
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.MODULE_VALUES, attempt);
    showCorrectTerminalFeedback(attempt);
  }

  /**
   * Handles riddle 2 step 3: remove the GPU module.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleTwoStepThreeGpuRemoved(TerminalAttempt attempt) {
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.MODULE_REMOVE_GPU, attempt);
    showCorrectTerminalFeedback(attempt);
  }

  /**
   * Handles riddle 2 step 4: read the module array length.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleTwoStepFourModuleLengthRead(TerminalAttempt attempt) {
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.MODULE_LENGTH, attempt);
    showCorrectTerminalFeedback(attempt);
  }

  /**
   * Handles riddle 3 step 1: count all non-null modules.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleThreeStepOneInventoryScannerCompleted(TerminalAttempt attempt) {
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.INVENTORY_COUNT, attempt);
    showCorrectTerminalFeedback(attempt);
  }

  /**
   * Handles riddle 4 step 1: create the packages array.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleFourStepOnePackagesCreated(TerminalAttempt attempt) {
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.TRANSPORT_ARRAY, attempt);
    showCorrectTerminalFeedback(attempt);
  }

  /**
   * Handles riddle 4 step 2: let the conveyor scanner collect packages in array order.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleFourStepTwoPackagesCollected(TerminalAttempt attempt) {
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.TRANSPORT_COLLECT, attempt);
    showCorrectTerminalFeedback(attempt);
  }

  /**
   * Handles riddle 7 step 1: create all archive arrays.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleSevenStepOneDataArchiveLoaded(TerminalAttempt attempt) {
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.ARCHIVE_ARRAYS, attempt);
    showCorrectTerminalFeedback(attempt);
  }

  /**
   * Handles riddle 8 step 1: create the two-dimensional storage array.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleEightStepOneStorageCreated(TerminalAttempt attempt) {
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.STORAGE_ARRAY, attempt);
    showCorrectTerminalFeedback(attempt);
  }

  /**
   * Handles riddle 8 step 2: fill the two-dimensional storage array.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleEightStepTwoStorageFilled(TerminalAttempt attempt) {
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.STORAGE_VALUES, attempt);
    showCorrectTerminalFeedback(attempt);
  }

  /**
   * Handles riddle 10 step 1: bubble sort the central array.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleTenStepOneBubbleSortCompleted(TerminalAttempt attempt) {
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.CENTRAL_SORT, attempt);
    showCorrectTerminalFeedback(attempt);
  }

  /**
   * Handles riddle 10 step 2: count all non-null modules.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleTenStepTwoModulesCounted(TerminalAttempt attempt) {
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.CENTRAL_COUNT, attempt);
    showCorrectTerminalFeedback(attempt);
  }

  /**
   * Handles riddle 10 step 3: search the map and collect every module.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleTenStepThreeModulesCollected(TerminalAttempt attempt) {
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.CENTRAL_SEARCH, attempt);
    showCorrectTerminalFeedback(attempt);
  }

  /**
   * Handles riddle 10 meta step: combine the three results shown by the central display.
   *
   * @param attempt submitted terminal attempt
   */
  public static void onRiddleTenMetaCombinationCompleted(TerminalAttempt attempt) {
    SystemRecoveryLevel.applyTerminalStep(TerminalStep.SYSTEM_CORE_META, attempt);
    showCorrectTerminalFeedback(attempt);
  }

  /**
   * Handles any terminal input that does not match the current requirement.
   *
   * @param attempt rejected terminal attempt
   */
  public static void onIncorrectTerminalInput(TerminalAttempt attempt) {
    showIncorrectTerminalFeedback(attempt);
    SystemRecoveryLevel.triggerEchoCallForIncorrectInput();
  }

  /**
   * Sends feedback for a correct terminal input to the submitting terminal.
   *
   * @param attempt accepted terminal attempt
   */
  public static void showCorrectTerminalFeedback(TerminalAttempt attempt) {
    SystemRecoveryLevel.recordAcceptedSolution(attempt);
    track(attempt, true);
    SystemRecoveryTerminalFeedback.send(attempt, true);
    Game.audio().playGlobal(SoundSpec.builder(SUCCESS_SOUND));
  }

  /**
   * Sends feedback for an incorrect terminal input to the submitting terminal.
   *
   * @param attempt rejected terminal attempt
   */
  public static void showIncorrectTerminalFeedback(TerminalAttempt attempt) {
    track(attempt, false);
    SystemRecoveryTerminalFeedback.send(attempt, false);
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
