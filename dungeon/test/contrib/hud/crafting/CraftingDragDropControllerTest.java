package contrib.hud.crafting;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import contrib.components.InventoryComponent;
import contrib.hud.utils.GridHitTest;
import contrib.hud.utils.InventoryDragController;
import contrib.item.Item;
import java.awt.Rectangle;
import org.junit.jupiter.api.Test;

/** Tests for {@link CraftingDragDropController}. */
public class CraftingDragDropControllerTest {

  private static final Rectangle LEFT_PANEL = new Rectangle(10, 10, 180, 180);
  private static final Rectangle RIGHT_PANEL = new Rectangle(250, 10, 180, 180);

  /** Click transfers move an item to the opposite inventory side. */
  @Test
  public void clickTransferMovesTargetItemToCraftingInventory() {
    Item item = item();
    InventoryComponent targetInventory = new InventoryComponent(3);
    InventoryComponent craftingInventory = new InventoryComponent(3);
    targetInventory.set(0, item);

    CraftingDragDropController controller =
        dragDropController(targetInventory, craftingInventory);

    controller.transferClickedItem(new GridHitTest.Slot<>(CraftingInventorySide.TARGET, 0));

    assertTrue(targetInventory.get(0).isEmpty());
    assertSame(item, craftingInventory.get(0).orElse(null));
  }

  /** Slot drops place an item into the explicit empty target slot. */
  @Test
  public void slotDropMovesTargetItemToExactCraftingSlot() {
    Item item = item();
    InventoryComponent targetInventory = new InventoryComponent(3);
    InventoryComponent craftingInventory = new InventoryComponent(3);
    targetInventory.set(0, item);

    CraftingDragDropController controller =
        dragDropController(targetInventory, craftingInventory);

    controller.transferDraggedItem(
        drag(CraftingInventorySide.TARGET, 0, item),
        new GridHitTest.Slot<>(CraftingInventorySide.CRAFTING, 2),
        LEFT_PANEL,
        RIGHT_PANEL,
        0,
        0);

    assertTrue(targetInventory.get(0).isEmpty());
    assertSame(item, craftingInventory.get(2).orElse(null));
  }

  /** Dropping a target item on the crafting panel transfers it to the crafting inventory. */
  @Test
  public void panelDropMovesTargetItemToCraftingInventory() {
    Item item = item();
    InventoryComponent targetInventory = new InventoryComponent(3);
    InventoryComponent craftingInventory = new InventoryComponent(3);
    targetInventory.set(1, item);

    CraftingDragDropController controller =
        dragDropController(targetInventory, craftingInventory);

    controller.transferDraggedItem(
        drag(CraftingInventorySide.TARGET, 1, item),
        null,
        LEFT_PANEL,
        RIGHT_PANEL,
        RIGHT_PANEL.x + 1,
        RIGHT_PANEL.y + 1);

    assertTrue(targetInventory.get(1).isEmpty());
    assertSame(item, craftingInventory.get(0).orElse(null));
  }

  /** Dropping a crafting item on the target panel transfers it back to the target inventory. */
  @Test
  public void panelDropMovesCraftingItemToTargetInventory() {
    Item item = item();
    InventoryComponent targetInventory = new InventoryComponent(3);
    InventoryComponent craftingInventory = new InventoryComponent(3);
    craftingInventory.set(0, item);

    CraftingDragDropController controller =
        dragDropController(targetInventory, craftingInventory);

    controller.transferDraggedItem(
        drag(CraftingInventorySide.CRAFTING, 0, item),
        null,
        LEFT_PANEL,
        RIGHT_PANEL,
        LEFT_PANEL.x + 1,
        LEFT_PANEL.y + 1);

    assertTrue(craftingInventory.get(0).isEmpty());
    assertSame(item, targetInventory.get(0).orElse(null));
  }

  private CraftingDragDropController dragDropController(
      InventoryComponent targetInventory, InventoryComponent craftingInventory) {
    return new CraftingDragDropController(
        new CraftingDialogController(targetInventory, craftingInventory));
  }

  private InventoryDragController.DragState<CraftingInventorySide> drag(
      CraftingInventorySide side, int slotIndex, Item item) {
    return new InventoryDragController.DragState<>(
        new GridHitTest.Slot<>(side, slotIndex), item);
  }

  private Item item() {
    Item item = mock(Item.class);
    when(item.maxStackSize()).thenReturn(1);
    when(item.stackSize()).thenReturn(1);
    return item;
  }
}
