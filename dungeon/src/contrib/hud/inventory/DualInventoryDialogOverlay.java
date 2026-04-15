package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.hud.elements.InventoryComponentProvider;
import contrib.hud.overlays.InventoryGridRenderer;
import contrib.hud.overlays.ItemTooltipRenderer;
import contrib.hud.overlays.DialogFrameRenderer;
import contrib.item.Item;
import core.Game;
import core.input.MouseButtons;
import core.ui.overlay.UiOverlay;
import core.ui.StageHandle;
import core.utils.InputManager;

import java.awt.*;
import java.util.stream.Stream;

/**
 * A UI overlay that provides a dual inventory dialog for managing two inventory parts simultaneously.
 *
 * <p>The DualInventoryDialogOverlay class enables users to interact with two inventory panels,
 * allowing item transfer between them and providing intuitive drag-and-drop functionality.
 *
 * <p>The overlay includes support for rendering panel backgrounds, handling input operations,
 * and drawing UI elements like tooltips, drag previews, and drop target highlights.
 *
 * <p>Key Features:
 * <ul>
 *   <li>Two separate inventory panels with customizable titles.</li>
 *   <li>Drag-and-drop functionality with labeled previews and defined drag thresholds.</li>
 *   <li>Visual and interactive feedback for item transfers, including highlights and hover tooltips.</li>
 *   <li>Ability to manage UI state such as visibility, dimensions, and interaction states.</li>
 * </ul>
 */
final class DualInventoryDialogOverlay
  implements UiOverlay, InventoryComponentProvider {

  private static final int DEFAULT_WIDTH = 1100;
  private static final int DEFAULT_HEIGHT = 470;
  private static final int PANEL_GAP = 18;
  private static final int PANEL_HEADER_GAP = 8;

  private static final int PANEL_PADDING = 8;
  private static final int DRAG_THRESHOLD_PX = 8;
  private static final int DRAG_PREVIEW_OFFSET_X = 14;
  private static final int DRAG_PREVIEW_OFFSET_Y = 18;
  private static final int DRAG_PREVIEW_PADDING_X = 10;
  private static final int DRAG_PREVIEW_PADDING_Y = 7;
  private static final int DRAG_TARGET_INSET = 3;
  private static final int DRAG_TARGET_ARC = 8;

  private final String leftTitle;
  private final InventoryComponent leftInventory;
  private final String rightTitle;
  private final InventoryComponent rightInventory;

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  private SlotSelection pressedSlotSelection = null;
  private boolean leftButtonDownLastFrame = false;
  private int pressedMouseX = 0;
  private int pressedMouseY = 0;
  private DragState dragState = null;

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
        DEFAULT_HEIGHT,
        108 + maxGridHeight + 2 * PANEL_PADDING + DialogFrameRenderer.PADDING);

    if (x == 0 && y == 0) {
      x = (Game.windowWidth() - width) / 2;
      y = (Game.windowHeight() - height) / 2;
    }

    int contentY;
    int leftStartX;
    int rightStartX;
    int gridTop;

    GridLayout leftGrid;
    GridLayout rightGrid;

    DialogFrameRenderer.RenderState state =
      DialogFrameRenderer.beginDialog(g);

    try {
      contentY =
        DialogFrameRenderer.drawFrameAndTitle(g, x, y, width, height, "Inventory");

      int totalGridWidth = leftGridWidth + PANEL_GAP + rightGridWidth;
      leftStartX = x + (width - totalGridWidth) / 2;
      rightStartX = leftStartX + leftGridWidth + PANEL_GAP;

      int titleBaseline = contentY + g.getFontMetrics().getAscent();
      g.setColor(Color.WHITE);
      g.drawString(leftTitle, leftStartX, titleBaseline);
      g.drawString(rightTitle, rightStartX, titleBaseline);

      gridTop =
        titleBaseline + PANEL_HEADER_GAP + InventoryGridRenderer.GRID_TOP_GAP;

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
        g,
        leftPanelBounds.x,
        leftPanelBounds.y,
        leftPanelBounds.width,
        leftPanelBounds.height);
      drawPanelBackground(
        g,
        rightPanelBounds.x,
        rightPanelBounds.y,
        rightPanelBounds.width,
        rightPanelBounds.height);

      leftGrid =
        new GridLayout(InventorySide.LEFT, leftStartX, gridTop, leftColumns, leftSlots);
      rightGrid =
        new GridLayout(InventorySide.RIGHT, rightStartX, gridTop, rightColumns, rightSlots);

      InventoryGridRenderer.drawGrid(g, leftSlots, leftStartX, gridTop, leftColumns);
      InventoryGridRenderer.drawGrid(g, rightSlots, rightStartX, gridTop, rightColumns);

      if (dragState != null) {
        SlotSelection hoveredTarget = hoveredDropTarget(leftGrid, rightGrid);
        if (hoveredTarget != null) {
          drawDropTargetHighlight(g, hoveredTarget, leftGrid, rightGrid);
        }
        drawDragPreview(g);
      } else {
        drawHoverTooltip(g, leftGrid, rightGrid);
      }
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }

    handleInput(leftGrid, rightGrid);
  }

  private void handleInput(GridLayout leftGrid, GridLayout rightGrid) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      pressedSlotSelection = null;
      dragState = null;
      leftButtonDownLastFrame = false;
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();
    boolean leftButtonDown = InputManager.isButtonPressed(MouseButtons.LEFT);

    if (leftButtonDown && !leftButtonDownLastFrame) {
      pressedSlotSelection = findSlotSelection(mouseX, mouseY, leftGrid, rightGrid);
      pressedMouseX = mouseX;
      pressedMouseY = mouseY;
      dragState = null;
    } else if (leftButtonDown) {
      maybeStartDrag(mouseX, mouseY);
    }

    if (!leftButtonDown && leftButtonDownLastFrame) {
      SlotSelection releasedSlotSelection = findSlotSelection(mouseX, mouseY, leftGrid, rightGrid);
      SlotSelection previouslyPressedSlot = pressedSlotSelection;
      DragState completedDrag = dragState;

      pressedSlotSelection = null;
      dragState = null;

      if (completedDrag != null) {
        handleDraggedRelease(completedDrag, releasedSlotSelection);
      } else if (previouslyPressedSlot != null && previouslyPressedSlot.equals(releasedSlotSelection)) {
        transferClickedItem(previouslyPressedSlot);
      }
    }

    leftButtonDownLastFrame = leftButtonDown;
  }

  private void maybeStartDrag(int mouseX, int mouseY) {
    if (dragState != null || pressedSlotSelection == null) {
      return;
    }

    int deltaX = mouseX - pressedMouseX;
    int deltaY = mouseY - pressedMouseY;
    int thresholdSquared = DRAG_THRESHOLD_PX * DRAG_THRESHOLD_PX;
    if ((deltaX * deltaX) + (deltaY * deltaY) < thresholdSquared) {
      return;
    }

    InventoryComponent source = inventoryOf(pressedSlotSelection.side());
    Item draggedItem = source.get(pressedSlotSelection.slotIndex()).orElse(null);
    if (draggedItem == null) {
      pressedSlotSelection = null;
      return;
    }

    dragState = new DragState(pressedSlotSelection, draggedItem);
  }

  private void handleDraggedRelease(DragState completedDrag, SlotSelection releasedSlotSelection) {
    if (releasedSlotSelection == null) {
      return;
    }

    if (releasedSlotSelection.side() == completedDrag.source().side()) {
      return;
    }

    transferDraggedItem(completedDrag, releasedSlotSelection);
  }

  private void transferClickedItem(SlotSelection slotSelection) {
    InventoryComponent source = inventoryOf(slotSelection.side());
    InventoryComponent destination = oppositeInventoryOf(slotSelection.side());

    Item item = source.get(slotSelection.slotIndex()).orElse(null);
    if (item == null) {
      return;
    }

    source.transfer(item, destination);
  }

  private void transferDraggedItem(DragState drag, SlotSelection releasedSlotSelection) {
    InventoryComponent source = inventoryOf(drag.source().side());
    InventoryComponent destination = inventoryOf(releasedSlotSelection.side());

    source.transfer(drag.source().slotIndex(), destination, releasedSlotSelection.slotIndex());
  }

  private SlotSelection hoveredDropTarget(GridLayout leftGrid, GridLayout rightGrid) {
    if (dragState == null) {
      return null;
    }

    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return null;
    }

    SlotSelection hovered = findSlotSelection(stage.mouseX(), stage.mouseY(), leftGrid, rightGrid);

    if (hovered == null) {
      return null;
    }

    if (hovered.side() == dragState.source().side()) {
      return null;
    }

    return hovered;
  }

  private void drawHoverTooltip(Graphics2D g, GridLayout leftGrid, GridLayout rightGrid) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();

    if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
      return;
    }

    SlotSelection hoveredSlot = findSlotSelection(mouseX, mouseY, leftGrid, rightGrid);
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
    Graphics2D g, SlotSelection targetSlot, GridLayout leftGrid, GridLayout rightGrid) {
    GridLayout targetGrid = targetSlot.side() == InventorySide.LEFT ? leftGrid : rightGrid;

    Rectangle bounds =
      InventoryGridRenderer.slotBounds(
        targetSlot.slotIndex(),
        targetGrid.startX(),
        targetGrid.startY(),
        targetGrid.columns());

    int insetX = bounds.x + DRAG_TARGET_INSET;
    int insetY = bounds.y + DRAG_TARGET_INSET;
    int insetWidth = bounds.width - 2 * DRAG_TARGET_INSET;
    int insetHeight = bounds.height - 2 * DRAG_TARGET_INSET;

    g.setColor(new Color(88, 168, 116, 70));
    g.fillRoundRect(insetX, insetY, insetWidth, insetHeight, DRAG_TARGET_ARC, DRAG_TARGET_ARC);

    g.setColor(new Color(132, 214, 156, 210));
    g.drawRoundRect(insetX, insetY, insetWidth, insetHeight, DRAG_TARGET_ARC, DRAG_TARGET_ARC);
  }

  private void drawDragPreview(Graphics2D g) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null || dragState == null) {
      return;
    }

    int previewX = stage.mouseX() + DRAG_PREVIEW_OFFSET_X;
    int previewY = stage.mouseY() + DRAG_PREVIEW_OFFSET_Y;

    String label = dragLabel(dragState.item());
    int textWidth = g.getFontMetrics().stringWidth(label);
    int textHeight = g.getFontMetrics().getAscent();

    int boxWidth = textWidth + 2 * DRAG_PREVIEW_PADDING_X;
    int boxHeight = textHeight + 2 * DRAG_PREVIEW_PADDING_Y;

    g.setColor(new Color(255, 255, 255, 235));
    g.fillRect(previewX, previewY, boxWidth, boxHeight);

    g.setColor(new Color(0x9dc1ebff, true));
    g.drawRect(previewX, previewY, boxWidth, boxHeight);

    g.setColor(Color.BLACK);
    g.drawString(
      label,
      previewX + DRAG_PREVIEW_PADDING_X,
      previewY + DRAG_PREVIEW_PADDING_Y + textHeight - 2);
  }

  private String dragLabel(Item item) {
    if (item == null) {
      return "";
    }

    String baseLabel = ItemTooltipRenderer.displayName(item);
    return item.stackSize() > 1 ? baseLabel + " x" + item.stackSize() : baseLabel;
  }

  private SlotSelection findSlotSelection(
    int mouseX, int mouseY, GridLayout leftGrid, GridLayout rightGrid) {
    int leftIndex =
      InventoryGridRenderer.findSlotIndexAt(
        mouseX, mouseY, leftGrid.slots(), leftGrid.startX(), leftGrid.startY(), leftGrid.columns());
    if (leftIndex >= 0) {
      return new SlotSelection(leftGrid.side(), leftIndex);
    }

    int rightIndex =
      InventoryGridRenderer.findSlotIndexAt(
        mouseX,
        mouseY,
        rightGrid.slots(),
        rightGrid.startX(),
        rightGrid.startY(),
        rightGrid.columns());
    if (rightIndex >= 0) {
      return new SlotSelection(rightGrid.side(), rightIndex);
    }

    return null;
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

  private record SlotSelection(InventorySide side, int slotIndex) {}

  private record DragState(SlotSelection source, Item item) {}

  private record GridLayout(
    InventorySide side, int startX, int startY, int columns, Item[] slots) {}
}
