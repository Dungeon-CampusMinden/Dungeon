package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.hud.itemgrid.GridHitTest;
import contrib.hud.itemgrid.InventoryDragController;
import contrib.hud.itemgrid.InventoryDropHandling;
import contrib.hud.itemgrid.InventoryTooltip;
import contrib.item.Item;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/** Shared hit-test, tooltip, drop-target, and primary mouse handling for inventory dialogs. */
final class InventoryDialogInput {

  private InventoryDialogInput() {}

  static <S> void handlePrimaryInput(
      InventoryDragController<S> dragController,
      List<GridHitTest.Grid<S>> grids,
      InventoryDragController.ItemResolver<S> itemResolver,
      DragReleaseHandler<S> dragReleaseHandler,
      ClickReleaseHandler<S> clickReleaseHandler) {
    Optional<InventoryDragController.MouseUpdate<S>> update =
        dragController.updateFromPrimaryMouse(
            (slotMouseX, slotMouseY) -> findSlotSelection(grids, slotMouseX, slotMouseY),
            itemResolver);

    if (update.isEmpty() || update.get().release().isEmpty()) {
      return;
    }

    InventoryDragController.MouseUpdate<S> mouseUpdate = update.get();
    InventoryDragController.Release<S> release = mouseUpdate.release().get();

    if (release.completedDrag() != null) {
      dragReleaseHandler.handle(
          release.completedDrag(), release.releasedSlot(), mouseUpdate.mouseX(), mouseUpdate.mouseY());
      return;
    }

    if (clickReleaseHandler != null
        && release.pressedSlot() != null
        && release.pressedSlot().equals(release.releasedSlot())) {
      clickReleaseHandler.handle(release.pressedSlot());
    }
  }

  static <S> GridHitTest.Slot<S> hoveredDropTarget(
      InventoryDragController<S> dragController,
      List<GridHitTest.Grid<S>> grids,
      InventoryDragController.DropTargetFilter<S> targetFilter) {
    return InventoryDropHandling.hoveredDropTarget(
        dragController,
        (mouseX, mouseY) -> findSlotSelection(grids, mouseX, mouseY),
        targetFilter);
  }

  static <S> void drawHoverTooltip(
      Graphics2D g,
      Rectangle hoverArea,
      List<GridHitTest.Grid<S>> grids,
      InventoryTooltip.ItemResolver<S> itemResolver) {
    InventoryTooltip.drawHoveredSlotTooltip(
        g,
        hoverArea,
        (mouseX, mouseY) -> findSlotSelection(grids, mouseX, mouseY),
        itemResolver);
  }

  static <S> GridHitTest.Slot<S> findSlotSelection(
      List<GridHitTest.Grid<S>> grids, int mouseX, int mouseY) {
    return GridHitTest.findGridSlotAt(mouseX, mouseY, grids);
  }

  static <S> Item itemOf(
      GridHitTest.Slot<S> slot, Function<S, InventoryComponent> inventoryResolver) {
    if (slot == null) {
      return null;
    }

    return inventoryResolver.apply(slot.side()).get(slot.slotIndex()).orElse(null);
  }

  @FunctionalInterface
  interface DragReleaseHandler<S> {
    void handle(
        InventoryDragController.DragState<S> drag,
        GridHitTest.Slot<S> releasedSlot,
        int mouseX,
        int mouseY);
  }

  @FunctionalInterface
  interface ClickReleaseHandler<S> {
    void handle(GridHitTest.Slot<S> slot);
  }
}
