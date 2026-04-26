package contrib.hud.crafting.input;

import contrib.components.InventoryComponent;
import contrib.hud.crafting.CraftingDialogAction;
import contrib.hud.crafting.CraftingDialogController;
import contrib.hud.crafting.CraftingDialogLayout;
import contrib.hud.crafting.CraftingInventorySide;
import contrib.hud.itemgrid.InventoryDropHandling;
import contrib.hud.itemgrid.ItemGridDragController;
import contrib.hud.itemgrid.ItemGridHitTest;
import contrib.item.Item;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles drag and drop operations for the crafting dialog.
 *
 * <p>Manages to drag items between different inventory sides (crafting, target) and provides
 * visual feedback for drop zones and dragged items.
 */
public final class CraftingDragDropController {

  private static final int DRAG_THRESHOLD_PX = 8;

  private static final Color DRAG_HIGHLIGHT = new Color(157, 193, 235, 180);
  private static final Color DRAG_HIGHLIGHT_FILL = new Color(157, 193, 235, 45);

  private final CraftingDialogController controller;
  private final ItemGridDragController<CraftingInventorySide> dragController =
      ItemGridDragController.withAxisThreshold(DRAG_THRESHOLD_PX);

  /**
   * Constructs a new CraftingDragDropController.
   *
   * @param controller the crafting dialog controller to manage drag and drop operations
   */
  public CraftingDragDropController(CraftingDialogController controller) {
    this.controller = controller;
  }

  /**
   * Gets the visible slots for the given inventory side.
   *
   * @param slots the item array representing the inventory
   * @param side the inventory side (crafting or target)
   * @return the filtered array of visible slots
   */
  public Item[] visibleSlots(Item[] slots, CraftingInventorySide side) {
    return dragController.visibleSlots(slots, side);
  }

  /**
   * Checks if an item is currently being dragged.
   *
   * @return true if a drag operation is in progress, false otherwise
   */
  public boolean isDragging() {
    return dragController.isDragging();
  }

  /**
   * Resets the drag and drop state.
   */
  public void reset() {
    dragController.reset();
  }

  /**
   * Draws a preview of the item being dragged.
   *
   * @param g the graphics context for rendering
   */
  public void drawDragPreview(Graphics2D g) {
    dragController.drawDragPreview(g);
  }

  /**
   * Handles mouse input for drag and drop operations.
   *
   * @param leftButtonDown true if the left mouse button is pressed
   * @param mouseX the current mouse X coordinate
   * @param mouseY the current mouse Y coordinate
   * @param leftGrid the item grid hit test for the left panel
   * @param leftPanelBounds the bounds of the left panel
   * @param rightPanelBounds the bounds of the right panel
   * @param craftingBounds the list of crafting slot bounds
   * @param actionHitTest the action hit test for detecting button interactions
   * @return an optional containing a crafting dialog action if a button was clicked, otherwise empty
   */
  public Optional<CraftingDialogAction> handleInput(
      boolean leftButtonDown,
      int mouseX,
      int mouseY,
      ItemGridHitTest.Grid<CraftingInventorySide> leftGrid,
      Rectangle leftPanelBounds,
      Rectangle rightPanelBounds,
      List<CraftingDialogLayout.SlotBounds> craftingBounds,
      ActionHitTest actionHitTest) {
    Optional<ItemGridDragController.Release<CraftingInventorySide>> release =
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

    ItemGridDragController.Release<CraftingInventorySide> released = release.get();
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

  /**
   * Finds the slot selection at the given mouse coordinates.
   *
   * @param mouseX the X coordinate of the mouse cursor
   * @param mouseY the Y coordinate of the mouse cursor
   * @param leftGrid the item grid hit test for the left panel
   * @param craftingBounds the list of crafting slot bounds
   * @return a slot selection if found at the mouse position, otherwise null
   */
  public ItemGridHitTest.Slot<CraftingInventorySide> findSlotSelection(
      int mouseX,
      int mouseY,
      ItemGridHitTest.Grid<CraftingInventorySide> leftGrid,
      List<CraftingDialogLayout.SlotBounds> craftingBounds) {
    return ItemGridHitTest.findSlotAt(
        mouseX, mouseY, List.of(leftGrid), toBoundedSlots(craftingBounds));
  }

  /**
   * Draws visual highlights for valid drop zones based on the current drag state.
   *
   * @param g the graphics context for rendering
   * @param leftGrid the item grid hit test for the left panel
   * @param leftPanelBounds the bounds of the left panel
   * @param rightPanelBounds the bounds of the right panel
   * @param craftingBounds the list of crafting slot bounds
   * @param mouseX the current mouse X coordinate
   * @param mouseY the current mouse Y coordinate
   */
  public void drawDropHighlights(
      Graphics2D g,
      ItemGridHitTest.Grid<CraftingInventorySide> leftGrid,
      Rectangle leftPanelBounds,
      Rectangle rightPanelBounds,
      List<CraftingDialogLayout.SlotBounds> craftingBounds,
      int mouseX,
      int mouseY) {
    ItemGridDragController.DragState<CraftingInventorySide> dragState = dragController.dragState();
    if (dragState == null) {
      return;
    }

    if (dragState.source().side() == CraftingInventorySide.TARGET) {
      if (rightPanelBounds.contains(mouseX, mouseY)) {
        drawHighlight(g, rightPanelBounds);
      }
      return;
    }

    ItemGridHitTest.Slot<CraftingInventorySide> hoveredTargetSlot =
        InventoryDropHandling.hoveredDropTarget(
            dragController,
            (slotMouseX, slotMouseY) ->
                ItemGridHitTest.findGridSlotAt(slotMouseX, slotMouseY, List.of(leftGrid)),
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

  Item itemOf(ItemGridHitTest.Slot<CraftingInventorySide> selection) {
    if (selection == null) {
      return null;
    }

    return inventoryOf(selection.side()).get(selection.slotIndex()).orElse(null);
  }

  void transferClickedItem(ItemGridHitTest.Slot<CraftingInventorySide> selection) {
    if (selection == null) {
      return;
    }

    controller.transferBySlot(selection.side(), selection.slotIndex());
  }

  void transferDraggedItem(
    ItemGridDragController.DragState<CraftingInventorySide> completedDrag,
    ItemGridHitTest.Slot<CraftingInventorySide> releasedSlotSelection,
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

  private List<ItemGridHitTest.BoundedSlot<CraftingInventorySide>> toBoundedSlots(
      List<CraftingDialogLayout.SlotBounds> craftingBounds) {
    List<ItemGridHitTest.BoundedSlot<CraftingInventorySide>> slots =
        new ArrayList<>(craftingBounds.size());

    for (CraftingDialogLayout.SlotBounds bounds : craftingBounds) {
      slots.add(
          new ItemGridHitTest.BoundedSlot<>(
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
  public interface ActionHitTest {
    /**
     * Finds a crafting dialog action at the given coordinates.
     *
     * @param mouseX the X coordinate of the mouse cursor
     * @param mouseY the Y coordinate of the mouse cursor
     * @return an optional containing the action if found at the coordinates, otherwise empty
     */
    Optional<CraftingDialogAction> findActionAt(int mouseX, int mouseY);
  }
}
