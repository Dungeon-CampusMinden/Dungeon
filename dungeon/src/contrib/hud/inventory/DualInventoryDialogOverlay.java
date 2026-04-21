package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.hud.elements.InventoryComponentProvider;
import contrib.hud.renderers.DialogFrameRenderer;
import contrib.hud.renderers.InventoryGridRenderer;
import contrib.hud.renderers.ItemTooltipRenderer;
import contrib.hud.utils.GridHitTest;
import contrib.hud.utils.InventoryDragController;
import contrib.item.Item;
import core.Game;
import core.input.MouseButtons;
import core.ui.StageHandle;
import core.ui.overlay.UiOverlay;
import core.utils.InputManager;
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
final class DualInventoryDialogOverlay implements UiOverlay, InventoryComponentProvider {

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

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  DualInventoryDialogOverlay(
      String leftTitle,
      InventoryComponent leftInventory,
      String rightTitle,
      InventoryComponent rightInventory) {
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

    if (x == 0 && y == 0) {
      x = (Game.windowWidth() - width) / 2;
      y = (Game.windowHeight() - height) / 2;
    }

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
          new Rectangle(
              leftStartX - PANEL_PADDING,
              gridTop - PANEL_PADDING,
              leftGridWidth + 2 * PANEL_PADDING,
              leftGridHeight + 2 * PANEL_PADDING);

      Rectangle rightPanelBounds =
          new Rectangle(
              rightStartX - PANEL_PADDING,
              gridTop - PANEL_PADDING,
              rightGridWidth + 2 * PANEL_PADDING,
              rightGridHeight + 2 * PANEL_PADDING);

      drawPanelBackground(
          g, leftPanelBounds.x, leftPanelBounds.y, leftPanelBounds.width, leftPanelBounds.height);
      drawPanelBackground(
          g,
          rightPanelBounds.x,
          rightPanelBounds.y,
          rightPanelBounds.width,
          rightPanelBounds.height);

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
          drawDropTargetHighlight(g, hoveredTarget, leftGrid, rightGrid);
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
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      dragController.reset();
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();
    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);

    Optional<InventoryDragController.Release<InventorySide>> release =
        dragController.update(
            leftButtonDown,
            mouseX,
            mouseY,
            (slotMouseX, slotMouseY) ->
                findSlotSelection(slotMouseX, slotMouseY, leftGrid, rightGrid),
            this::itemOf);

    if (release.isEmpty()) {
      return;
    }

    InventoryDragController.Release<InventorySide> released = release.get();
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
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return null;
    }

    return dragController.dropTargetAt(
        stage.mouseX(),
        stage.mouseY(),
        (mouseX, mouseY) -> findSlotSelection(mouseX, mouseY, leftGrid, rightGrid),
        (source, target) -> target.side() != source.side());
  }

  private void drawHoverTooltip(
      Graphics2D g,
      GridHitTest.Grid<InventorySide> leftGrid,
      GridHitTest.Grid<InventorySide> rightGrid) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();

    if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
      return;
    }

    GridHitTest.Slot<InventorySide> hoveredSlot =
        findSlotSelection(mouseX, mouseY, leftGrid, rightGrid);
    if (hoveredSlot == null) {
      return;
    }

    InventoryComponent hoveredInventory = inventoryOf(hoveredSlot.side());
    Item hoveredItem = hoveredInventory.get(hoveredSlot.slotIndex()).orElse(null);
    if (hoveredItem == null) {
      return;
    }

    ItemTooltipRenderer.drawTooltip(
        g, hoveredItem, mouseX, mouseY, (int) stage.getWidth(), (int) stage.getHeight());
  }

  private InventoryComponent inventoryOf(InventorySide side) {
    return side == InventorySide.LEFT ? leftInventory : rightInventory;
  }

  private InventoryComponent oppositeInventoryOf(InventorySide side) {
    return side == InventorySide.LEFT ? rightInventory : leftInventory;
  }

  private void drawDropTargetHighlight(
      Graphics2D g,
      GridHitTest.Slot<InventorySide> targetSlot,
      GridHitTest.Grid<InventorySide> leftGrid,
      GridHitTest.Grid<InventorySide> rightGrid) {
    GridHitTest.Grid<InventorySide> targetGrid =
        targetSlot.side() == InventorySide.LEFT ? leftGrid : rightGrid;

    InventoryDragController.drawDropHighlight(
        g,
        targetGrid.slotBounds(targetSlot.slotIndex()),
        InventoryDragController.DEFAULT_DROP_FILL,
        InventoryDragController.DEFAULT_DROP_OUTLINE);
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

  private void drawPanelBackground(Graphics2D g, int x, int y, int width, int height) {
    g.setColor(new Color(62, 62, 99, 96));
    g.fillRect(x, y, width, height);

    g.setColor(new Color(0x9dc1ebff, true));
    g.drawRect(x, y, width, height);
  }

  @Override
  public int x() {
    return x;
  }

  @Override
  public void x(int x) {
    this.x = x;
  }

  @Override
  public int y() {
    return y;
  }

  @Override
  public void y(int y) {
    this.y = y;
  }

  @Override
  public int width() {
    return width;
  }

  @Override
  public void width(int width) {
    this.width = width;
  }

  @Override
  public int height() {
    return height;
  }

  @Override
  public void height(int height) {
    this.height = height;
  }

  @Override
  public boolean visible() {
    return visible;
  }

  @Override
  public void visible(boolean visible) {
    this.visible = visible;
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
