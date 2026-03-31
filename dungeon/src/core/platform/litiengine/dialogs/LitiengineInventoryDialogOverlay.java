package core.platform.litiengine.dialogs;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Game;
import core.platform.litiengine.ui.LitiengineUiOverlay;
import java.awt.Graphics2D;

/**
 * Minimal single-inventory overlay for the LITIENGINE backend.
 *
 * <p>This intentionally starts as a visual-only implementation: it renders inventory slots and
 * item names, but does not yet implement drag-and-drop, item usage, or slot transfer logic.
 */
final class LitiengineInventoryDialogOverlay implements LitiengineUiOverlay {

  private static final int DEFAULT_WIDTH = 620;
  private static final int DEFAULT_HEIGHT = 360;

  private final String title;
  private final InventoryComponent inventory;

  private int x;
  private int y;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  LitiengineInventoryDialogOverlay(String title, InventoryComponent inventory) {
    this.title = (title == null || title.isBlank()) ? "Inventory" : title;
    this.inventory = inventory;
  }

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    Item[] slots = inventory.items();
    int columns = LitiengineInventoryGridRenderer.columnsFor(slots);
    int rows = LitiengineInventoryGridRenderer.rowsFor(slots, columns);

    width =
      Math.max(
        DEFAULT_WIDTH,
        2 * LitiengineDialogOverlaySupport.PADDING
          + LitiengineInventoryGridRenderer.gridWidth(columns));

    height =
      Math.max(
        DEFAULT_HEIGHT,
        130
          + LitiengineInventoryGridRenderer.gridHeight(rows)
          + LitiengineDialogOverlaySupport.PADDING);

    if (x == 0 && y == 0) {
      x = (Game.windowWidth() - width) / 2;
      y = (Game.windowHeight() - height) / 2;
    }

    LitiengineDialogOverlaySupport.RenderState state =
      LitiengineDialogOverlaySupport.beginDialog(g);

    try {
      int contentY =
        LitiengineDialogOverlaySupport.drawFrameAndTitle(g, x, y, width, height, title);

      LitiengineInventoryGridRenderer.drawInventoryInfo(
        g,
        inventory,
        slots,
        x + LitiengineDialogOverlaySupport.PADDING,
        contentY);

      int gridTop =
        contentY
          + LitiengineInventoryGridRenderer.INFO_LINE_GAP
          + LitiengineInventoryGridRenderer.GRID_TOP_GAP;

      int startX = x + (width - LitiengineInventoryGridRenderer.gridWidth(columns)) / 2;

      LitiengineInventoryGridRenderer.drawGrid(g, slots, startX, gridTop, columns);
    } finally {
      LitiengineDialogOverlaySupport.finishDialog(g, state);
    }
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
}
