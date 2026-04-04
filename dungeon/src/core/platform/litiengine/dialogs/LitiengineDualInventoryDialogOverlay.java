package core.platform.litiengine.dialogs;

import contrib.components.InventoryComponent;
import contrib.hud.UIUtils;
import contrib.item.Item;
import core.Game;
import core.input.MouseButtons;
import core.platform.litiengine.ui.LitiengineUiOverlay;
import core.ui.StageHandle;
import core.utils.InputManager;
import java.awt.*;

/**
 * Dual-inventory overlay for the LITIENGINE backend.
 *
 * <p>This version keeps the existing click-based transfer and additionally supports drag-based
 * item movement between both inventories.
 *
 * <p>Dragging an item onto a concrete slot on the opposite side performs an exact slot-to-slot
 * transfer.
 */
final class LitiengineDualInventoryDialogOverlay implements LitiengineUiOverlay {

  private static final int DEFAULT_WIDTH = 1180;
  private static final int DEFAULT_HEIGHT = 420;
  private static final int PANEL_GAP = 26;
  private static final int PANEL_HEADER_GAP = 14;

  private static final int PANEL_PADDING = 12;
  private static final int DRAG_THRESHOLD_PX = 8;
  private static final int DRAG_PREVIEW_OFFSET_X = 14;
  private static final int DRAG_PREVIEW_OFFSET_Y = 18;
  private static final int DRAG_PREVIEW_PADDING_X = 10;
  private static final int DRAG_PREVIEW_PADDING_Y = 7;
  private static final int DRAG_TARGET_INSET = 3;
  private static final int DRAG_TARGET_ARC = 8;

  private static final int TOOLTIP_OFFSET_X = 12;
  private static final int TOOLTIP_OFFSET_Y = 14;
  private static final int TOOLTIP_PADDING_X = 12;
  private static final int TOOLTIP_PADDING_Y = 10;
  private static final int TOOLTIP_LINE_GAP = 6;
  private static final int TOOLTIP_CORNER_RADIUS = 10;

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

  LitiengineDualInventoryDialogOverlay(
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

    int leftColumns = LitiengineInventoryGridRenderer.columnsFor(leftSlots);
    int rightColumns = LitiengineInventoryGridRenderer.columnsFor(rightSlots);
    int leftRows = LitiengineInventoryGridRenderer.rowsFor(leftSlots, leftColumns);
    int rightRows = LitiengineInventoryGridRenderer.rowsFor(rightSlots, rightColumns);

    int leftGridWidth = LitiengineInventoryGridRenderer.gridWidth(leftColumns);
    int rightGridWidth = LitiengineInventoryGridRenderer.gridWidth(rightColumns);

    int contentWidth =
      leftGridWidth + rightGridWidth + PANEL_GAP + 2 * LitiengineDialogOverlaySupport.PADDING;
    width = Math.max(DEFAULT_WIDTH, contentWidth);

    int leftGridHeight = LitiengineInventoryGridRenderer.gridHeight(leftRows);
    int rightGridHeight = LitiengineInventoryGridRenderer.gridHeight(rightRows);
    int maxGridHeight = Math.max(leftGridHeight, rightGridHeight);

    height =
      Math.max(
        DEFAULT_HEIGHT, 160 + maxGridHeight + LitiengineDialogOverlaySupport.PADDING);

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

    LitiengineDialogOverlaySupport.RenderState state =
      LitiengineDialogOverlaySupport.beginDialog(g);

    try {
      contentY =
        LitiengineDialogOverlaySupport.drawFrameAndTitle(
          g, x, y, width, height, "Inventory");

      int totalGridWidth = leftGridWidth + PANEL_GAP + rightGridWidth;
      leftStartX = x + (width - totalGridWidth) / 2;
      rightStartX = leftStartX + leftGridWidth + PANEL_GAP;

      int titleBaseline = contentY + g.getFontMetrics().getAscent();
      g.setColor(Color.WHITE);
      g.drawString(leftTitle, leftStartX, titleBaseline);
      g.drawString(rightTitle, rightStartX, titleBaseline);

      int infoY = contentY + PANEL_HEADER_GAP + LitiengineInventoryGridRenderer.INFO_LINE_GAP;

      LitiengineInventoryGridRenderer.drawInventoryInfo(
        g, leftInventory, leftSlots, leftStartX, infoY);
      LitiengineInventoryGridRenderer.drawInventoryInfo(
        g, rightInventory, rightSlots, rightStartX, infoY);

      gridTop =
        infoY
          + LitiengineInventoryGridRenderer.GRID_TOP_GAP
          + LitiengineInventoryGridRenderer.INFO_LINE_GAP;

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

      LitiengineInventoryGridRenderer.drawGrid(g, leftSlots, leftStartX, gridTop, leftColumns);
      LitiengineInventoryGridRenderer.drawGrid(g, rightSlots, rightStartX, gridTop, rightColumns);

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
      LitiengineDialogOverlaySupport.finishDialog(g, state);
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

  private SlotSelection hoveredDropTarget(GridLayout leftGrid, GridLayout rightGrid) {
    if (dragState == null) {
      return null;
    }

    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return null;
    }

    SlotSelection hovered =
      findSlotSelection(stage.mouseX(), stage.mouseY(), leftGrid, rightGrid);

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

    String itemTitle = safeDisplayName(hoveredItem);
    String formattedDescription =
      UIUtils.formatString(hoveredItem.description() == null ? "" : hoveredItem.description());

    String[] descriptionLines =
      formattedDescription.isBlank() ? new String[0] : formattedDescription.split("\\R");

    FontMetrics metrics = g.getFontMetrics();

    int descriptionWidth = 0;
    for (String line : descriptionLines) {
      descriptionWidth = Math.max(descriptionWidth, metrics.stringWidth(line));
    }

    int tooltipWidth =
      Math.max(metrics.stringWidth(itemTitle), descriptionWidth) + 2 * TOOLTIP_PADDING_X;

    int tooltipHeight = 2 * TOOLTIP_PADDING_Y + metrics.getAscent();
    if (descriptionLines.length > 0) {
      tooltipHeight += TOOLTIP_LINE_GAP + descriptionLines.length * metrics.getHeight();
    }

    int tooltipX = mouseX + TOOLTIP_OFFSET_X;
    int tooltipY = mouseY + TOOLTIP_OFFSET_Y;

    if (tooltipX + tooltipWidth > stage.getWidth()) {
      tooltipX = mouseX - tooltipWidth - TOOLTIP_OFFSET_X;
    }

    if (tooltipY + tooltipHeight > stage.getHeight()) {
      tooltipY = mouseY - tooltipHeight - TOOLTIP_OFFSET_Y;
    }

    g.setColor(new Color(248, 248, 252, 235));
    g.fillRoundRect(
      tooltipX,
      tooltipY,
      tooltipWidth,
      tooltipHeight,
      TOOLTIP_CORNER_RADIUS,
      TOOLTIP_CORNER_RADIUS);

    g.setColor(new Color(84, 88, 96, 220));
    g.drawRoundRect(
      tooltipX,
      tooltipY,
      tooltipWidth,
      tooltipHeight,
      TOOLTIP_CORNER_RADIUS,
      TOOLTIP_CORNER_RADIUS);

    int textX = tooltipX + TOOLTIP_PADDING_X;
    int baselineY = tooltipY + TOOLTIP_PADDING_Y + metrics.getAscent();

    g.setColor(Color.BLACK);
    g.drawString(itemTitle, textX, baselineY);

    if (descriptionLines.length > 0) {
      g.setColor(new Color(0x000000b0, true));
      baselineY += TOOLTIP_LINE_GAP + metrics.getHeight();
      for (String line : descriptionLines) {
        g.drawString(line, textX, baselineY);
        baselineY += metrics.getHeight();
      }
    }
  }

  private String safeDisplayName(Item item) {
    if (item == null) {
      return "";
    }

    String displayName = item.displayName();
    return displayName == null || displayName.isBlank()
      ? item.getClass().getSimpleName()
      : displayName;
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

    source.transfer(
      drag.source().slotIndex(),
      destination,
      releasedSlotSelection.slotIndex());
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
      LitiengineInventoryGridRenderer.slotBounds(
        targetSlot.slotIndex(),
        targetGrid.startX(),
        targetGrid.startY(),
        targetGrid.columns());

    int insetX = bounds.x + DRAG_TARGET_INSET;
    int insetY = bounds.y + DRAG_TARGET_INSET;
    int insetWidth = bounds.width - 2 * DRAG_TARGET_INSET;
    int insetHeight = bounds.height - 2 * DRAG_TARGET_INSET;

    g.setColor(new Color(88, 168, 116, 70));
    g.fillRoundRect(
      insetX, insetY, insetWidth, insetHeight, DRAG_TARGET_ARC, DRAG_TARGET_ARC);

    g.setColor(new Color(132, 214, 156, 210));
    g.drawRoundRect(
      insetX, insetY, insetWidth, insetHeight, DRAG_TARGET_ARC, DRAG_TARGET_ARC);
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

    g.setColor(new Color(20, 20, 24, 220));
    g.fillRoundRect(previewX, previewY, boxWidth, boxHeight, 10, 10);
    g.setColor(new Color(220, 220, 230, 220));
    g.drawRoundRect(previewX, previewY, boxWidth, boxHeight, 10, 10);

    g.setColor(Color.WHITE);
    g.drawString(
      label,
      previewX + DRAG_PREVIEW_PADDING_X,
      previewY + DRAG_PREVIEW_PADDING_Y + textHeight - 2);
  }

  private String dragLabel(Item item) {
    if (item == null) {
      return "";
    }

    String baseLabel =
      item.displayName() == null || item.displayName().isBlank()
        ? item.getClass().getSimpleName()
        : item.displayName();

    return item.stackSize() > 1 ? baseLabel + " x" + item.stackSize() : baseLabel;
  }

  private SlotSelection findSlotSelection(int mouseX, int mouseY, GridLayout leftGrid, GridLayout rightGrid) {
    int leftIndex =
      LitiengineInventoryGridRenderer.findSlotIndexAt(
        mouseX, mouseY, leftGrid.slots(), leftGrid.startX(), leftGrid.startY(), leftGrid.columns());
    if (leftIndex >= 0) {
      return new SlotSelection(leftGrid.side(), leftIndex);
    }

    int rightIndex =
      LitiengineInventoryGridRenderer.findSlotIndexAt(
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
    g.setColor(new Color(28, 30, 38, 170));
    g.fillRoundRect(x, y, width, height, 12, 12);
    g.setColor(new Color(90, 94, 108, 180));
    g.drawRoundRect(x, y, width, height, 12, 12);
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

  private enum InventorySide {
    LEFT,
    RIGHT
  }

  private record SlotSelection(InventorySide side, int slotIndex) {}

  private record DragState(SlotSelection source, Item item) {}

  private record GridLayout(
    InventorySide side, int startX, int startY, int columns, Item[] slots) {}
}
