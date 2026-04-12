package core.ui.dialogs;

import core.Game;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.AlphaComposite;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared rendering/layout helper for simple LITIENGINE dialog overlays.
 *
 * <p>Keeps duplicated panel, title, text wrapping and button drawing logic out of the concrete
 * OK/YES_NO/TEXT overlays.
 */
final class LitiengineDialogOverlaySupport {

  static final int ARC = 14;
  static final int PADDING = 20;
  static final int BUTTON_WIDTH = 120;
  static final int BUTTON_HEIGHT = 34;
  static final int BUTTON_BOTTOM_MARGIN = 18;

  private static final float BACKDROP_ALPHA = 0.35f;
  private static final Color PANEL_FILL = new Color(32, 32, 40, 235);
  private static final Color PANEL_BORDER = new Color(180, 180, 210);
  private static final Color BUTTON_FILL = new Color(75, 95, 140);
  private static final Color BUTTON_PRESSED_FILL = new Color(105, 135, 190);

  private LitiengineDialogOverlaySupport() {}

  static RenderState beginDialog(Graphics2D g) {
    Composite oldComposite = g.getComposite();
    Color oldColor = g.getColor();
    Font oldFont = g.getFont();

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, BACKDROP_ALPHA));
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, Game.windowWidth(), Game.windowHeight());

    return new RenderState(oldComposite, oldColor, oldFont);
  }

  static void finishDialog(Graphics2D g, RenderState state) {
    g.setComposite(state.composite());
    g.setColor(state.color());
    g.setFont(state.font());
  }

  static int drawFrameAndTitle(Graphics2D g, int x, int y, int width, int height, String title) {
    g.setComposite(AlphaComposite.SrcOver);
    g.setColor(PANEL_FILL);
    g.fillRoundRect(x, y, width, height, ARC, ARC);

    g.setColor(PANEL_BORDER);
    g.drawRoundRect(x, y, width, height, ARC, ARC);

    g.setColor(Color.WHITE);
    g.setFont(g.getFont().deriveFont(Font.BOLD, 18f));
    g.drawString(title, x + PADDING, y + 32);

    g.setFont(g.getFont().deriveFont(15f));
    return y + 62;
  }

  static int drawWrappedText(Graphics2D g, String text, int x, int startY, int maxWidth) {
    FontMetrics fm = g.getFontMetrics();
    int y = startY;

    for (String line : wrapText(text, fm, maxWidth)) {
      g.drawString(line, x, y);
      y += fm.getHeight();
    }

    return y;
  }

  static void drawButton(Graphics2D g, Rectangle bounds, String label, boolean pressed) {
    g.setColor(pressed ? BUTTON_PRESSED_FILL : BUTTON_FILL);
    g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

    g.setColor(Color.WHITE);
    g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

    FontMetrics fm = g.getFontMetrics();
    int tx = bounds.x + (bounds.width - fm.stringWidth(label)) / 2;
    int ty = bounds.y + ((bounds.height - fm.getHeight()) / 2) + fm.getAscent();
    g.drawString(label, tx, ty);
  }

  static List<Rectangle> centeredButtonRow(
    int dialogX, int dialogY, int dialogWidth, int dialogHeight, int buttonCount, int buttonGap) {
    List<Rectangle> bounds = new ArrayList<>();
    if (buttonCount <= 0) {
      return bounds;
    }

    int totalWidth = buttonCount * BUTTON_WIDTH + (buttonCount - 1) * buttonGap;
    int startX = dialogX + (dialogWidth - totalWidth) / 2;
    int by = dialogY + dialogHeight - BUTTON_HEIGHT - BUTTON_BOTTOM_MARGIN;

    for (int i = 0; i < buttonCount; i++) {
      bounds.add(
        new Rectangle(
          startX + i * (BUTTON_WIDTH + buttonGap), by, BUTTON_WIDTH, BUTTON_HEIGHT));
    }

    return bounds;
  }

  static List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
    List<String> lines = new ArrayList<>();
    if (text == null || text.isBlank()) {
      lines.add("");
      return lines;
    }

    for (String paragraph : text.split("\n")) {
      if (paragraph.isBlank()) {
        lines.add("");
        continue;
      }

      String[] words = paragraph.trim().split("\\s+");
      StringBuilder current = new StringBuilder();

      for (String word : words) {
        String candidate = current.isEmpty() ? word : current + " " + word;
        if (fm.stringWidth(candidate) <= maxWidth) {
          current.setLength(0);
          current.append(candidate);
        } else {
          if (!current.isEmpty()) {
            lines.add(current.toString());
          }
          current.setLength(0);
          current.append(word);
        }
      }

      if (!current.isEmpty()) {
        lines.add(current.toString());
      }
    }

    return lines;
  }

  record RenderState(Composite composite, Color color, Font font) {}
}
