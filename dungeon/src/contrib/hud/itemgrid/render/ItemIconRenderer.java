package contrib.hud.itemgrid.render;

import contrib.item.Item;
import core.game.render.image.ImageFrameResolver;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/** Shared item icon rendering helpers for inventory-like HUD components. */
public final class ItemIconRenderer {

  private ItemIconRenderer() {}

  /**
   * Resolves the current inventory icon frame for an item.
   *
   * @param item the item whose icon should be resolved
   * @return the resolved icon image or {@code null} if no icon is available
   */
  public static BufferedImage resolveItemIcon(Item item) {
    if (item == null || item.inventoryAnimation() == null) {
      return null;
    }

    try {
      return ImageFrameResolver.toImage(item.inventoryAnimation().update());
    } catch (RuntimeException ignored) {
      return null;
    }
  }

  /**
   * Draws an item icon centered inside the given bounds while preserving aspect ratio.
   *
   * @param g the graphics context
   * @param bounds the target bounds
   * @param item the item whose icon should be rendered
   * @param padding the inner padding inside the bounds
   */
  public static void drawItemIcon(Graphics2D g, Rectangle bounds, Item item, int padding) {
    drawCenteredImage(g, bounds, resolveItemIcon(item), padding);
  }

  /**
   * Draws an image centered inside the given bounds while preserving aspect ratio.
   *
   * @param g the graphics context
   * @param bounds the target bounds
   * @param image the image to render
   * @param padding the inner padding inside the bounds
   */
  public static void drawCenteredImage(
      Graphics2D g, Rectangle bounds, BufferedImage image, int padding) {
    if (g == null || bounds == null || image == null) {
      return;
    }

    int maxWidth = bounds.width - 2 * padding;
    int maxHeight = bounds.height - 2 * padding;
    if (maxWidth <= 0 || maxHeight <= 0) {
      return;
    }

    double scale =
        Math.min(
            maxWidth / (double) Math.max(1, image.getWidth()),
            maxHeight / (double) Math.max(1, image.getHeight()));

    int drawWidth = Math.max(1, (int) Math.round(image.getWidth() * scale));
    int drawHeight = Math.max(1, (int) Math.round(image.getHeight() * scale));
    int drawX = bounds.x + (bounds.width - drawWidth) / 2;
    int drawY = bounds.y + (bounds.height - drawHeight) / 2;

    g.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
  }
}
