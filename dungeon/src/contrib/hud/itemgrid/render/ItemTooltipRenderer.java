package contrib.hud.itemgrid.render;

import contrib.hud.UIUtils;
import contrib.item.Item;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * A utility class for rendering item tooltips.
 *
 * <p>This class provides methods to draw styled tooltips for items, displaying their name and
 * description.
 *
 * <p>Tooltips are automatically positioned to stay within the viewport boundaries and avoid
 * clipping at screen edges.
 */
public final class ItemTooltipRenderer {

  private static final int TOOLTIP_OFFSET_X = 10;
  private static final int TOOLTIP_OFFSET_Y = 10;
  private static final int TOOLTIP_PADDING_X = 8;
  private static final int TOOLTIP_PADDING_Y = 8;
  private static final int TOOLTIP_LINE_GAP = 5;

  private static final Color TOOLTIP_FILL = new Color(255, 255, 255, 240);
  private static final Color TOOLTIP_BORDER = new Color(0x9dc1ebff, true);
  private static final Color TITLE_COLOR = Color.BLACK;
  private static final Color DESCRIPTION_COLOR = new Color(0x282828FF, false);

  private ItemTooltipRenderer() {}

  /**
   * Draws an item tooltip at the specified mouse position.
   *
   * <p>The tooltip displays the item's display name and description. It automatically adjusts its
   * position to remain visible within the viewport, repositioning to the left or top if it extends
   * beyond the right or bottom edges.
   *
   * @param g the Graphics2D object to draw with
   * @param item the item to display a tooltip for (might be null)
   * @param mouseX the mouse x coordinate
   * @param mouseY the mouse y coordinate
   * @param viewportWidth the viewport width in pixels
   * @param viewportHeight the viewport height in pixels
   */
  public static void drawTooltip(
      Graphics2D g, Item item, int mouseX, int mouseY, int viewportWidth, int viewportHeight) {
    if (g == null || item == null) {
      return;
    }

    String itemTitle = displayName(item);
    String formattedDescription =
        UIUtils.formatString(item.description() == null ? "" : item.description());

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

    if (tooltipX + tooltipWidth > viewportWidth) {
      tooltipX = mouseX - tooltipWidth - TOOLTIP_OFFSET_X;
    }

    if (tooltipY + tooltipHeight > viewportHeight) {
      tooltipY = mouseY - tooltipHeight - TOOLTIP_OFFSET_Y;
    }

    tooltipX = Math.max(0, tooltipX);
    tooltipY = Math.max(0, tooltipY);

    g.setColor(TOOLTIP_FILL);
    g.fillRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight);

    g.setColor(TOOLTIP_BORDER);
    g.drawRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight);

    int textX = tooltipX + TOOLTIP_PADDING_X;
    int baselineY = tooltipY + TOOLTIP_PADDING_Y + metrics.getAscent();

    g.setColor(TITLE_COLOR);
    g.drawString(itemTitle, textX, baselineY);

    if (descriptionLines.length > 0) {
      g.setColor(DESCRIPTION_COLOR);
      baselineY += TOOLTIP_LINE_GAP + metrics.getHeight();
      for (String line : descriptionLines) {
        g.drawString(line, textX, baselineY);
        baselineY += metrics.getHeight();
      }
    }
  }

  /**
   * Gets the display name for an item.
   *
   * <p>If the item has a display name, it is returned. Otherwise, the simple class name of the item
   * is returned as a fallback.
   *
   * @param item the item to get the display name for (might be null)
   * @return the display name or an empty string if the item is null
   */
  public static String displayName(Item item) {
    if (item == null) {
      return "";
    }

    String displayName = item.displayName();
    return displayName == null || displayName.isBlank()
        ? item.getClass().getSimpleName()
        : displayName;
  }
}
