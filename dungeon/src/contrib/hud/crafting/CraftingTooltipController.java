package contrib.hud.crafting;

import contrib.hud.utils.GridHitTest;
import contrib.hud.utils.InventoryTooltip;
import contrib.item.Item;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

/** Handles hover tooltip resolution for crafting input and result items. */
final class CraftingTooltipController {

  private final CraftingDragDropController dragDropController;

  CraftingTooltipController(CraftingDragDropController dragDropController) {
    this.dragDropController = dragDropController;
  }

  void drawHoverTooltip(
      Graphics2D g,
      Rectangle dialogBounds,
      GridHitTest.Grid<CraftingInventorySide> leftGrid,
      List<CraftingDialogLayout.SlotBounds> craftingBounds,
      Item[] resultItems,
      List<CraftingDialogLayout.ItemBounds> resultBounds) {
    if (InventoryTooltip.drawHoveredSlotTooltip(
        g,
        dialogBounds,
        (mouseX, mouseY) ->
            dragDropController.findSlotSelection(mouseX, mouseY, leftGrid, craftingBounds),
        dragDropController::itemOf)) {
      return;
    }

    for (int i = 0; i < resultBounds.size() && i < resultItems.length; i++) {
      CraftingDialogLayout.ItemBounds bounds = resultBounds.get(i);
      if (InventoryTooltip.drawItemTooltip(g, resultBounds(bounds), resultItems[i])) {
        return;
      }
    }
  }

  Item resultItemAt(
      int mouseX,
      int mouseY,
      Item[] resultItems,
      List<CraftingDialogLayout.ItemBounds> resultBounds) {
    int index = resultItemIndexAt(mouseX, mouseY, resultItems, resultBounds);
    return index < 0 ? null : resultItems[index];
  }

  int resultItemIndexAt(
      int mouseX,
      int mouseY,
      Item[] resultItems,
      List<CraftingDialogLayout.ItemBounds> resultBounds) {
    if (resultItems == null || resultBounds == null) {
      return -1;
    }

    for (int i = 0; i < resultBounds.size() && i < resultItems.length; i++) {
      if (resultBounds(resultBounds.get(i)).contains(mouseX, mouseY)) {
        return i;
      }
    }

    return -1;
  }

  private Rectangle resultBounds(CraftingDialogLayout.ItemBounds bounds) {
    return new Rectangle(bounds.x(), bounds.y(), bounds.size(), bounds.size());
  }
}
