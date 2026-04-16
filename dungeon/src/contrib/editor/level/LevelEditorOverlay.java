package contrib.editor.level;

import core.ui.overlay.UiOverlay;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an overlay UI component for a level editor, providing a dynamically sized panel
 * to display editor-related information, including status lines, a title, and feedback messages.
 *
 * <p>This class implements the {@code UiOverlay} interface, allowing it to be rendered
 * on top of the main game screen and manipulated based on its position, size, and visibility.
 *
 * <p>It is a final class, meaning it cannot be subclassed.
 */
public final class LevelEditorOverlay implements UiOverlay {

  private static final int DEFAULT_WIDTH = 700;
  private static final int DEFAULT_HEIGHT = 300;
  private static final int PADDING = 12;
  private static final int ARC = 12;

  private static final Color PANEL_FILL = new Color(18, 18, 26, 220);
  private static final Color PANEL_BORDER = new Color(210, 210, 225);
  private static final Color TEXT_COLOR = Color.WHITE;

  private int x = 12;
  private int y = 12;
  private int width = DEFAULT_WIDTH;
  private int height = DEFAULT_HEIGHT;
  private boolean visible = true;

  private String title = "";
  private List<String> lines = List.of();
  private String feedback = "";
  private Color feedbackColor = Color.WHITE;

  /**
   * Updates the currently displayed editor information.
   *
   * @param title overlay title, may be blank
   * @param lines main status lines
   * @param feedback optional feedback message
   * @param feedbackColor color for the feedback message
   */
  public void content(String title, List<String> lines, String feedback, Color feedbackColor) {
    this.title = title == null ? "" : title;
    this.lines = lines == null ? List.of() : List.copyOf(lines);
    this.feedback = feedback == null ? "" : feedback;
    this.feedbackColor = feedbackColor == null ? Color.WHITE : feedbackColor;

    int estimatedLineCount = this.lines.size() + feedbackLines().size();
    int estimatedHeight = 90 + estimatedLineCount * 18;
    this.height = Math.max(DEFAULT_HEIGHT, estimatedHeight);
  }

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    Graphics2D g2 = (Graphics2D) g.create();
    try {
      g2.setColor(PANEL_FILL);
      g2.fillRoundRect(x, y, width, height, ARC, ARC);

      g2.setColor(PANEL_BORDER);
      g2.drawRoundRect(x, y, width, height, ARC, ARC);

      g2.setColor(TEXT_COLOR);
      g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 15f));
      FontMetrics fm = g2.getFontMetrics();

      int lineY = y + PADDING + fm.getAscent();

      if (!title.isBlank()) {
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
        g2.setColor(TEXT_COLOR);
        g2.drawString(title, x + PADDING, y + 28);

        g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 15f));
        fm = g2.getFontMetrics();
        lineY = y + 54;
      }

      for (String line : lines) {
        g2.setColor(TEXT_COLOR);
        g2.drawString(line == null ? "" : line, x + PADDING, lineY);
        lineY += fm.getHeight();
      }

      List<String> feedbackLines = feedbackLines();
      if (!feedbackLines.isEmpty()) {
        lineY += 6;
        g2.setColor(feedbackColor);

        for (String line : feedbackLines) {
          if (lineY > y + height - PADDING) {
            break;
          }

          g2.drawString(line, x + PADDING, lineY);
          lineY += fm.getHeight();
        }
      }
    } finally {
      g2.dispose();
    }
  }

  private List<String> feedbackLines() {
    if (feedback == null || feedback.isBlank()) {
      return List.of();
    }

    String[] split = feedback.split("\\R", -1);
    List<String> result = new ArrayList<>(split.length);
    for (String line : split) {
      result.add(line == null ? "" : line);
    }
    return List.copyOf(result);
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
