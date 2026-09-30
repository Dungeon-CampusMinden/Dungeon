package rooms.systemRecovery.level;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import engine.Entity;
import engine.components.PlayerComponent;
import feature.components.InventoryComponent;
import feature.inventory.items.ItemKey;
import java.util.List;
import org.junit.jupiter.api.Test;
import rooms.systemRecovery.items.SearchProgramChipItem;
import rooms.systemRecovery.modules.computer.UsbProgramDraft;
import rooms.systemRecovery.save.SystemRecoverySave;

class SystemRecoveryInventoryRestoreTest {

  @Test
  void restoresTheProgrammedChipToTheSavedPlayer() {
    Entity ada = player("Ada");
    SystemRecoverySave.PlayerItemData savedChip =
        new SystemRecoverySave.PlayerItemData("Ada", "search-program-chip", true);

    List<SystemRecoverySave.PlayerItemData> unresolved =
        SystemRecoveryLevel.restorePuzzleItems(List.of(ada), List.of(savedChip), "Ada");

    assertTrue(unresolved.isEmpty());
    SearchProgramChipItem chip =
        (SearchProgramChipItem)
            ada.fetch(InventoryComponent.class)
                .orElseThrow()
                .itemOfClass(SearchProgramChipItem.class)
                .orElseThrow();
    assertTrue(chip.programmed());
  }

  @Test
  void keepsItemsPendingUntilTheirNamedOwnerJoins() {
    Entity bob = player("Bob");
    SystemRecoverySave.PlayerItemData savedChip =
        new SystemRecoverySave.PlayerItemData("Ada", "search-program-chip", true);

    List<SystemRecoverySave.PlayerItemData> unresolved =
        SystemRecoveryLevel.restorePuzzleItems(List.of(bob), List.of(savedChip), "Ada");

    assertEquals(List.of(savedChip), unresolved);
    assertFalse(
        bob.fetch(InventoryComponent.class).orElseThrow().hasItem(SearchProgramChipItem.class));
  }

  @Test
  void restoresTheUnprogrammedChipDraft() {
    Entity ada = player("Ada");
    String draft = UsbProgramDraft.encode(List.of("", "map[row][column] == 1", ""));
    SystemRecoverySave.PlayerItemData savedChip =
        new SystemRecoverySave.PlayerItemData("Ada", "search-program-chip", false, draft);

    SystemRecoveryLevel.restorePuzzleItems(List.of(ada), List.of(savedChip), "Ada");

    SearchProgramChipItem chip =
        (SearchProgramChipItem)
            ada.fetch(InventoryComponent.class)
                .orElseThrow()
                .itemOfClass(SearchProgramChipItem.class)
                .orElseThrow();
    assertFalse(chip.programmed());
    assertEquals(draft, chip.draft());
  }

  @Test
  void restoresTheArchiveKeyToItsSavedOwner() {
    Entity ada = player("Ada");
    SystemRecoverySave.PlayerItemData savedKey =
        new SystemRecoverySave.PlayerItemData("Ada", "archive-key", false, "");

    List<SystemRecoverySave.PlayerItemData> unresolved =
        SystemRecoveryLevel.restorePuzzleItems(List.of(ada), List.of(savedKey), "Ada");

    assertTrue(unresolved.isEmpty());
    assertTrue(
        ada.fetch(InventoryComponent.class).orElseThrow().itemOfClass(ItemKey.class).isPresent());
  }

  private static Entity player(String name) {
    Entity player = new Entity(name);
    player.add(new PlayerComponent(true, name));
    player.add(new InventoryComponent(1));
    return player;
  }
}
