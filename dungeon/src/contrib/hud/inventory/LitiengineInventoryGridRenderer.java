package contrib.hud.inventory;

import contrib.components.InventoryComponent;
import contrib.item.Item;
import core.game.render.LitiengineAnimationFrames;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Shared visual renderer for inventory slot grids in LITIENGINE overlays.
 *
 * <p>This renderer intentionally mimics the legacy libGDX inventory styling more closely while
 * keeping the current LITIENGINE overlay architecture.
 */
final class LitiengineInventoryGridRenderer {

  static final int MAX_COLUMNS = 6;
  static final int SLOT_WIDTH = 78;
  static final int SLOT_HEIGHT = 78;
  static final int SLOT_GAP = 8;
  static final int INFO_LINE_GAP = 18;
  static final int GRID_TOP_GAP = 12;

  private static final int ITEM_ICON_PADDING = 8;
  private static final int STACK_PADDING = 5;

  private static final Color INFO_COLOR = Color.WHITE;
  private static final Color SLOT_FILL = new Color(62, 62, 99, 185);
  private static final Color EMPTY_SLOT_FILL = new Color(46, 46, 74, 150);
  private static final Color SLOT_BORDER = new Color(0x9dc1ebff, true);
  private static final Color STACK_TEXT = Color.WHITE;
  private static final Color STACK_SHADOW = new Color(0, 0, 0, 176);

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
    g.setColor(INFO_COLOR);
    g.drawString(
      inventory.count() + " / " + slots.length + " slots used",
      x,
      y + g.getFontMetrics().getAscent());
  }

  static void drawGrid(Graphics2D g, Item[] slots, int startX, int startY, int columns) {
    for (int i = 0; i < slots.length; i++) {
      Rectangle bounds = slotBounds(i, startX, startY, columns);
      drawSlot(g, bounds, slots[i]);
    }
  }

  static Rectangle slotBounds(int index, int startX, int startY, int columns) {
    int col = index % columns;
    int row = index / columns;

    return new Rectangle(
      startX + col * (SLOT_WIDTH + SLOT_GAP),
      startY + row * (SLOT_HEIGHT + SLOT_GAP),
      SLOT_WIDTH,
      SLOT_HEIGHT);
  }

  static int findSlotIndexAt(
    int mouseX, int mouseY, Item[] slots, int startX, int startY, int columns) {
    for (int i = 0; i < slots.length; i++) {
      if (slotBounds(i, startX, startY, columns).contains(mouseX, mouseY)) {
        return i;
      }
    }
    return -1;
  }

  private static void drawSlot(Graphics2D g, Rectangle bounds, Item item) {
    g.setColor(item == null ? EMPTY_SLOT_FILL : SLOT_FILL);
    g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

    g.setColor(SLOT_BORDER);
    g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);

    if (item == null) {
      return;
    }

    BufferedImage icon = resolveItemIcon(item);
    if (icon != null) {
      drawItemIcon(g, bounds, icon);
    }

    drawStackSize(g, bounds, item);
  }

  private static BufferedImage resolveItemIcon(Item item) {
    if (item == null || item.inventoryAnimation() == null) {
      return null;
    }

    try {
      return LitiengineAnimationFrames.toImage(item.inventoryAnimation().update());
    } catch (RuntimeException ignored) {
      return null;
    }
  }

  private static void drawItemIcon(Graphics2D g, Rectangle bounds, BufferedImage icon) {
    int maxWidth = bounds.width - 2 * ITEM_ICON_PADDING;
    int maxHeight = bounds.height - 2 * ITEM_ICON_PADDING;

    if (maxWidth <= 0 || maxHeight <= 0) {
      return;
    }

    double scale =
      Math.min(
        maxWidth / (double) Math.max(1, icon.getWidth()),
        maxHeight / (double) Math.max(1, icon.getHeight()));

    int drawWidth = Math.max(1, (int) Math.round(icon.getWidth() * scale));
    int drawHeight = Math.max(1, (int) Math.round(icon.getHeight() * scale));

    int drawX = bounds.x + (bounds.width - drawWidth) / 2;
    int drawY = bounds.y + (bounds.height - drawHeight) / 2;

    g.drawImage(icon, drawX, drawY, drawWidth, drawHeight, null);
  }

  private static void drawStackSize(Graphics2D g, Rectangle bounds, Item item) {
    if (item.stackSize() <= 1) {
      return;
    }

    String stackText = Integer.toString(item.stackSize());

    Font oldFont = g.getFont();
    g.setFont(oldFont.deriveFont(Font.BOLD, 12f));

    FontMetrics fm = g.getFontMetrics();
    int textX = bounds.x + bounds.width - fm.stringWidth(stackText) - STACK_PADDING;
    int textY = bounds.y + fm.getAscent() + STACK_PADDING;

    g.setColor(STACK_SHADOW);
    g.drawString(stackText, textX + 1, textY + 1);

    g.setColor(STACK_TEXT);
    g.drawString(stackText, textX, textY);

    g.setFont(oldFont);
  }
}
