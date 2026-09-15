package rooms.systemRecovery.util.interpreter;

import engine.Game;
import engine.sound.SoundSpec;
import feature.hud.DialogUtils;
import java.util.function.Supplier;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.story.SystemRecoveryStoryDialogs;
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
  private static final ThreadLocal<TerminalAttempt> CURRENT_ATTEMPT = new ThreadLocal<>();

  private InterpretationCallbacks() {}

  /**
   * Attaches the authoritative terminal state and submitted source to callbacks invoked by the
   * generic interpreter. The interpreter itself remains independent from room tracking.
   */
  public static <T> T withTerminalAttempt(int state, String source, Supplier<T> action) {
    TerminalAttempt previous = CURRENT_ATTEMPT.get();
    CURRENT_ATTEMPT.set(new TerminalAttempt(state, source));
    try {
      return action.get();
    } finally {
      if (previous == null) CURRENT_ATTEMPT.remove();
      else CURRENT_ATTEMPT.set(previous);
    }
  }

  /** Handles riddle 1 step 1: initialize the energy array. */
  public static void onRiddleOneStepOneEnergyArrayInitialized() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.spawnEnergyCrates();
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.ENERGY_VALUES);
  }

  /** Handles riddle 1 step 2: set all energy values. */
  public static void onRiddleOneStepTwoEnergyValuesSet() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.completeEnergyPuzzle();
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.ENERGY_BATTERY);
  }

  /** Handles riddle 2 step 1: initialize the module array. */
  public static void onRiddleTwoStepOneModuleArrayInitialized() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.activateModuleSockets();
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.MODULE_VALUES);
  }

  /** Handles riddle 2 step 2: assign all modules. */
  public static void onRiddleTwoStepTwoModulesAssigned() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.spawnModuleChips();
  }

  /** Handles riddle 2 step 3: remove the GPU module. */
  public static void onRiddleTwoStepThreeGpuRemoved() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.removeGpuChip();
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.READ_MODULE_LENGTH);
  }

  /** Handles riddle 2 step 4: read the module array length. */
  public static void onRiddleTwoStepFourModuleLengthRead() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.showModuleArrayLength();
    SystemRecoveryLevel.portModuleChipsToScanner();
  }

  /** Handles riddle 3 step 1: count all non-null modules. */
  public static void onRiddleThreeStepOneInventoryScannerCompleted() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.completeScannerPuzzle();
    SystemRecoveryLevel.announceStoryToAllPlayers(SystemRecoveryStoryDialogs.SCANNER_LEVER);
  }

  /** Handles riddle 4 step 1: create the packages array. */
  public static void onRiddleFourStepOnePackagesCreated() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.spawnTransportPackages();
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.PACKAGES_LOOP);
  }

  /** Handles riddle 4 step 2: let the conveyor scanner collect packages in array order. */
  public static void onRiddleFourStepTwoPackagesCollected() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.startTransportSequence();
  }

  /** Handles riddle 7 step 1: create all archive arrays. */
  public static void onRiddleSevenStepOneDataArchiveLoaded() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.completeDataArchive();
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.STORAGE_UNLOCKED);
  }

  /** Handles riddle 8 step 1: create the two-dimensional storage array. */
  public static void onRiddleEightStepOneStorageCreated() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.activateStorageMatrix();
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.STORAGE_VALUES);
  }

  /** Handles riddle 8 step 2: fill the two-dimensional storage array. */
  public static void onRiddleEightStepTwoStorageFilled() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.fillStorageMatrix();
    SystemRecoveryLevel.completeStorageMatrix();
  }

  /** Handles riddle 9 step 1: search the map and collect batteries. */
  public static void onRiddleNineStepOneSearchRobotCompleted() {
    showCorrectTerminalInputDialog();
    // TODO: Suchroboter: Diese Legacy-Terminalroute sollte langfristig durch den Ortungschip-
    // Controller ersetzt werden.
  }

  /** Handles riddle 10 step 1: bubble sort the central array. */
  public static void onRiddleTenStepOneBubbleSortCompleted() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.completeSystemCoreSort();
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.CENTRAL_COUNT);
  }

  /** Handles riddle 10 step 2: count all non-null modules. */
  public static void onRiddleTenStepTwoModulesCounted() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.completeSystemCoreModuleCount();
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.CENTRAL_SEARCH);
  }

  /** Handles riddle 10 step 3: search the map and collect batteries. */
  public static void onRiddleTenStepThreeBatteriesCollected() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.completeSystemCoreMapSearch();
    SystemRecoveryLevel.completeSystemCoreRiddle();
    SystemRecoveryLevel.announceStoryCompletion();
  }

  /** Handles any terminal input that does not match the current requirement. */
  public static void onIncorrectTerminalInput() {
    showIncorrectTerminalInputDialog();
  }

  /** Shows feedback for a correct terminal input. */
  public static void showCorrectTerminalInputDialog() {
    track(true);
    DialogUtils.showTextPopup(
        SystemRecoveryText.text("computer.feedback-correct"),
        SystemRecoveryText.text("computer.terminal"));
    Game.audio().playGlobal(SoundSpec.builder(SUCCESS_SOUND));
  }

  /** Shows feedback for an incorrect terminal input. */
  public static void showIncorrectTerminalInputDialog() {
    track(false);
    DialogUtils.showTextPopup(
        SystemRecoveryText.text("computer.feedback-incorrect"),
        SystemRecoveryText.text("computer.terminal"));
    Game.audio().playGlobal(SoundSpec.builder(FAILURE_SOUND));
  }

  /** Writes the current terminal state and complete submitted source to the room outbox. */
  private static void track(boolean correct) {
    TerminalAttempt attempt = CURRENT_ATTEMPT.get();
    if (attempt != null) {
      track(
          attempt.state(),
          attempt.source(),
          correct,
          SystemRecoveryStoryDialogs.currentTerminalPlayer().orElse(-1));
    }
  }

  /** Records one terminal result with the state and complete source submitted by the player. */
  private static void track(int state, String input, boolean correct, int playerId) {
    SystemRecoveryPuzzleEvents.terminalAttempt(state, input, correct, playerId);
  }

  private record TerminalAttempt(int state, String source) {}
}
