package contrib.hud.dialogs.showimage;

import contrib.hud.frame.DialogFrameMetrics;
import contrib.hud.frame.DialogFrameRenderer;
import core.Game;
import core.game.render.image.ImageAssets;
import core.ui.overlay.BaseUiOverlay;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Represents an overlay that displays an image with optional text and animated transition effects.
 *
 * <p>This class extends {@link BaseUiOverlay} to allow rendering within the user interface layer.
 *
 * <p>It supports customizable scaling, positioning, and transition animations for the image.
 */
final class ShowImageDialogOverlay extends BaseUiOverlay {

  private static final float DEFAULT_MAX_SIZE = 0.85f;
  private static final int PANEL_PADDING = 12;
  private static final int PANEL_ARC = 14;
  private static final int ANIMATION_OFFSET_X = -5;
  private static final int ANIMATION_OFFSET_Y = -50;

  private static final int TEXT_SIDE_PADDING = 24;
  private static final int TEXT_VERTICAL_PADDING = 12;
  private static final int TEXT_ARC = 12;

  private final String imagePath;
  private final TransitionSpeed transitionSpeed;
  private final float maxSize;
  private final String imageText;
  private final float imageTextScale;
  private final Color imageTextColor;

  private String loadedImagePath;
  private BufferedImage image;

  private float animation;

  ShowImageDialogOverlay(
    String imagePath,
    TransitionSpeed transitionSpeed,
    float maxSize,
    String imageText,
    float imageTextScale,
    int imageTextColorRgba8888) {
    super(480, 320);

    this.imagePath = imagePath;
    this.transitionSpeed = transitionSpeed == null ? TransitionSpeed.MEDIUM : transitionSpeed;
    this.maxSize = maxSize > 0f ? maxSize : DEFAULT_MAX_SIZE;
    this.imageText = normalizeText(imageText);
    this.imageTextScale = imageTextScale > 0f ? imageTextScale : 1f;
    this.imageTextColor = awtColorFromRgba8888(imageTextColorRgba8888);

    this.animation = this.transitionSpeed == TransitionSpeed.DISABLED ? 1f : 0f;
  }

  @Override
  public void render(Graphics2D g) {
    if (!visible) {
      return;
    }

    ensureImageLoaded();

    DialogFrameRenderer.RenderState state =
      DialogFrameRenderer.beginDialog(g);

    try {
      if (image == null) {
        renderMissingImage(g);
      } else {
        renderImage(g);
      }
    } finally {
      DialogFrameRenderer.finishDialog(g, state);
    }

    advanceAnimation();
  }

  private void ensureImageLoaded() {
    if (imagePath == null || imagePath.isBlank()) {
      image = null;
      loadedImagePath = imagePath;
      return;
    }

    if (imagePath.equals(loadedImagePath)) {
      return;
    }

    loadedImagePath = imagePath;
    image = ImageAssets.get(imagePath);
  }

  private void renderImage(Graphics2D g) {
    int windowWidth = Game.windowWidth();
    int windowHeight = Game.windowHeight();

    float maxWidth = windowWidth * maxSize;
    float maxHeight = windowHeight * maxSize;

    double scale = Math.min(maxWidth / image.getWidth(), maxHeight / image.getHeight());
    if (!(scale > 0d)) {
      scale = 1d;
    }

    int drawWidth = Math.max(1, (int) Math.round(image.getWidth() * scale));
    int drawHeight = Math.max(1, (int) Math.round(image.getHeight() * scale));

    this.width = drawWidth + 2 * PANEL_PADDING;
    this.height = drawHeight + 2 * PANEL_PADDING;

    int baseX = (windowWidth - width) / 2;
    int baseY = (windowHeight - height) / 2;

    this.x = baseX + animationOffsetX();
    this.y = baseY + animationOffsetY();

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, easedAnimation()));
    g.setColor(new Color(20, 20, 26, 235));
    g.fillRoundRect(x, y, width, height, PANEL_ARC, PANEL_ARC);

    int imageX = x + PANEL_PADDING;
    int imageY = y + PANEL_PADDING;

    g.drawImage(image, imageX, imageY, drawWidth, drawHeight, null);

    renderOverlayText(g, imageX, imageY, drawWidth, drawHeight);

    g.setColor(new Color(220, 220, 230));
    g.drawRoundRect(x, y, width, height, PANEL_ARC, PANEL_ARC);
  }

  private void renderOverlayText(Graphics2D g, int imageX, int imageY, int imageWidth, int imageHeight) {
    if (imageText == null || imageText.isBlank()) {
      return;
    }

    Font previousFont = g.getFont();
    Font textFont =
      previousFont.deriveFont(Math.max(14f, previousFont.getSize2D() * imageTextScale));
    g.setFont(textFont);

    FontMetrics fm = g.getFontMetrics();
    int maxTextWidth = Math.max(40, imageWidth - 2 * TEXT_SIDE_PADDING);
    List<String> lines = DialogFrameRenderer.wrapText(imageText, fm, maxTextWidth);

    int textBlockHeight = lines.size() * fm.getHeight();
    int boxWidth = maxTextWidth + TEXT_SIDE_PADDING;
    int boxHeight = textBlockHeight + 2 * TEXT_VERTICAL_PADDING;

    int boxX = imageX + (imageWidth - boxWidth) / 2;
    int boxY = imageY + (imageHeight - boxHeight) / 2;

    g.setColor(new Color(255, 255, 255, 120));
    g.fillRoundRect(boxX, boxY, boxWidth, boxHeight, TEXT_ARC, TEXT_ARC);

    g.setColor(new Color(0, 0, 0, 90));
    g.drawRoundRect(boxX, boxY, boxWidth, boxHeight, TEXT_ARC, TEXT_ARC);

    int lineY = boxY + TEXT_VERTICAL_PADDING + fm.getAscent();
    for (String line : lines) {
      int lineX = imageX + (imageWidth - fm.stringWidth(line)) / 2;

      g.setColor(new Color(0, 0, 0, 180));
      g.drawString(line, lineX + 1, lineY + 1);

      g.setColor(imageTextColor);
      g.drawString(line, lineX, lineY);

      lineY += fm.getHeight();
    }

    g.setFont(previousFont);
  }

  private void renderMissingImage(Graphics2D g) {
    this.width = 500;
    this.height = 150;

    int windowWidth = Game.windowWidth();
    int windowHeight = Game.windowHeight();

    this.x = (windowWidth - width) / 2 + animationOffsetX();
    this.y = (windowHeight - height) / 2 + animationOffsetY();

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, easedAnimation()));

    int textY =
      DialogFrameRenderer.drawFrameAndTitle(g, x, y, width, height, "Image");

    DialogFrameRenderer.drawWrappedText(
      g,
      "Could not load image:\n" + imagePath,
      x + DialogFrameMetrics.PADDING,
      textY,
      width - 2 * DialogFrameMetrics.PADDING);
  }

  private void advanceAnimation() {
    if (animation >= 1f || transitionSpeed == TransitionSpeed.DISABLED) {
      animation = 1f;
      return;
    }

    int frames = (int) Math.max(1, transitionSpeed.framesToComplete);
    animation = Math.min(1f, animation + (1f / frames));
  }

  private float easedAnimation() {
    float t = Math.clamp(animation, 0f, 1f);
    return t * t * (3f - 2f * t);
  }

  private int animationOffsetX() {
    return Math.round(ANIMATION_OFFSET_X * (1f - easedAnimation()));
  }

  private int animationOffsetY() {
    return Math.round(ANIMATION_OFFSET_Y * (1f - easedAnimation()));
  }

  private static String normalizeText(String text) {
    if (text == null) {
      return null;
    }

    String normalized = text.strip();
    return normalized.isEmpty() ? null : normalized;
  }

  private static Color awtColorFromRgba8888(int rgba8888) {
    int r = (rgba8888 >>> 24) & 0xFF;
    int g = (rgba8888 >>> 16) & 0xFF;
    int b = (rgba8888 >>> 8) & 0xFF;
    int a = rgba8888 & 0xFF;
    return new Color(r, g, b, a);
  }
}
