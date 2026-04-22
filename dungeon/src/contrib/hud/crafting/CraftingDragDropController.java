package contrib.hud.crafting;

import contrib.components.InventoryComponent;
import contrib.hud.utils.GridHitTest;
import contrib.hud.utils.InventoryDragController;
import contrib.hud.utils.InventoryDropHandling;
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
  private final InventoryDragController<InventorySide> dragController =
      InventoryDragController.withAxisThreshold(DRAG_THRESHOLD_PX);

  CraftingDragDropController(CraftingDialogController controller) {
    this.controller = controller;
  }

  Item[] visibleSlots(Item[] slots, InventorySide side) {
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
      GridHitTest.Grid<InventorySide> leftGrid,
      Rectangle leftPanelBounds,
      Rectangle rightPanelBounds,
      List<CraftingDialogLayout.SlotBounds> craftingBounds,
      ActionHitTest actionHitTest) {
    Optional<InventoryDragController.Release<InventorySide>> release =
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

    InventoryDragController.Release<InventorySide> released = release.get();
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

  void transferClickedItem(GridHitTest.Slot<InventorySide> selection) {
    if (selection == null) {
      return;
    }

    controller.transferBySlot(selection.side().controllerSide(), selection.slotIndex());
  }

  void transferDraggedItem(
      InventoryDragController.DragState<InventorySide> completedDrag,
      GridHitTest.Slot<InventorySide> releasedSlotSelection,
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
          completedDrag.source().side().controllerSide(),
          completedDrag.source().slotIndex(),
          releasedSlotSelection.side().controllerSide(),
          releasedSlotSelection.slotIndex());
      return;
    }

    if (completedDrag.source().side() == InventorySide.TARGET
        && rightPanelBounds.contains(mouseX, mouseY)) {
      controller.transferBySlot(
          CraftingDialogController.InventorySide.TARGET, completedDrag.source().slotIndex());
      return;
    }

    if (completedDrag.source().side() == InventorySide.CRAFTING
        && leftPanelBounds.contains(mouseX, mouseY)) {
      controller.transferBySlot(
          CraftingDialogController.InventorySide.CRAFTING, completedDrag.source().slotIndex());
    }
  }

  GridHitTest.Slot<InventorySide> findSlotSelection(
      int mouseX,
      int mouseY,
      GridHitTest.Grid<InventorySide> leftGrid,
      List<CraftingDialogLayout.SlotBounds> craftingBounds) {
    return GridHitTest.findSlotAt(
        mouseX, mouseY, List.of(leftGrid), toBoundedSlots(craftingBounds));
  }

  void drawDropHighlights(
      Graphics2D g,
      GridHitTest.Grid<InventorySide> leftGrid,
      Rectangle leftPanelBounds,
      Rectangle rightPanelBounds,
      List<CraftingDialogLayout.SlotBounds> craftingBounds,
      int mouseX,
      int mouseY) {
    InventoryDragController.DragState<InventorySide> dragState = dragController.dragState();
    if (dragState == null) {
      return;
    }

    if (dragState.source().side() == InventorySide.TARGET) {
      if (rightPanelBounds.contains(mouseX, mouseY)) {
        drawHighlight(g, rightPanelBounds);
      }
      return;
    }

    GridHitTest.Slot<InventorySide> hoveredTargetSlot =
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

  Item itemOf(GridHitTest.Slot<InventorySide> selection) {
    if (selection == null) {
      return null;
    }

    return inventoryOf(selection.side()).get(selection.slotIndex()).orElse(null);
  }

  private List<GridHitTest.BoundedSlot<InventorySide>> toBoundedSlots(
      List<CraftingDialogLayout.SlotBounds> craftingBounds) {
    List<GridHitTest.BoundedSlot<InventorySide>> slots = new ArrayList<>(craftingBounds.size());

    for (CraftingDialogLayout.SlotBounds bounds : craftingBounds) {
      slots.add(
          new GridHitTest.BoundedSlot<>(
              InventorySide.CRAFTING,
              bounds.slotIndex(),
              new Rectangle(bounds.x(), bounds.y(), bounds.size(), bounds.size())));
    }

    return List.copyOf(slots);
  }

  private void drawHighlight(Graphics2D g, Rectangle bounds) {
    InventoryDropHandling.drawDropHighlight(g, bounds, DRAG_HIGHLIGHT_FILL, DRAG_HIGHLIGHT);
  }

  private InventoryComponent inventoryOf(InventorySide side) {
    return side == InventorySide.TARGET
        ? controller.targetInventory()
        : controller.craftingInventory();
  }

  @FunctionalInterface
  interface ActionHitTest {
    Optional<CraftingDialogAction> findActionAt(int mouseX, int mouseY);
  }

  enum InventorySide {
    TARGET(CraftingDialogController.InventorySide.TARGET),
    CRAFTING(CraftingDialogController.InventorySide.CRAFTING);

    private final CraftingDialogController.InventorySide controllerSide;

    InventorySide(CraftingDialogController.InventorySide controllerSide) {
      this.controllerSide = controllerSide;
    }

    CraftingDialogController.InventorySide controllerSide() {
      return controllerSide;
    }
  }
}
