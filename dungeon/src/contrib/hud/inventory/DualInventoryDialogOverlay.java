package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.hud.dialogs.DialogInventoryProvider;
import contrib.hud.renderers.DialogFrameRenderer;
import contrib.hud.itemgrid.GridHitTest;
import contrib.hud.itemgrid.InventoryDragController;
import contrib.hud.itemgrid.InventoryDropHandling;
import contrib.item.Item;
import core.Game;
import core.ui.overlay.BaseUiOverlay;
import java.awt.Graphics2D;
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
final class DualInventoryDialogOverlay extends BaseUiOverlay
    implements DialogInventoryProvider {

  private static final int DEFAULT_WIDTH = 1100;
  private static final int DEFAULT_HEIGHT = 470;
  private static final int PANEL_GAP = 34;
  private static final int DRAG_THRESHOLD_PX = 8;

  private final String leftTitle;
  private final InventoryComponent leftInventory;
  private final String rightTitle;
  private final InventoryComponent rightInventory;
  private final InventoryDragController<InventorySide> dragController =
      InventoryDragController.withDistanceThreshold(DRAG_THRESHOLD_PX);

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
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    Item[] leftSlots = leftInventory.items();
    Item[] rightSlots = rightInventory.items();

    Item[] visibleLeftSlots = dragController.visibleSlots(leftSlots, InventorySide.LEFT);
    Item[] visibleRightSlots = dragController.visibleSlots(rightSlots, InventorySide.RIGHT);

    InventoryDialogLayoutState.Measurement<InventorySide> measurement =
        InventoryDialogLayoutState.measure(
            DEFAULT_WIDTH,
            DEFAULT_HEIGHT,
            PANEL_GAP,
            true,
            List.of(
                InventoryDialogLayoutState.PanelSpec.of(
                    InventorySide.LEFT, leftTitle, leftSlots, visibleLeftSlots),
                InventoryDialogLayoutState.PanelSpec.of(
                    InventorySide.RIGHT, rightTitle, rightSlots, visibleRightSlots)));

    width = measurement.dialogWidth();
    height = measurement.dialogHeight();

    centerInIfUnpositioned(Game.windowWidth(), Game.windowHeight());

    InventoryDialogLayoutState<InventorySide> layoutState = null;

    DialogFrameRenderer.RenderState state = DialogFrameRenderer.beginDialog(g);

    try {
      int contentY = DialogFrameRenderer.drawFrameAndTitle(g, x, y, width, height, "Inventory");
      layoutState = InventoryDialogRenderer.draw(g, x, contentY, measurement);

      if (dragController.isDragging()) {
        GridHitTest.Slot<InventorySide> hoveredTarget = hoveredDropTarget(layoutState.grids());
        if (hoveredTarget != null) {
          InventoryDropHandling.drawGridDropHighlight(g, hoveredTarget, layoutState.grids());
        }
        dragController.drawDragPreview(g);
      } else {
        InventoryDialogInput.drawHoverTooltip(g, bounds(), layoutState.grids(), this::itemOf);
      }
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }

    if (layoutState != null) {
      handleInput(layoutState.grids());
    }
  }

  private void handleInput(List<GridHitTest.Grid<InventorySide>> grids) {
    InventoryDialogInput.handlePrimaryInput(
        dragController,
        grids,
        this::itemOf,
        (drag, releasedSlot, mouseX, mouseY) -> handleDraggedRelease(drag, releasedSlot),
        this::transferClickedItem);
  }

  private void handleDraggedRelease(
      InventoryDragController.DragState<InventorySide> completedDrag,
      GridHitTest.Slot<InventorySide> releasedSlotSelection) {
    if (releasedSlotSelection == null) {
      return;
    }

    if (releasedSlotSelection.equals(completedDrag.source())) {
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
      InventoryDragController.DragState<InventorySide> drag,
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

  private GridHitTest.Slot<InventorySide> hoveredDropTarget(
      List<GridHitTest.Grid<InventorySide>> grids) {
    return InventoryDialogInput.hoveredDropTarget(
        dragController, grids, (source, target) -> target.side() != source.side());
  }

  private InventoryComponent inventoryOf(InventorySide side) {
    return side == InventorySide.LEFT ? leftInventory : rightInventory;
  }

  private InventoryComponent oppositeInventoryOf(InventorySide side) {
    return side == InventorySide.LEFT ? rightInventory : leftInventory;
  }

  private Item itemOf(GridHitTest.Slot<InventorySide> slot) {
    return InventoryDialogInput.itemOf(slot, this::inventoryOf);
  }

  @Override
  public Stream<InventoryComponent> inventoryComponents() {
    return Stream.of(leftInventory, rightInventory);
  }

  private enum InventorySide {
    LEFT,
    RIGHT
  }
}
