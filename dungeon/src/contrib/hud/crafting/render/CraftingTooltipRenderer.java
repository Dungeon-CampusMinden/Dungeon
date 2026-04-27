package contrib.hud.crafting.render;

import contrib.hud.crafting.CraftingDialogLayout;
import contrib.hud.crafting.CraftingInventorySide;
import contrib.hud.crafting.input.CraftingDragDropController;
import contrib.hud.itemgrid.InventoryTooltip;
import contrib.hud.itemgrid.ItemGridHitTest;
import contrib.item.Item;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

/**
 * Renders a tooltip for crafting-related UI elements.
 *
 * <p>The {@code CraftingTooltipRenderer} is responsible for rendering tooltips that display
 * information about the items or slots the user interacts with in the crafting dialog.
 *
 * <p>It uses a {@code CraftingDragDropController} to determine item and slot interactions.
 *
 * <p>This class is immutable and thread-safe, but the tooltip rendering functionality is
 * intended to be used only within the UI thread due to its reliance on graphical operations.
 */
public final class CraftingTooltipRenderer {

  private final CraftingDragDropController dragDropController;

  /**
   * Constructs a new {@code CraftingTooltipRenderer} instance with the specified drag-and-drop controller.
   *
   * @param dragDropController the {@code CraftingDragDropController} used to manage item and slot interactions
   */
  public CraftingTooltipRenderer(CraftingDragDropController dragDropController) {
    this.dragDropController = dragDropController;
  }

  /**
   * Draws a tooltip for the currently hovered item in the crafting dialog.
   *
   * <p>This method checks for hovered items in the left grid and crafting slots first,
   * then checks result items if no tooltip was drawn for the left grid.
   *
   * <p>Tooltips are drawn at the appropriate position relative to the dialog bounds.
   *
   * @param g the Graphics2D object used for drawing
   * @param dialogBounds the bounds of the crafting dialog
   * @param leftGrid the grid of inventory items with slot information
   * @param craftingBounds the list of crafting slot bounds
   * @param resultItems the array of items resulting from the current crafting recipe
   * @param resultBounds the list of bounds for each result item
   */
  public void drawHoverTooltip(
      Graphics2D g,
      Rectangle dialogBounds,
      ItemGridHitTest.Grid<CraftingInventorySide> leftGrid,
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

  private Rectangle resultBounds(CraftingDialogLayout.ItemBounds bounds) {
    return new Rectangle(bounds.x(), bounds.y(), bounds.size(), bounds.size());
  }
}
