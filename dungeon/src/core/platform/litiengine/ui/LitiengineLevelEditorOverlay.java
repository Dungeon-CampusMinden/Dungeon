package core.platform.litiengine.ui;

import core.ui.overlay.LitiengineUiOverlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;

/**
 * Minimal status overlay for the LITIENGINE level editor shell.
 *
 * <p>This intentionally only visualizes editor state, selected mode and feedback messages.
 * Actual editing logic is added in follow-up commits.
 */
public final class LitiengineLevelEditorOverlay implements LitiengineUiOverlay {

  private static final int DEFAULT_WIDTH = 640;
  private static final int DEFAULT_HEIGHT = 230;
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

  private String title = "LITIENGINE Level Editor";
  private List<String> lines = List.of();
  private String feedback = "";
  private Color feedbackColor = Color.WHITE;

  /**
   * Updates the currently displayed editor information.
   *
   * @param title overlay title
   * @param lines main status lines
   * @param feedback optional feedback message
   * @param feedbackColor color for the feedback message
   */
  public void content(String title, List<String> lines, String feedback, Color feedbackColor) {
    this.title = title == null ? "" : title;
    this.lines = lines == null ? List.of() : List.copyOf(lines);
    this.feedback = feedback == null ? "" : feedback;
    this.feedbackColor = feedbackColor == null ? Color.WHITE : feedbackColor;
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
      g2.setFont(g2.getFont().deriveFont(Font.BOLD, 18f));
      g2.drawString(title, x + PADDING, y + 28);

      g2.setFont(g2.getFont().deriveFont(14f));
      FontMetrics fm = g2.getFontMetrics();

      int lineY = y + 54;
      for (String line : lines) {
        g2.setColor(TEXT_COLOR);
        g2.drawString(line, x + PADDING, lineY);
        lineY += fm.getHeight();
      }

      if (!feedback.isBlank()) {
        lineY += 6;
        g2.setColor(feedbackColor);
        g2.drawString(feedback, x + PADDING, Math.min(lineY, y + height - PADDING));
      }
    } finally {
      g2.dispose();
    }
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
