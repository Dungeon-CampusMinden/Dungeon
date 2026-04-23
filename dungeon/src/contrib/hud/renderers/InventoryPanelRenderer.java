package contrib.hud.renderers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** Shared rendering helpers for inventory-like panel backgrounds. */
public final class InventoryPanelRenderer {

  private static final Color PANEL_FILL = new Color(62, 62, 99, 96);
  private static final Color PANEL_OUTLINE = new Color(0x9dc1ebff, true);

  private InventoryPanelRenderer() {}

  /**
   * Calculates panel bounds around a grid with symmetric padding.
   *
   * @param gridX the grid's left coordinate
   * @param gridY the grid's top coordinate
   * @param gridWidth the grid width
   * @param gridHeight the grid height
   * @param padding the panel padding around the grid
   * @return the panel bounds
   */
  public static Rectangle panelBounds(
      int gridX, int gridY, int gridWidth, int gridHeight, int padding) {
    return new Rectangle(
        gridX - padding, gridY - padding, gridWidth + 2 * padding, gridHeight + 2 * padding);
  }

  /**
   * Draws the shared inventory panel background.
   *
   * @param g the graphics context
   * @param bounds the panel bounds
   */
  public static void drawPanelBackground(Graphics2D g, Rectangle bounds) {
    if (g == null || bounds == null) {
      return;
    }

    g.setColor(PANEL_FILL);
    g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);

    g.setColor(PANEL_OUTLINE);
    g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
  }
}
