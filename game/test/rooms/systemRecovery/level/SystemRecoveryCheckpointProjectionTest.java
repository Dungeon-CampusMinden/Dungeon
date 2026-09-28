package rooms.systemRecovery.level;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import feature.inventory.Item;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.petrinet.SystemRecoveryLearningStep;

class SystemRecoveryCheckpointProjectionTest {

  @Test
  void archiveArraysCheckpointRestoresTheOpenDoorWithoutRespawningTheKey() {
    SystemRecoveryLevel level = mock(SystemRecoveryLevel.class);

    SystemRecoveryCheckpointProjection.apply(level, SystemRecoveryLearningStep.ARCHIVE_ARRAYS);

    verify(level).restoreCompletedBubbleSort();
    verify(level).openDoor(SystemRecoveryPointRegistry.DOOR_DATA_ARCHIVE);
    verify(level, never()).restoreCompletedDataArchive();
    verify(level, never())
        .spawnWorldItemIfMissing(any(Item.class), eq(SystemRecoveryPointRegistry.ARCHIVE_KEY_SPAWN));
  }

  @Test
  void uploadedSortProgramResumesAtTheMachineWithoutSpawningAnotherStick() {
    SystemRecoveryLevel level = mock(SystemRecoveryLevel.class);

    SystemRecoveryCheckpointProjection.apply(level, SystemRecoveryLearningStep.BUBBLE_SORT_MACHINE);

    verify(level).restoreAtBubbleSortMachine();
    verify(level, never()).spawnWorldItemIfMissing(any(Item.class), eq("chip_spawn"));
  }

  @Test
  void pickedUpSystemCoreAccessModuleIsNotDuplicatedAtItsCheckpoint() {
    SystemRecoveryLevel level = mock(SystemRecoveryLevel.class);
    when(level.playerHasPuzzleItem("system-core-access")).thenReturn(true);

    SystemRecoveryCheckpointProjection.apply(level, SystemRecoveryLearningStep.SYSTEM_CORE_ACCESS);

    verify(level).markSystemCoreAccessModuleDeliveredAfterRestore();
    verify(level, never())
        .spawnWorldItemIfMissing(any(Item.class), eq("roboter_item_destination"));
  }

  @Test
  void programmedSearchChipCheckpointDoesNotSpawnASecondChip() {
    SystemRecoveryLevel level = mock(SystemRecoveryLevel.class);
    when(level.playerHasPuzzleItem("search-program-chip")).thenReturn(true);

    SystemRecoveryCheckpointProjection.apply(level, SystemRecoveryLearningStep.SEARCH_PROGRAM);

    verify(level, never()).spawnWorldItemIfMissing(any(Item.class), eq("chip"));
  }
}
