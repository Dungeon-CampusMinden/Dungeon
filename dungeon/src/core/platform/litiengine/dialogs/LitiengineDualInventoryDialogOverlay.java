package core.platform.litiengine.dialogs;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Game;
import core.input.MouseButtons;
import core.platform.litiengine.ui.LitiengineUiOverlay;
import core.ui.StageHandle;
import core.utils.InputManager;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Minimal dual-inventory overlay for the LITIENGINE backend.
 *
 * <p>This version shows two inventories side by side and supports simple click-based item transfer
 * between them.
 *
 * <p>Drag-and-drop and more advanced slot interaction are still not implemented.
 */
final class LitiengineDualInventoryDialogOverlay implements LitiengineUiOverlay {

  private static final int DEFAULT_WIDTH = 1180;
  private static final int DEFAULT_HEIGHT = 420;
  private static final int PANEL_GAP = 26;
  private static final int PANEL_HEADER_GAP = 14;

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

    int maxGridHeight =
      Math.max(
        LitiengineInventoryGridRenderer.gridHeight(leftRows),
        LitiengineInventoryGridRenderer.gridHeight(rightRows));

    height =
      Math.max(
        DEFAULT_HEIGHT,
        160 + maxGridHeight + LitiengineDialogOverlaySupport.PADDING);

    if (x == 0 && y == 0) {
      x = (Game.windowWidth() - width) / 2;
      y = (Game.windowHeight() - height) / 2;
    }

    int contentY;
    int leftStartX;
    int rightStartX;
    int gridTop;

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

      gridTop = infoY + LitiengineInventoryGridRenderer.GRID_TOP_GAP + 4;

      drawPanelBackground(
        g,
        leftStartX - 12,
        gridTop - 12,
        leftGridWidth + 24,
        LitiengineInventoryGridRenderer.gridHeight(leftRows) + 24);

      drawPanelBackground(
        g,
        rightStartX - 12,
        gridTop - 12,
        rightGridWidth + 24,
        LitiengineInventoryGridRenderer.gridHeight(rightRows) + 24);

      LitiengineInventoryGridRenderer.drawGrid(g, leftSlots, leftStartX, gridTop, leftColumns);
      LitiengineInventoryGridRenderer.drawGrid(g, rightSlots, rightStartX, gridTop, rightColumns);
    } finally {
      LitiengineDialogOverlaySupport.finishDialog(g, state);
    }

    GridLayout leftGrid =
      new GridLayout(InventorySide.LEFT, leftStartX, gridTop, leftColumns, leftSlots);
    GridLayout rightGrid =
      new GridLayout(InventorySide.RIGHT, rightStartX, gridTop, rightColumns, rightSlots);

    handleInput(leftGrid, rightGrid);
  }

  private void handleInput(GridLayout leftGrid, GridLayout rightGrid) {
    StageHandle stage = Game.stage().orElse(null);
    if (stage == null) {
      return;
    }

    int mouseX = stage.mouseX();
    int mouseY = stage.mouseY();

    if (InputManager.isButtonJustPressed(MouseButtons.LEFT)) {
      pressedSlotSelection = findSlotSelection(mouseX, mouseY, leftGrid, rightGrid);
    }

    if (InputManager.isButtonJustReleased(MouseButtons.LEFT)) {
      SlotSelection releasedSlotSelection = findSlotSelection(mouseX, mouseY, leftGrid, rightGrid);
      SlotSelection previouslyPressedSlot = pressedSlotSelection;
      pressedSlotSelection = null;

      if (previouslyPressedSlot != null && previouslyPressedSlot.equals(releasedSlotSelection)) {
        transferClickedItem(previouslyPressedSlot);
      }
    }
  }

  private void transferClickedItem(SlotSelection slotSelection) {
    InventoryComponent source =
      slotSelection.side() == InventorySide.LEFT ? leftInventory : rightInventory;
    InventoryComponent destination =
      slotSelection.side() == InventorySide.LEFT ? rightInventory : leftInventory;

    Item item = source.get(slotSelection.slotIndex()).orElse(null);
    if (item == null) {
      return;
    }

    source.transfer(item, destination);
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

  private record GridLayout(
    InventorySide side, int startX, int startY, int columns, Item[] slots) {}
}
