package core.platform.litiengine.dialogs;

import contrib.hud.elements.Button;
import contrib.hud.elements.ImageButton;
import core.platform.litiengine.render.LitiengineAnimationFrames;
import core.platform.litiengine.render.LitiengineImages;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * LITIENGINE/AWT renderer for backend-neutral HUD buttons.
 *
 * <p>This keeps Graphics2D image loading and painting out of the neutral button model.
 */
final class LitiengineButtonRenderer {

  private static final int ARC = 10;
  private static final int PADDING = 8;
  private static final Color FALLBACK_FILL = new Color(75, 95, 140);
  private static final Color FALLBACK_PRESSED_FILL = new Color(105, 135, 190);
  private static final Color FALLBACK_HOVER_FILL = new Color(90, 115, 165);

  private LitiengineButtonRenderer() {}

  static void draw(Graphics2D g, Button button, String label) {
    if (g == null || button == null) {
      return;
    }

    button.updateFromStage();
    drawBackground(g, button);
    drawForeground(g, button, label);
  }

  private static void drawBackground(Graphics2D g, Button button) {
    String path =
      button.backgroundTexturePath() == null ? null : button.backgroundTexturePath().pathString();

    BufferedImage background = path == null ? null : LitiengineImages.get(path);
    if (background != null) {
      g.drawImage(background, button.x(), button.y(), button.width(), button.height(), null);
      return;
    }

    Color fill =
      switch (button.visualState()) {
        case PRESSED -> FALLBACK_PRESSED_FILL;
        case HOVER -> FALLBACK_HOVER_FILL;
        case IDLE -> FALLBACK_FILL;
      };

    g.setColor(fill);
    g.fillRoundRect(button.x(), button.y(), button.width(), button.height(), ARC, ARC);
    g.setColor(Color.WHITE);
    g.drawRoundRect(button.x(), button.y(), button.width(), button.height(), ARC, ARC);
  }

  private static void drawForeground(Graphics2D g, Button button, String label) {
    int contentLeft = button.x() + PADDING;
    int contentRight = button.x() + button.width() - PADDING;

    BufferedImage icon = imageOf(button);
    if (icon != null) {
      int iconSize = Math.min(button.height() - 10, 18);
      int iconX = contentLeft;
      int iconY = button.y() + (button.height() - iconSize) / 2;
      g.drawImage(icon, iconX, iconY, iconSize, iconSize, null);
      contentLeft = iconX + iconSize + 6;
    }

    if (label == null || label.isBlank()) {
      return;
    }

    g.setColor(Color.WHITE);
    FontMetrics fm = g.getFontMetrics();
    int textWidth = fm.stringWidth(label);
    int availableWidth = Math.max(0, contentRight - contentLeft);
    int tx = contentLeft + Math.max(0, (availableWidth - textWidth) / 2);
    int ty = button.y() + ((button.height() - fm.getHeight()) / 2) + fm.getAscent();
    g.drawString(label, tx, ty);
  }

  private static BufferedImage imageOf(Button button) {
    if (!(button instanceof ImageButton imageButton)) {
      return null;
    }

    return LitiengineAnimationFrames.toImage(imageButton.animation().update());
  }
}
