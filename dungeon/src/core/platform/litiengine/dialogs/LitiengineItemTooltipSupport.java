package core.platform.litiengine.dialogs;

import contrib.hud.UIUtils;
import contrib.item.Item;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/**
 * Shared tooltip/text helper for LITIENGINE inventory-style overlays.
 *
 * <p>This keeps identical item tooltip rendering logic out of the individual inventory overlays.
 */
final class LitiengineItemTooltipSupport {

  private static final int TOOLTIP_OFFSET_X = 12;
  private static final int TOOLTIP_OFFSET_Y = 14;
  private static final int TOOLTIP_PADDING_X = 12;
  private static final int TOOLTIP_PADDING_Y = 10;
  private static final int TOOLTIP_LINE_GAP = 6;
  private static final int TOOLTIP_CORNER_RADIUS = 10;

  private LitiengineItemTooltipSupport() {}

  static void drawTooltip(
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

  static String displayName(Item item) {
    if (item == null) {
      return "";
    }

    String displayName = item.displayName();
    return displayName == null || displayName.isBlank()
      ? item.getClass().getSimpleName()
      : displayName;
  }
}
