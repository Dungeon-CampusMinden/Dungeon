package rooms.systemRecovery.modules.computer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.items.SearchProgramChipItem;
import rooms.systemRecovery.items.SortProgramStickItem;

class MountedPuzzleItemsTest {

  @AfterEach
  void clearMountedItems() {
    MountedPuzzleItems.clear();
  }

  @Test
  void mountSnapshotAndUnmountTrackOnlyTheSameItemInstance() {
    Entity player = new Entity("mount-owner");
    SearchProgramChipItem chip = new SearchProgramChipItem(true, "map[0][0] = 1;");

    MountedPuzzleItems.mount(player, chip);

    assertEquals(1, MountedPuzzleItems.snapshot().size());
    assertEquals(player.id(), MountedPuzzleItems.snapshot().getFirst().playerId());
    assertFalse(MountedPuzzleItems.unmount(player.id(), new SearchProgramChipItem(true)));
    assertEquals(1, MountedPuzzleItems.snapshot().size());
    assertTrue(MountedPuzzleItems.unmount(player.id(), chip));
    assertTrue(MountedPuzzleItems.snapshot().isEmpty());
  }

  @Test
  void ignoresNonPuzzleItemsAndClearResetsTheRunRegistry() {
    Entity player = new Entity("mount-owner");
    MountedPuzzleItems.mount(player, new feature.inventory.items.ItemKey());
    MountedPuzzleItems.mount(player, new SortProgramStickItem(true, "for (...) {"));

    assertEquals(1, MountedPuzzleItems.snapshot().size());
    MountedPuzzleItems.clear();
    assertTrue(MountedPuzzleItems.snapshot().isEmpty());
  }
}
