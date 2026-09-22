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
          level.restoreCompletedManualSorting();
        }
      }
      case ARCHIVE_ACCESS -> {
        level.restoreCompletedBubbleSort();
        level.spawnWorldItemIfMissing(new ItemKey(), SystemRecoveryPointRegistry.ARCHIVE_KEY_SPAWN);
      }
      case STORAGE_ARRAY -> {
        level.restoreCompletedBubbleSort();
        level.openDoor(SystemRecoveryPointRegistry.DOOR_DATA_ARCHIVE);
        level.restoreCompletedDataArchive();
      }
      case SEARCH_PROGRAM -> {
        level.restoreCompletedStorage();
        level.spawnWorldItemIfMissing(
            new SearchProgramChipItem(), SystemRecoveryPointRegistry.SEARCH_PROGRAM_CHIP);
      }
      case SYSTEM_CORE_ACCESS -> {
        level.restoreCompletedStorage();
        level.restoreCompletedSearchRobot();
        level.markSystemCoreAccessModuleDeliveredAfterRestore();
        level.spawnWorldItemIfMissing(
            new SystemCoreAccessChipItem(),
            SystemRecoveryPointRegistry.SYSTEM_CORE_ACCESS_MODULE_DESTINATION);
      }
      default -> throw new IllegalArgumentException("Not a main-riddle checkpoint: " + checkpoint);
    }
  }
}
