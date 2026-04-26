package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.hud.itemgrid.GridHitTest;
import contrib.hud.itemgrid.ItemGridDragController;
import contrib.item.Item;
import java.util.List;
import java.util.stream.Stream;

/**
 * A UI overlay that provides a dual inventory dialog for managing two inventory parts
 * simultaneously.
 *
 * <p>The DualInventoryDialogOverlay class enables users to interact with two inventory panels,
 * allowing item transfer between them and providing intuitive drag-and-drop functionality.
 *
 * <p>The overlay includes support for rendering panel backgrounds, handling input operations, and
 * drawing UI elements like tooltips, drag previews, and drop target highlights.
 *
 * <p>Key Features:
 *
 * <ul>
 *   <li>Two separate inventory panels with customizable titles.
 *   <li>Drag-and-drop functionality with labeled previews and defined drag thresholds.
 *   <li>Visual and interactive feedback for item transfers, including highlights and hover
 *       tooltips.
 *   <li>Ability to manage UI state such as visibility, dimensions, and interaction states.
 * </ul>
 */
final class DualInventoryDialogOverlay
    extends BaseInventoryOverlay<DualInventoryDialogOverlay.InventorySide> {

  private static final int DEFAULT_WIDTH = 1100;
  private static final int DEFAULT_HEIGHT = 470;
  private static final int PANEL_GAP = 34;

  private final String leftTitle;
  private final InventoryComponent leftInventory;
  private final String rightTitle;
  private final InventoryComponent rightInventory;

  DualInventoryDialogOverlay(
      String leftTitle,
      InventoryComponent leftInventory,
      String rightTitle,
      InventoryComponent rightInventory) {
    super(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    this.leftTitle = (leftTitle == null || leftTitle.isBlank()) ? "Inventory" : leftTitle;
    this.leftInventory = leftInventory;
    this.rightTitle = (rightTitle == null || rightTitle.isBlank()) ? "Inventory" : rightTitle;
    this.rightInventory = rightInventory;
  }

  @Override
  protected InventoryDialogLayoutState.Measurement<InventorySide> measure() {
    Item[] leftSlots = leftInventory.items();
    Item[] rightSlots = rightInventory.items();

    Item[] visibleLeftSlots = visibleSlots(leftSlots, InventorySide.LEFT);
    Item[] visibleRightSlots = visibleSlots(rightSlots, InventorySide.RIGHT);

    return InventoryDialogLayoutState.measure(
        DEFAULT_WIDTH,
        DEFAULT_HEIGHT,
        PANEL_GAP,
        true,
        List.of(
            InventoryDialogLayoutState.PanelSpec.of(
                InventorySide.LEFT, leftTitle, leftSlots, visibleLeftSlots),
            InventoryDialogLayoutState.PanelSpec.of(
                InventorySide.RIGHT, rightTitle, rightSlots, visibleRightSlots)));
  }

  @Override
  protected String dialogTitle() {
    return "Inventory";
  }

  @Override
  protected void handleInput(List<GridHitTest.Grid<InventorySide>> grids) {
    handlePrimaryInput(
        grids,
        (drag, releasedSlot, mouseX, mouseY) -> handleDraggedRelease(drag, releasedSlot),
        this::transferClickedItem);
  }

  private void handleDraggedRelease(
      ItemGridDragController.DragState<InventorySide> completedDrag,
      GridHitTest.Slot<InventorySide> releasedSlotSelection) {
    if (!acceptsDrop(completedDrag.source(), releasedSlotSelection)) {
      return;
    }

    if (releasedSlotSelection.side() == completedDrag.source().side()) {
      moveOrSwapWithinInventory(completedDrag.source(), releasedSlotSelection);
      return;
    }

    transferDraggedItem(completedDrag, releasedSlotSelection);
  }

  private void moveOrSwapWithinInventory(
      GridHitTest.Slot<InventorySide> sourceSelection,
      GridHitTest.Slot<InventorySide> targetSelection) {
    InventoryComponent inventory = inventoryOf(sourceSelection.side());

    int sourceSlot = sourceSelection.slotIndex();
    int targetSlot = targetSelection.slotIndex();

    if (sourceSlot == targetSlot) {
      return;
    }

    Item sourceItem = inventory.remove(sourceSlot).orElse(null);
    if (sourceItem == null) {
      return;
    }

    Item targetItem = inventory.remove(targetSlot).orElse(null);

    inventory.set(targetSlot, sourceItem);
    if (targetItem != null) {
      inventory.set(sourceSlot, targetItem);
    }
  }

  private void transferClickedItem(GridHitTest.Slot<InventorySide> slotSelection) {
    InventoryComponent source = inventoryOf(slotSelection.side());
    InventoryComponent destination = oppositeInventoryOf(slotSelection.side());

    Item item = source.get(slotSelection.slotIndex()).orElse(null);
    if (item == null) {
      return;
    }

    source.transfer(item, destination);
  }

  private void transferDraggedItem(
      ItemGridDragController.DragState<InventorySide> drag,
      GridHitTest.Slot<InventorySide> releasedSlotSelection) {
    InventoryComponent source = inventoryOf(drag.source().side());
    InventoryComponent destination = inventoryOf(releasedSlotSelection.side());

    int sourceSlot = drag.source().slotIndex();
    int targetSlot = releasedSlotSelection.slotIndex();

    Item sourceItem = source.remove(sourceSlot).orElse(null);
    if (sourceItem == null) {
      return;
    }

    Item targetItem = destination.remove(targetSlot).orElse(null);

    destination.set(targetSlot, sourceItem);
    if (targetItem != null) {
      source.set(sourceSlot, targetItem);
    }
  }

  private InventoryComponent inventoryOf(InventorySide side) {
    return side == InventorySide.LEFT ? leftInventory : rightInventory;
  }

  private InventoryComponent oppositeInventoryOf(InventorySide side) {
    return side == InventorySide.LEFT ? rightInventory : leftInventory;
  }

  @Override
  protected Item itemOf(GridHitTest.Slot<InventorySide> slot) {
    if (slot == null) {
      return null;
    }

    return inventoryOf(slot.side()).get(slot.slotIndex()).orElse(null);
  }

  private boolean acceptsDrop(
      GridHitTest.Slot<InventorySide> source, GridHitTest.Slot<InventorySide> target) {
    return target != null && !target.equals(source);
  }

  @Override
  protected ItemGridDragController.DropTargetFilter<InventorySide> dropTargetFilter() {
    return this::acceptsDrop;
  }

  @Override
  public Stream<InventoryComponent> inventoryComponents() {
    return Stream.of(leftInventory, rightInventory);
  }

  enum InventorySide {
    LEFT,
    RIGHT
  }
}
