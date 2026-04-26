package contrib.hud.dialogs.frame;

import core.Game;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for rendering styled dialog frames and components.
 *
 * <p>This class provides static methods to render dialog overlays with rounded frames, titles,
 * wrapped text, and buttons.
 *
 * <p>It manages graphics state transitions and applies consistent styling throughout the dialog UI.
 */
public final class DialogFrameRenderer {

  private static final float BACKDROP_ALPHA = 0.35f;
  private static final Color PANEL_FILL = new Color(32, 32, 40, 235);
  private static final Color PANEL_BORDER = new Color(180, 180, 210);
  private static final Color BUTTON_FILL = new Color(75, 95, 140);
  private static final Color BUTTON_PRESSED_FILL = new Color(105, 135, 190);

  private DialogFrameRenderer() {}

  /**
   * Initializes graphics rendering for a dialog by applying a semi-transparent black backdrop.
   *
   * <p>This method saves the current graphics state and applies a dark overlay to the entire
   * screen. The saved state should be restored using {@link #finishDialog(Graphics2D,
   * RenderState)}.
   *
   * @param g the Graphics2D object to modify
   * @return the saved graphics state for later restoration
   */
  public static RenderState beginDialog(Graphics2D g) {
    Composite oldComposite = g.getComposite();
    Color oldColor = g.getColor();
    Font oldFont = g.getFont();

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, BACKDROP_ALPHA));
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, Game.windowWidth(), Game.windowHeight());

    return new RenderState(oldComposite, oldColor, oldFont);
  }

  /**
   * Restores the graphics state after dialog rendering.
   *
   * @param g the Graphics2D object to restore
   * @param state the saved graphics state from {@link #beginDialog(Graphics2D)}
   */
  public static void finishDialog(Graphics2D g, RenderState state) {
    g.setComposite(state.composite());
    g.setColor(state.color());
    g.setFont(state.font());
  }

  /**
   * Draws a rounded dialog frame with a title.
   *
   * @param g the Graphics2D object to draw with
   * @param x the x coordinate of the frame's top-left corner
   * @param y the y coordinate of the frame's top-left corner
   * @param width the width of the frame
   * @param height the height of the frame
   * @param title the title text to display
   * @return the y coordinate where content should begin rendering (after the title)
   */
  public static int drawFrameAndTitle(
      Graphics2D g, int x, int y, int width, int height, String title) {
    g.setComposite(AlphaComposite.SrcOver);
    g.setColor(PANEL_FILL);
    g.fillRoundRect(x, y, width, height, DialogFrameMetrics.ARC, DialogFrameMetrics.ARC);

    g.setColor(PANEL_BORDER);
    g.drawRoundRect(x, y, width, height, DialogFrameMetrics.ARC, DialogFrameMetrics.ARC);

    g.setColor(Color.WHITE);
    g.setFont(g.getFont().deriveFont(Font.BOLD, 18f));
    g.drawString(title, x + DialogFrameMetrics.PADDING, y + 32);

    g.setFont(g.getFont().deriveFont(15f));
    return y + 62;
  }

  /**
   * Draws text with automatic line wrapping within a maximum width.
   *
   * @param g the Graphics2D object to draw with
   * @param text the text to draw (may contain newlines)
   * @param x the x coordinate where text begins
   * @param startY the y coordinate where text begins
   * @param maxWidth the maximum width for text lines
   * @return the y coordinate where text rendering ended
   */
  public static int drawWrappedText(Graphics2D g, String text, int x, int startY, int maxWidth) {
    FontMetrics fm = g.getFontMetrics();
    int y = startY;

    for (String line : wrapText(text, fm, maxWidth)) {
      g.drawString(line, x, y);
      y += fm.getHeight();
    }

    return y;
  }

  /**
   * Draws a styled button with a text label.
   *
   * @param g the Graphics2D object to draw with
   * @param bounds the rectangular bounds of the button
   * @param label the text label for the button
   * @param pressed whether the button is in a pressed state
   */
  public static void drawButton(Graphics2D g, Rectangle bounds, String label, boolean pressed) {
    g.setColor(pressed ? BUTTON_PRESSED_FILL : BUTTON_FILL);
    g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

    g.setColor(Color.WHITE);
    g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 10, 10);

    FontMetrics fm = g.getFontMetrics();
    int tx = bounds.x + (bounds.width - fm.stringWidth(label)) / 2;
    int ty = bounds.y + ((bounds.height - fm.getHeight()) / 2) + fm.getAscent();
    g.drawString(label, tx, ty);
  }

  /**
   * Calculates button positions for a centered row of buttons within a dialog.
   *
   * @param dialogX the x coordinate of the dialog's top-left corner
   * @param dialogY the y coordinate of the dialog's top-left corner
   * @param dialogWidth the width of the dialog
   * @param dialogHeight the height of the dialog
   * @param buttonCount the number of buttons to arrange
   * @param buttonGap the spacing between buttons
   * @return a list of rectangles representing the bounds of each button
   */
  public static List<Rectangle> centeredButtonRow(
      int dialogX, int dialogY, int dialogWidth, int dialogHeight, int buttonCount, int buttonGap) {
    List<Rectangle> bounds = new ArrayList<>();
    if (buttonCount <= 0) {
      return bounds;
    }

    int totalWidth = buttonCount * DialogFrameMetrics.BUTTON_WIDTH + (buttonCount - 1) * buttonGap;
    int startX = dialogX + (dialogWidth - totalWidth) / 2;
    int by =
        dialogY
            + dialogHeight
            - DialogFrameMetrics.BUTTON_HEIGHT
            - DialogFrameMetrics.BUTTON_BOTTOM_MARGIN;

    for (int i = 0; i < buttonCount; i++) {
      bounds.add(
          new Rectangle(
              startX + i * (DialogFrameMetrics.BUTTON_WIDTH + buttonGap),
              by,
              DialogFrameMetrics.BUTTON_WIDTH,
              DialogFrameMetrics.BUTTON_HEIGHT));
    }

    return bounds;
  }

  /**
   * Wraps text into multiple lines based on a maximum width constraint.
   *
   * @param text the text to wrap (may contain newlines to preserve paragraph breaks)
   * @param fm the FontMetrics to use for measuring text width
   * @param maxWidth the maximum width for each line
   * @return a list of text lines, each fitting within the maximum width
   */
  public static List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
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

  /**
   * Represents saved graphics rendering state.
   *
   * @param composite the saved composite setting
   * @param color the saved color setting
   * @param font the saved font setting
   */
  public record RenderState(Composite composite, Color color, Font font) {}
}
