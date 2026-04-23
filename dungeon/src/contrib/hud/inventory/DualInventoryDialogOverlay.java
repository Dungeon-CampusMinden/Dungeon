package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.hud.InventoryComponentProvider;
import contrib.hud.renderers.DialogFrameRenderer;
import contrib.hud.itemgrid.GridHitTest;
import contrib.hud.itemgrid.InventoryDragController;
import contrib.hud.itemgrid.InventoryDropHandling;
import contrib.hud.itemgrid.InventoryGridRenderer;
import contrib.hud.itemgrid.InventoryPanelRenderer;
import contrib.hud.itemgrid.InventoryTooltip;
import contrib.item.Item;
import core.Game;
import core.ui.overlay.BaseUiOverlay;
import java.awt.*;
import java.util.List;
import java.util.Optional;
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
    implements InventoryComponentProvider {

  private static final int DEFAULT_WIDTH = 1100;
  private static final int DEFAULT_HEIGHT = 470;
  private static final int PANEL_GAP = 34;
  private static final int PANEL_HEADER_GAP = 8;

  private static final int PANEL_PADDING = 8;
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

    int leftColumns = InventoryGridRenderer.columnsFor(leftSlots);
    int rightColumns = InventoryGridRenderer.columnsFor(rightSlots);
    int leftRows = InventoryGridRenderer.rowsFor(leftSlots, leftColumns);
    int rightRows = InventoryGridRenderer.rowsFor(rightSlots, rightColumns);

    int leftGridWidth = InventoryGridRenderer.gridWidth(leftColumns);
    int rightGridWidth = InventoryGridRenderer.gridWidth(rightColumns);

    int contentWidth =
        leftGridWidth
            + rightGridWidth
            + PANEL_GAP
            + 2 * DialogFrameRenderer.PADDING
            + 2 * PANEL_PADDING;
    width = Math.max(DEFAULT_WIDTH, contentWidth);

    int leftGridHeight = InventoryGridRenderer.gridHeight(leftRows);
    int rightGridHeight = InventoryGridRenderer.gridHeight(rightRows);
    int maxGridHeight = Math.max(leftGridHeight, rightGridHeight);

    height =
        Math.max(
            DEFAULT_HEIGHT, 108 + maxGridHeight + 2 * PANEL_PADDING + DialogFrameRenderer.PADDING);

    centerInIfUnpositioned(Game.windowWidth(), Game.windowHeight());

    int contentY;
    int leftStartX;
    int rightStartX;
    int gridTop;

    GridHitTest.Grid<InventorySide> leftGrid;
    GridHitTest.Grid<InventorySide> rightGrid;

    DialogFrameRenderer.RenderState state = DialogFrameRenderer.beginDialog(g);

    try {
      contentY = DialogFrameRenderer.drawFrameAndTitle(g, x, y, width, height, "Inventory");

      int totalGridWidth = leftGridWidth + PANEL_GAP + rightGridWidth;
      leftStartX = x + (width - totalGridWidth) / 2;
      rightStartX = leftStartX + leftGridWidth + PANEL_GAP;

      int titleBaseline = contentY + g.getFontMetrics().getAscent();
      g.setColor(Color.WHITE);
      g.drawString(leftTitle, leftStartX, titleBaseline);
      g.drawString(rightTitle, rightStartX, titleBaseline);

      gridTop = titleBaseline + PANEL_HEADER_GAP + InventoryGridRenderer.GRID_TOP_GAP;

      Rectangle leftPanelBounds =
          InventoryPanelRenderer.panelBounds(
              leftStartX, gridTop, leftGridWidth, leftGridHeight, PANEL_PADDING);

      Rectangle rightPanelBounds =
          InventoryPanelRenderer.panelBounds(
              rightStartX, gridTop, rightGridWidth, rightGridHeight, PANEL_PADDING);

      InventoryPanelRenderer.drawPanelBackground(g, leftPanelBounds);
      InventoryPanelRenderer.drawPanelBackground(g, rightPanelBounds);

      leftGrid =
          new GridHitTest.Grid<>(
              InventorySide.LEFT, leftStartX, gridTop, leftColumns, visibleLeftSlots);
      rightGrid =
          new GridHitTest.Grid<>(
              InventorySide.RIGHT, rightStartX, gridTop, rightColumns, visibleRightSlots);

      InventoryGridRenderer.drawGrid(g, visibleLeftSlots, leftStartX, gridTop, leftColumns);
      InventoryGridRenderer.drawGrid(g, visibleRightSlots, rightStartX, gridTop, rightColumns);

      if (dragController.isDragging()) {
        GridHitTest.Slot<InventorySide> hoveredTarget = hoveredDropTarget(leftGrid, rightGrid);
        if (hoveredTarget != null) {
          InventoryDropHandling.drawGridDropHighlight(g, hoveredTarget, leftGrid, rightGrid);
        }
        dragController.drawDragPreview(g);
      } else {
        drawHoverTooltip(g, leftGrid, rightGrid);
      }
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }

    handleInput(leftGrid, rightGrid);
  }

  private void handleInput(
      GridHitTest.Grid<InventorySide> leftGrid, GridHitTest.Grid<InventorySide> rightGrid) {
    Optional<InventoryDragController.MouseUpdate<InventorySide>> update =
        dragController.updateFromPrimaryMouse(
            (slotMouseX, slotMouseY) ->
                findSlotSelection(slotMouseX, slotMouseY, leftGrid, rightGrid),
            this::itemOf);

    if (update.isEmpty() || update.get().release().isEmpty()) {
      return;
    }

    InventoryDragController.Release<InventorySide> released = update.get().release().get();
    if (released.completedDrag() != null) {
      handleDraggedRelease(released.completedDrag(), released.releasedSlot());
    } else if (released.pressedSlot() != null
        && released.pressedSlot().equals(released.releasedSlot())) {
      transferClickedItem(released.pressedSlot());
    }
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
      GridHitTest.Grid<InventorySide> leftGrid, GridHitTest.Grid<InventorySide> rightGrid) {
    return InventoryDropHandling.hoveredDropTarget(
        dragController,
        (mouseX, mouseY) -> findSlotSelection(mouseX, mouseY, leftGrid, rightGrid),
        (source, target) -> target.side() != source.side());
  }

  private void drawHoverTooltip(
      Graphics2D g,
      GridHitTest.Grid<InventorySide> leftGrid,
      GridHitTest.Grid<InventorySide> rightGrid) {
    InventoryTooltip.drawHoveredSlotTooltip(
        g,
        bounds(),
        (mouseX, mouseY) -> findSlotSelection(mouseX, mouseY, leftGrid, rightGrid),
        this::itemOf);
  }

  private InventoryComponent inventoryOf(InventorySide side) {
    return side == InventorySide.LEFT ? leftInventory : rightInventory;
  }

  private InventoryComponent oppositeInventoryOf(InventorySide side) {
    return side == InventorySide.LEFT ? rightInventory : leftInventory;
  }

  private GridHitTest.Slot<InventorySide> findSlotSelection(
      int mouseX,
      int mouseY,
      GridHitTest.Grid<InventorySide> leftGrid,
      GridHitTest.Grid<InventorySide> rightGrid) {
    return GridHitTest.findGridSlotAt(mouseX, mouseY, List.of(leftGrid, rightGrid));
  }

  private Item itemOf(GridHitTest.Slot<InventorySide> slot) {
    if (slot == null) {
      return null;
    }

    return inventoryOf(slot.side()).get(slot.slotIndex()).orElse(null);
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
