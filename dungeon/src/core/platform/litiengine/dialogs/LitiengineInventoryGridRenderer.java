package core.platform.litiengine.dialogs;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Shared visual renderer for inventory slot grids in LITIENGINE overlays.
 *
 * <p>This helper intentionally stays visual-only. It renders slot boxes and item labels,
 * but does not implement click, transfer, drag-and-drop, or item usage logic.
 */
final class LitiengineInventoryGridRenderer {

  static final int MAX_COLUMNS = 6;
  static final int SLOT_WIDTH = 88;
  static final int SLOT_HEIGHT = 64;
  static final int SLOT_GAP = 10;
  static final int INFO_LINE_GAP = 22;
  static final int GRID_TOP_GAP = 18;
  static final int SLOT_TEXT_PADDING = 6;
  static final int SLOT_INDEX_PADDING = 5;

  private LitiengineInventoryGridRenderer() {}

  static int columnsFor(Item[] slots) {
    return Math.clamp(slots.length, 1, MAX_COLUMNS);
  }

  static int rowsFor(Item[] slots, int columns) {
    return Math.max(1, (Math.max(1, slots.length) + columns - 1) / columns);
  }

  static int gridWidth(int columns) {
    return columns * SLOT_WIDTH + Math.max(0, columns - 1) * SLOT_GAP;
  }

  static int gridHeight(int rows) {
    return rows * SLOT_HEIGHT + Math.max(0, rows - 1) * SLOT_GAP;
  }

  static void drawInventoryInfo(
    Graphics2D g, InventoryComponent inventory, Item[] slots, int x, int y) {
    g.setColor(new Color(205, 205, 220));
    g.drawString(
      inventory.count() + " / " + slots.length + " slots used",
      x,
      y + g.getFontMetrics().getAscent());
  }

  static void drawGrid(
    Graphics2D g,
    Item[] slots,
    int startX,
    int startY,
    int columns) {

    for (int i = 0; i < slots.length; i++) {
      Rectangle bounds = slotBounds(i, startX, startY, columns);
      drawSlot(g, bounds, i, slots[i]);
    }
  }

  private static Rectangle slotBounds(int index, int startX, int startY, int columns) {
    int col = index % columns;
    int row = index / columns;

    return new Rectangle(
      startX + col * (SLOT_WIDTH + SLOT_GAP),
      startY + row * (SLOT_HEIGHT + SLOT_GAP),
      SLOT_WIDTH,
      SLOT_HEIGHT);
  }

  private static void drawSlot(Graphics2D g, Rectangle bounds, int slotIndex, Item item) {
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

  private static void drawCenteredString(Graphics2D g, String text, Rectangle bounds) {
    FontMetrics fm = g.getFontMetrics();
    int textX = bounds.x + (bounds.width - fm.stringWidth(text)) / 2;
    int textY = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();
    g.drawString(text, textX, textY);
  }

  private static String fitToWidth(String text, FontMetrics fm, int maxWidth) {
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
}
