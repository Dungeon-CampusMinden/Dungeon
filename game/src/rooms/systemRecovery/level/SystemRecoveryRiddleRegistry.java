package rooms.systemRecovery.level;

import rooms.systemRecovery.modules.interpreter.TerminalAttempt;
import rooms.systemRecovery.riddles.DataArchiveRiddle;
import rooms.systemRecovery.riddles.EnergyRiddle;
import rooms.systemRecovery.riddles.InventoryScannerRiddle;
import rooms.systemRecovery.riddles.ModuleStorageRiddle;
import rooms.systemRecovery.riddles.SystemCoreRiddle;
import rooms.systemRecovery.riddles.TransportStorageRiddle;
import rooms.systemRecovery.riddles.TwoDimensionalStorageRiddle;
import rooms.systemRecovery.story.SystemRecoveryStoryDialogs;
import rooms.systemRecovery.util.interpreter.TerminalStep;

/**
 * Owns the level-specific effects of successful terminal steps.
 *
 * <p>The generic interpreter validates source code and invokes callbacks. This registry is the
 * single bridge from those callbacks to the riddle instances belonging to the current level. It
 * deliberately does not validate code and does not contain static mutable gameplay state.
 */
final class SystemRecoveryRiddleRegistry {

  private final EnergyRiddle energy;
  private final ModuleStorageRiddle moduleStorage;
  private final InventoryScannerRiddle inventoryScanner;
  private final TransportStorageRiddle transportStorage;
  private final DataArchiveRiddle dataArchive;
  private final TwoDimensionalStorageRiddle twoDimensionalStorage;
  private final SystemCoreRiddle systemCore;
  private final SystemRecoveryStoryDialogs storyDialogs;
  private final Runnable completeSystemCore;

  SystemRecoveryRiddleRegistry(
      EnergyRiddle energy,
      ModuleStorageRiddle moduleStorage,
      InventoryScannerRiddle inventoryScanner,
      TransportStorageRiddle transportStorage,
      DataArchiveRiddle dataArchive,
      TwoDimensionalStorageRiddle twoDimensionalStorage,
      SystemCoreRiddle systemCore,
      SystemRecoveryStoryDialogs storyDialogs,
      Runnable completeSystemCore) {
    this.energy = energy;
    this.moduleStorage = moduleStorage;
    this.inventoryScanner = inventoryScanner;
    this.transportStorage = transportStorage;
    this.dataArchive = dataArchive;
    this.twoDimensionalStorage = twoDimensionalStorage;
    this.systemCore = systemCore;
    this.storyDialogs = storyDialogs;
    this.completeSystemCore = completeSystemCore;
  }

  /**
   * Applies the world and story effect belonging to a validated terminal step.
   *
   * @param step named terminal step that has just been accepted
   * @param attempt source and player context of the accepted input
   */
  void apply(TerminalStep step, TerminalAttempt attempt) {
    switch (step) {
      case ENERGY_ARRAY -> {
        energy.spawnEnergyCrates();
        if (SystemRecoveryLevel.recordedInitialTerminalAttemptWasCorrect()) {
          storyDialogs.announceForPlayer(
              SystemRecoveryStoryDialogs.ENERGY_VALUES,
              attempt.playerId(),
              SystemRecoveryLevel::triggerEchoCallAfterInitialCorrectInput);
        } else {
          tellPlayer(SystemRecoveryStoryDialogs.ENERGY_VALUES, attempt);
        }
      }
      case ENERGY_VALUES -> {
        energy.completeEnergyPuzzle();
        tellPlayer(SystemRecoveryStoryDialogs.ENERGY_BATTERY, attempt);
      }
      case MODULE_ARRAY -> {
        moduleStorage.activateModuleSockets();
        tellPlayer(SystemRecoveryStoryDialogs.MODULE_VALUES, attempt);
      }
      case MODULE_VALUES -> {
        moduleStorage.spawnModuleChips();
        tellPlayer(SystemRecoveryStoryDialogs.MODULE_ASSIGNMENT, attempt);
      }
      case MODULE_REMOVE_GPU -> {
        moduleStorage.removeGpuChip();
        tellPlayer(SystemRecoveryStoryDialogs.READ_MODULE_LENGTH, attempt);
      }
      case MODULE_LENGTH -> {
        moduleStorage.showModuleArrayLength();
        moduleStorage.portModuleChipsToScanner();
      }
      case INVENTORY_COUNT -> {
        inventoryScanner.completeScannerPuzzle();
        storyDialogs.announceToAllPlayers(SystemRecoveryStoryDialogs.SCANNER_LEVER);
      }
      case TRANSPORT_ARRAY -> {
        transportStorage.spawnTransportPackages();
        tellPlayer(SystemRecoveryStoryDialogs.PACKAGES_LOOP, attempt);
      }
      case TRANSPORT_COLLECT -> transportStorage.startTransportSequence();
      case ARCHIVE_ARRAYS -> {
        dataArchive.complete();
        tellPlayer(SystemRecoveryStoryDialogs.STORAGE_UNLOCKED, attempt);
      }
      case STORAGE_ARRAY -> {
        twoDimensionalStorage.activateMatrix();
        tellPlayer(SystemRecoveryStoryDialogs.STORAGE_VALUES, attempt);
      }
      case STORAGE_VALUES -> {
        twoDimensionalStorage.fillMatrix();
        twoDimensionalStorage.complete();
      }
      case CENTRAL_SORT -> {
        systemCore.completeSort();
        tellPlayer(SystemRecoveryStoryDialogs.CENTRAL_COUNT, attempt);
      }
      case CENTRAL_COUNT -> {
        systemCore.completeModuleCount();
        tellPlayer(SystemRecoveryStoryDialogs.CENTRAL_SEARCH, attempt);
      }
      case CENTRAL_SEARCH -> {
        systemCore.startMapSearch(
            () -> SystemRecoveryLevel.completeSystemCoreRobotSearch(attempt.playerId()));
      }
      case SYSTEM_CORE_META -> {
        completeSystemCore.run();
      }
      case SEARCH_PROGRAM -> {
        // Search-chip source is validated by the chip editor, not by the shared terminal.
      }
    }
  }

  private void tellPlayer(SystemRecoveryStoryDialogs.StoryStep step, TerminalAttempt attempt) {
    if (attempt.playerId() >= 0) {
      storyDialogs.announceForPlayer(step, attempt.playerId());
    } else {
      storyDialogs.announceToAllPlayers(step);
    }
  }
}
