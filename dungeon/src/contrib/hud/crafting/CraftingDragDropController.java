package contrib.hud.crafting;

import contrib.components.InventoryComponent;
import contrib.hud.itemgrid.GridHitTest;
import contrib.hud.itemgrid.InventoryDragController;
import contrib.hud.itemgrid.InventoryDropHandling;
import contrib.item.Item;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Coordinates drag/drop state and inventory transfers for the crafting dialog. */
final class CraftingDragDropController {

  private static final int DRAG_THRESHOLD_PX = 8;

  private static final Color DRAG_HIGHLIGHT = new Color(157, 193, 235, 180);
  private static final Color DRAG_HIGHLIGHT_FILL = new Color(157, 193, 235, 45);

  private final CraftingDialogController controller;
  private final InventoryDragController<CraftingInventorySide> dragController =
      InventoryDragController.withAxisThreshold(DRAG_THRESHOLD_PX);

  CraftingDragDropController(CraftingDialogController controller) {
    this.controller = controller;
  }

  Item[] visibleSlots(Item[] slots, CraftingInventorySide side) {
    return dragController.visibleSlots(slots, side);
  }

  boolean isDragging() {
    return dragController.isDragging();
  }

  void reset() {
    dragController.reset();
  }

  void drawDragPreview(Graphics2D g) {
    dragController.drawDragPreview(g);
  }

  Optional<CraftingDialogAction> handleInput(
      boolean leftButtonDown,
      int mouseX,
      int mouseY,
      GridHitTest.Grid<CraftingInventorySide> leftGrid,
      Rectangle leftPanelBounds,
      Rectangle rightPanelBounds,
      List<CraftingDialogLayout.SlotBounds> craftingBounds,
      ActionHitTest actionHitTest) {
    Optional<InventoryDragController.Release<CraftingInventorySide>> release =
        dragController.update(
            leftButtonDown,
            mouseX,
            mouseY,
            (slotMouseX, slotMouseY) -> {
              if (actionHitTest.findActionAt(slotMouseX, slotMouseY).isPresent()) {
                return null;
              }
              return findSlotSelection(slotMouseX, slotMouseY, leftGrid, craftingBounds);
            },
            this::itemOf);

    if (release.isEmpty()) {
      return Optional.empty();
    }

    InventoryDragController.Release<CraftingInventorySide> released = release.get();
    Optional<CraftingDialogAction> releasedButton = actionHitTest.findActionAt(mouseX, mouseY);
    if (releasedButton.isPresent() && released.completedDrag() == null) {
      return releasedButton;
    }

    if (released.completedDrag() != null) {
      transferDraggedItem(
          released.completedDrag(),
          released.releasedSlot(),
          leftPanelBounds,
          rightPanelBounds,
          mouseX,
          mouseY);
    } else if (released.pressedSlot() != null
        && released.pressedSlot().equals(released.releasedSlot())) {
      transferClickedItem(released.pressedSlot());
    }

    return Optional.empty();
  }

  void transferClickedItem(GridHitTest.Slot<CraftingInventorySide> selection) {
    if (selection == null) {
      return;
    }

    controller.transferBySlot(selection.side(), selection.slotIndex());
  }

  void transferDraggedItem(
      InventoryDragController.DragState<CraftingInventorySide> completedDrag,
      GridHitTest.Slot<CraftingInventorySide> releasedSlotSelection,
      Rectangle leftPanelBounds,
      Rectangle rightPanelBounds,
      int mouseX,
      int mouseY) {
    if (completedDrag == null) {
      return;
    }

    if (releasedSlotSelection != null
        && completedDrag.source().side() != releasedSlotSelection.side()) {
      controller.transferBySlotToSlot(
          completedDrag.source().side(),
          completedDrag.source().slotIndex(),
          releasedSlotSelection.side(),
          releasedSlotSelection.slotIndex());
      return;
    }

    if (completedDrag.source().side() == CraftingInventorySide.TARGET
        && rightPanelBounds.contains(mouseX, mouseY)) {
      controller.transferBySlot(CraftingInventorySide.TARGET, completedDrag.source().slotIndex());
      return;
    }

    if (completedDrag.source().side() == CraftingInventorySide.CRAFTING
        && leftPanelBounds.contains(mouseX, mouseY)) {
      controller.transferBySlot(CraftingInventorySide.CRAFTING, completedDrag.source().slotIndex());
    }
  }

  GridHitTest.Slot<CraftingInventorySide> findSlotSelection(
      int mouseX,
      int mouseY,
      GridHitTest.Grid<CraftingInventorySide> leftGrid,
      List<CraftingDialogLayout.SlotBounds> craftingBounds) {
    return GridHitTest.findSlotAt(
        mouseX, mouseY, List.of(leftGrid), toBoundedSlots(craftingBounds));
  }

  void drawDropHighlights(
      Graphics2D g,
      GridHitTest.Grid<CraftingInventorySide> leftGrid,
      Rectangle leftPanelBounds,
      Rectangle rightPanelBounds,
      List<CraftingDialogLayout.SlotBounds> craftingBounds,
      int mouseX,
      int mouseY) {
    InventoryDragController.DragState<CraftingInventorySide> dragState = dragController.dragState();
    if (dragState == null) {
      return;
    }

    if (dragState.source().side() == CraftingInventorySide.TARGET) {
      if (rightPanelBounds.contains(mouseX, mouseY)) {
        drawHighlight(g, rightPanelBounds);
      }
      return;
    }

    GridHitTest.Slot<CraftingInventorySide> hoveredTargetSlot =
        InventoryDropHandling.hoveredDropTarget(
            dragController,
            (slotMouseX, slotMouseY) ->
                GridHitTest.findGridSlotAt(slotMouseX, slotMouseY, List.of(leftGrid)),
            (source, target) -> target.side() != source.side());
    if (hoveredTargetSlot != null) {
      drawHighlight(g, leftGrid.slotBounds(hoveredTargetSlot.slotIndex()));
      return;
    }

    if (leftPanelBounds.contains(mouseX, mouseY)) {
      drawHighlight(g, leftPanelBounds);
      return;
    }

    for (CraftingDialogLayout.SlotBounds bounds : craftingBounds) {
      if (bounds.contains(mouseX, mouseY)) {
        drawHighlight(g, new Rectangle(bounds.x(), bounds.y(), bounds.size(), bounds.size()));
        return;
      }
    }
  }

  Item itemOf(GridHitTest.Slot<CraftingInventorySide> selection) {
    if (selection == null) {
      return null;
    }

    return inventoryOf(selection.side()).get(selection.slotIndex()).orElse(null);
  }

  private List<GridHitTest.BoundedSlot<CraftingInventorySide>> toBoundedSlots(
      List<CraftingDialogLayout.SlotBounds> craftingBounds) {
    List<GridHitTest.BoundedSlot<CraftingInventorySide>> slots =
        new ArrayList<>(craftingBounds.size());

    for (CraftingDialogLayout.SlotBounds bounds : craftingBounds) {
      slots.add(
          new GridHitTest.BoundedSlot<>(
              CraftingInventorySide.CRAFTING,
              bounds.slotIndex(),
              new Rectangle(bounds.x(), bounds.y(), bounds.size(), bounds.size())));
    }

    return List.copyOf(slots);
  }

  private void drawHighlight(Graphics2D g, Rectangle bounds) {
    InventoryDropHandling.drawDropHighlight(g, bounds, DRAG_HIGHLIGHT_FILL, DRAG_HIGHLIGHT);
  }

  private InventoryComponent inventoryOf(CraftingInventorySide side) {
    return side == CraftingInventorySide.TARGET
        ? controller.targetInventory()
        : controller.craftingInventory();
  }

  @FunctionalInterface
  interface ActionHitTest {
    Optional<CraftingDialogAction> findActionAt(int mouseX, int mouseY);
  }
}
