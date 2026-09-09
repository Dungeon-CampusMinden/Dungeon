package rooms.systemRecovery.util.interpreter;

import engine.Game;
import engine.sound.SoundSpec;
import feature.hud.DialogUtils;
import rooms.systemRecovery.level.SystemRecoveryLevel;

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
  }

  /** Handles riddle 1 step 2: set all energy values. */
  public static void onRiddleOneStepTwoEnergyValuesSet() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.completeEnergyPuzzle();
  }

  /** Handles riddle 2 step 1: initialize the module array. */
  public static void onRiddleTwoStepOneModuleArrayInitialized() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.activateModuleSockets();
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
  }

  /** Handles riddle 4 step 1: create the packages array. */
  public static void onRiddleFourStepOnePackagesCreated() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.spawnTransportPackages();
  }

  /** Handles riddle 4 step 2: let the conveyor scanner collect packages in array order. */
  public static void onRiddleFourStepTwoPackagesCollected() {
    showCorrectTerminalInputDialog();
    SystemRecoveryLevel.startTransportSequence();
  }

  /** Handles riddle 7 step 1: create all archive arrays. */
  public static void onRiddleSevenStepOneDataArchiveLoaded() {
    showCorrectTerminalInputDialog();
    // TODO: Datenarchiv: Archivdaten anzeigen/freischalten.
  }

  /** Handles riddle 8 step 1: create the two-dimensional storage array. */
  public static void onRiddleEightStepOneStorageCreated() {
    showCorrectTerminalInputDialog();
    // TODO: Zweidimensionales Lager: Raster sichtbar initialisieren.
  }

  /** Handles riddle 8 step 2: fill the two-dimensional storage array. */
  public static void onRiddleEightStepTwoStorageFilled() {
    showCorrectTerminalInputDialog();
    // TODO: Zweidimensionales Lager: Werte im Raster platzieren.
  }

  /** Handles riddle 8 step 3: read a two-dimensional storage cell. */
  public static void onRiddleEightStepThreeStorageRead() {
    showCorrectTerminalInputDialog();
    // TODO: Zweidimensionales Lager: gelesene Zelle hervorheben/auswerten.
  }

  /** Handles riddle 9 step 1: search the map and collect batteries. */
  public static void onRiddleNineStepOneSearchRobotCompleted() {
    showCorrectTerminalInputDialog();
    // TODO: Suchroboter: Batterien auf der Map einsammeln und ggf. Batterie-Items spawnen.
  }

  /** Handles riddle 10 step 1: bubble sort the central array. */
  public static void onRiddleTenStepOneBubbleSortCompleted() {
    showCorrectTerminalInputDialog();
    // TODO: Zentrales Rechenzentrum: sortierte Werte an Maschine/Rechenzentrum melden.
  }

  /** Handles riddle 10 step 2: count all non-null modules. */
  public static void onRiddleTenStepTwoModulesCounted() {
    showCorrectTerminalInputDialog();
    // TODO: Zentrales Rechenzentrum: Modulanzahl in Zentralstatus uebernehmen.
  }

  /** Handles riddle 10 step 3: search the map and collect batteries. */
  public static void onRiddleTenStepThreeBatteriesCollected() {
    showCorrectTerminalInputDialog();
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
