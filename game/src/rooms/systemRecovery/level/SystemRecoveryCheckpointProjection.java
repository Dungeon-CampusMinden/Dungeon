package rooms.systemRecovery.level;

import feature.inventory.items.ItemKey;
import rooms.systemRecovery.items.SearchProgramChipItem;
import rooms.systemRecovery.items.SystemCoreAccessChipItem;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;

/** Rebuilds the authoritative world at the beginning of a saved main-riddle checkpoint. */
final class SystemRecoveryCheckpointProjection {

  private SystemRecoveryCheckpointProjection() {}

  /**
   * Applies one checkpoint without replaying terminal callbacks or story dialogs.
   *
   * @param level level whose entities and riddle controllers are projected
   * @param checkpoint saved main-riddle checkpoint
   */
  static void apply(SystemRecoveryLevel level, SystemRecoveryLearningStep checkpoint) {
    apply(level, checkpoint, false);
  }

  static void apply(
      SystemRecoveryLevel level, SystemRecoveryLearningStep checkpoint, boolean systemCoreExitOpen) {
    switch (checkpoint) {
      case ENERGY_ARRAY -> {}
      case MODULE_ARRAY -> {
        level.restoreCompletedEnergy();
        level.openDoor(SystemRecoveryPointRegistry.DOOR_MODULE_STORAGE);
        level.showModuleAssignmentsAfterRestore();
      }
      case INVENTORY_COUNT -> {
        level.restoreCompletedModules();
        level.openDoor(SystemRecoveryPointRegistry.DOOR_INVENTORY_SCANNER);
      }
      case TRANSPORT_ARRAY -> {
        level.restoreCompletedModules();
        level.restoreCompletedInventoryScanner();
        level.openDoor(SystemRecoveryPointRegistry.DOOR_INVENTORY_SCANNER);
        level.openDoor(SystemRecoveryPointRegistry.DOOR_TRANSPORT_STORAGE);
      }
      case MANUAL_SORTING, BUBBLE_SORT_CONDITION -> {
        level.restoreCompletedTransport();
        level.openDoor(SystemRecoveryPointRegistry.DOOR_DATA_STORAGE);
        if (checkpoint == SystemRecoveryLearningStep.BUBBLE_SORT_CONDITION) {
          level.restoreCompletedManualSorting(
              !level.playerHasPuzzleItem("sort-program-stick"));
        }
      }
      case BUBBLE_SORT_MACHINE -> level.restoreAtBubbleSortMachine();
      case ARCHIVE_ACCESS -> {
        level.restoreCompletedBubbleSort();
        if (!level.playerHasPuzzleItem("archive-key")) {
          level.spawnWorldItemIfMissing(new ItemKey(), SystemRecoveryPointRegistry.ARCHIVE_KEY_SPAWN);
        }
      }
      case ARCHIVE_ARRAYS -> {
        level.restoreCompletedBubbleSort();
        level.openDoor(SystemRecoveryPointRegistry.DOOR_DATA_ARCHIVE);
      }
      case STORAGE_ARRAY -> {
        level.restoreCompletedBubbleSort();
        level.openDoor(SystemRecoveryPointRegistry.DOOR_DATA_ARCHIVE);
        level.restoreCompletedDataArchive();
      }
      case SEARCH_PROGRAM -> {
        level.restoreCompletedStorage();
        if (!level.playerHasPuzzleItem("search-program-chip")) {
          level.spawnWorldItemIfMissing(
              new SearchProgramChipItem(), SystemRecoveryPointRegistry.SEARCH_PROGRAM_CHIP);
        }
      }
      case SEARCH_ROBOT_RUN -> level.restoreCompletedStorage();
      case SYSTEM_CORE_ACCESS -> {
        level.restoreCompletedStorage();
        level.restoreCompletedSearchRobot();
        level.markSystemCoreAccessModuleDeliveredAfterRestore();
        if (!level.playerHasPuzzleItem("system-core-access")) {
          level.spawnWorldItemIfMissing(
              new SystemCoreAccessChipItem(),
              SystemRecoveryPointRegistry.SYSTEM_CORE_ACCESS_MODULE_DESTINATION);
        }
      }
      case CORE_SORT, CORE_COUNT, CORE_SEARCH, CORE_SEARCH_ROBOT, CORE_META, COMPLETE -> {
        level.restoreCompletedStorage();
        level.restoreCompletedSearchRobot();
        level.restoreCoreCheckpoint(checkpoint, systemCoreExitOpen);
      }
      default -> throw new IllegalArgumentException("Not a main-riddle checkpoint: " + checkpoint);
    }
  }
}
