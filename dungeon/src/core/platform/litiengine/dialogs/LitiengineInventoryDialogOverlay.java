package core.platform.litiengine.dialogs;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.Game;
import core.platform.litiengine.ui.LitiengineUiOverlay;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Minimal single-inventory overlay for the LITIENGINE backend.
 *
 * <p>This intentionally starts as a visual-only implementation: it renders inventory slots and
 * item names, but does not yet implement drag-and-drop, item usage, or slot transfer logic.
 */
final class LitiengineInventoryDialogOverlay implements LitiengineUiOverlay {

  private static final int DEFAULT_WIDTH = 620;
  private static final int DEFAULT_HEIGHT = 360;

  private static final int MAX_COLUMNS = 6;
  private static final int SLOT_WIDTH = 88;
  private static final int SLOT_HEIGHT = 64;
  private static final int SLOT_GAP = 10;

  private static final int INFO_LINE_GAP = 22;
  private static final int GRID_TOP_GAP = 18;
  private static final int SLOT_TEXT_PADDING = 6;
  private static final int SLOT_INDEX_PADDING = 5;

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
    int slotCount = Math.max(1, slots.length);
    int columns = Math.min(MAX_COLUMNS, slotCount);
    int rows = Math.max(1, (slotCount + columns - 1) / columns);

    width =
      Math.max(
        DEFAULT_WIDTH,
        2 * LitiengineDialogOverlaySupport.PADDING
          + columns * SLOT_WIDTH
          + (columns - 1) * SLOT_GAP);

    height =
      Math.max(
        DEFAULT_HEIGHT,
        130
          + rows * SLOT_HEIGHT
          + Math.max(0, rows - 1) * SLOT_GAP
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

      drawInventoryInfo(g, slots, contentY);

      int gridTop = contentY + INFO_LINE_GAP + GRID_TOP_GAP;
      for (int i = 0; i < slots.length; i++) {
        drawSlot(g, slotBounds(i, columns, gridTop), i, slots[i]);
      }
    } finally {
      LitiengineDialogOverlaySupport.finishDialog(g, state);
    }
  }

  private void drawInventoryInfo(Graphics2D g, Item[] slots, int contentY) {
    g.setColor(new Color(205, 205, 220));
    g.drawString(
      inventory.count() + " / " + slots.length + " slots used",
      x + LitiengineDialogOverlaySupport.PADDING,
      contentY + g.getFontMetrics().getAscent());
  }

  private void drawSlot(Graphics2D g, Rectangle bounds, int slotIndex, Item item) {
    g.setColor(new Color(35, 38, 48, 235));
    g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

    g.setColor(item == null ? new Color(110, 115, 130) : new Color(220, 220, 230));
    g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

    g.setColor(new Color(150, 155, 170));
    g.drawString(
      Integer.toString(slotIndex + 1),
      bounds.x + SLOT_INDEX_PADDING,
      bounds.y + g.getFontMetrics().getAscent() + SLOT_INDEX_PADDING);

    if (item == null) {
      g.setColor(new Color(120, 125, 135));
      drawCenteredString(g, "Empty", bounds);
      return;
    }

    g.setColor(Color.WHITE);
    FontMetrics fm = g.getFontMetrics();
    String label = fitToWidth(item.displayName(), fm, bounds.width - 2 * SLOT_TEXT_PADDING);
    int textX = bounds.x + (bounds.width - fm.stringWidth(label)) / 2;
    int textY = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();
    g.drawString(label, textX, textY);
  }

  private Rectangle slotBounds(int index, int columns, int gridTop) {
    int col = index % columns;
    int row = index / columns;

    int totalGridWidth = columns * SLOT_WIDTH + (columns - 1) * SLOT_GAP;
    int startX = x + (width - totalGridWidth) / 2;

    return new Rectangle(
      startX + col * (SLOT_WIDTH + SLOT_GAP),
      gridTop + row * (SLOT_HEIGHT + SLOT_GAP),
      SLOT_WIDTH,
      SLOT_HEIGHT);
  }

  private void drawCenteredString(Graphics2D g, String text, Rectangle bounds) {
    FontMetrics fm = g.getFontMetrics();
    int textX = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
    int textY = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();
    g.drawString(text, textX, textY);
  }

  private String fitToWidth(String text, FontMetrics fm, int maxWidth) {
    if (text == null || text.isBlank()) {
      return "";
    }
    if (fm.stringWidth(text) <= maxWidth) {
      return text;
    }

    String ellipsis = "...";
    int ellipsisWidth = fm.stringWidth(ellipsis);
    StringBuilder result = new StringBuilder();

    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (fm.stringWidth(result.toString() + c) + ellipsisWidth > maxWidth) {
        break;
      }
      result.append(c);
    }

    if (result.isEmpty()) {
      return ellipsis;
    }
    return result + ellipsis;
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
