package rooms.systemRecovery.util.interpreter;

import engine.Game;
import engine.sound.SoundSpec;
import feature.hud.DialogUtils;
import rooms.systemRecovery.level.SystemRecoveryLevel;
import rooms.systemRecovery.story.SystemRecoveryStoryDialogs;

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
        SystemRecoveryStoryDialogs.MODULE_ARRAY);
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
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.REMOVE_GPU);
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
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.SCANNER_CODE);
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
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.STORAGE_ARRAY);
    // TODO: Datenarchiv: Archivdaten anzeigen/freischalten.
  }

  /** Handles riddle 8 step 1: create the two-dimensional storage array. */
  public static void onRiddleEightStepOneStorageCreated() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.STORAGE_VALUES);
    // TODO: Zweidimensionales Lager: Raster sichtbar initialisieren.
  }

  /** Handles riddle 8 step 2: fill the two-dimensional storage array. */
  public static void onRiddleEightStepTwoStorageFilled() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.STORAGE_READ);
    // TODO: Zweidimensionales Lager: Werte im Raster platzieren.
  }

  /** Handles riddle 8 step 3: read a two-dimensional storage cell. */
  public static void onRiddleEightStepThreeStorageRead() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.SEARCH_ROBOT);
    // TODO: Zweidimensionales Lager: gelesene Zelle hervorheben/auswerten.
  }

  /** Handles riddle 9 step 1: search the map and collect batteries. */
  public static void onRiddleNineStepOneSearchRobotCompleted() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.CENTRAL_SORT);
    // TODO: Suchroboter: Batterien auf der Map einsammeln und ggf. Batterie-Items spawnen.
  }

  /** Handles riddle 10 step 1: bubble sort the central array. */
  public static void onRiddleTenStepOneBubbleSortCompleted() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.CENTRAL_COUNT);
    // TODO: Zentrales Rechenzentrum: sortierte Werte an Maschine/Rechenzentrum melden.
  }

  /** Handles riddle 10 step 2: count all non-null modules. */
  public static void onRiddleTenStepTwoModulesCounted() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.announceStoryForCurrentTerminalPlayer(
        SystemRecoveryStoryDialogs.CENTRAL_SEARCH);
    // TODO: Zentrales Rechenzentrum: Modulanzahl in Zentralstatus uebernehmen.
  }

  /** Handles riddle 10 step 3: search the map and collect batteries. */
  public static void onRiddleTenStepThreeBatteriesCollected() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.announceStoryCompletion();
    // TODO: Zentrales Rechenzentrum: finale Batterie-Suche bestaetigen und naechste Aktion
    // ausloesen.
  }

  /** Handles any terminal input that does not match the current requirement. */
  public static void onIncorrectTerminalInput() {
    showIncorrectTerminalInputDialog();
  }

  /** Shows feedback for a correct terminal input. */
  public static void showCorrectTerminalInputDialog() {
    DialogUtils.showTextPopup("War richtig", "Terminal");
    Game.audio().playGlobal(SoundSpec.builder(SUCCESS_SOUND));
  }

  /** Shows feedback for an incorrect terminal input. */
  public static void showIncorrectTerminalInputDialog() {
    DialogUtils.showTextPopup("war falsch", "Terminal");
    Game.audio().playGlobal(SoundSpec.builder(FAILURE_SOUND));
  }
}
