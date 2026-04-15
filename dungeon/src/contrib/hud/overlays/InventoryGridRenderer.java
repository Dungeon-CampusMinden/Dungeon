package contrib.hud.overlays;

import contrib.item.Item;
import core.render.AnimationFrameImages;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * A utility class for rendering inventory grids with items.
 *
 * <p>This class provides static methods to render and manage a grid-based inventory display,
 * including slot calculations, item rendering, stack indicators, and mouse interaction support.
 *
 * <p>It handles item icons, stack sizes, and provides visual feedback for empty and filled slots.
 */
public final class InventoryGridRenderer {

  public static final int MAX_COLUMNS = 6;
  public static final int SLOT_WIDTH = 78;
  public static final int SLOT_HEIGHT = 78;
  public static final int SLOT_GAP = 8;
  public static final int GRID_TOP_GAP = 12;

  private static final int ITEM_ICON_PADDING = 8;
  private static final int STACK_PADDING = 5;

  private static final Color SLOT_FILL = new Color(62, 62, 99, 185);
  private static final Color EMPTY_SLOT_FILL = new Color(46, 46, 74, 150);
  private static final Color SLOT_BORDER = new Color(0x9dc1ebff, true);
  private static final Color STACK_TEXT = Color.WHITE;
  private static final Color STACK_SHADOW = new Color(0, 0, 0, 176);

  private InventoryGridRenderer() {}

  /**
   * Calculates the optimal number of columns for displaying the given slots.
   *
   * @param slots the inventory slots to arrange
   * @return the number of columns (between 1 and MAX_COLUMNS)
   */
  public static int columnsFor(Item[] slots) {
    return Math.clamp(slots.length, 1, MAX_COLUMNS);
  }

  /**
   * Calculates the number of rows needed to display the given slots.
   *
   * @param slots the inventory slots to arrange
   * @param columns the number of columns per row
   * @return the number of rows (at least 1)
   */
  public static int rowsFor(Item[] slots, int columns) {
    return Math.max(1, (Math.max(1, slots.length) + columns - 1) / columns);
  }

  /**
   * Calculates the total width of a grid with the specified number of columns.
   *
   * @param columns the number of columns
   * @return the total width in pixels
   */
  public static int gridWidth(int columns) {
    return columns * SLOT_WIDTH + Math.max(0, columns - 1) * SLOT_GAP;
  }

  /**
   * Calculates the total height of a grid with the specified number of rows.
   *
   * @param rows the number of rows
   * @return the total height in pixels
   */
  public static int gridHeight(int rows) {
    return rows * SLOT_HEIGHT + Math.max(0, rows - 1) * SLOT_GAP;
  }

  /**
   * Draws the complete inventory grid with all slots and items.
   *
   * @param g the Graphics2D object to draw with
   * @param slots the inventory slots to render
   * @param startX the x coordinate of the grid's top-left corner
   * @param startY the y coordinate of the grid's top-left corner
   * @param columns the number of columns in the grid
   */
  public static void drawGrid(Graphics2D g, Item[] slots, int startX, int startY, int columns) {
    for (int i = 0; i < slots.length; i++) {
      Rectangle bounds = slotBounds(i, startX, startY, columns);
      drawSlot(g, bounds, slots[i]);
    }
  }

  /**
   * Renders a preview of an item in an inventory slot at the specified location.
   *
   * @param g the Graphics2D object used for rendering; must not be null
   * @param x the x-coordinate of the top-left corner of the preview location
   * @param y the y-coordinate of the top-left corner of the preview location
   * @param item the item to render a preview for; if null, no item is rendered
   */
  public static void drawItemPreview(Graphics2D g, int x, int y, Item item) {
    if (g == null || item == null) {
      return;
    }

    Rectangle bounds = new Rectangle(x, y, SLOT_WIDTH, SLOT_HEIGHT);

    BufferedImage icon = resolveItemIcon(item);
    if (icon != null) {
      drawItemIcon(g, bounds, icon);
    }

    drawStackSize(g, bounds, item);
  }

  /**
   * Calculates the rectangular bounds of a specific slot in the grid.
   *
   * @param index the slot index
   * @param startX the x coordinate of the grid's top-left corner
   * @param startY the y coordinate of the grid's top-left corner
   * @param columns the number of columns in the grid
   * @return a Rectangle representing the slot's bounds
   */
  public static Rectangle slotBounds(int index, int startX, int startY, int columns) {
    int col = index % columns;
    int row = index / columns;

    return new Rectangle(
      startX + col * (SLOT_WIDTH + SLOT_GAP),
      startY + row * (SLOT_HEIGHT + SLOT_GAP),
      SLOT_WIDTH,
      SLOT_HEIGHT);
  }

  /**
   * Finds the inventory slot index at the specified mouse coordinates.
   *
   * @param mouseX the mouse x coordinate
   * @param mouseY the mouse y coordinate
   * @param slots the inventory slots array
   * @param startX the x coordinate of the grid's top-left corner
   * @param startY the y coordinate of the grid's top-left corner
   * @param columns the number of columns in the grid
   * @return the slot index if a slot is found at the coordinates, otherwise -1
   */
  public static int findSlotIndexAt(
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
      return AnimationFrameImages.toImage(item.inventoryAnimation().update());
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
