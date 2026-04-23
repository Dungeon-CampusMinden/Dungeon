package core.render.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/** Tests for image effect rendering helpers. */
class ImageEffectsTest {

  @Test
  void outlinedSpriteUsesUpdatedSourcePixelsForCachedTint() {
    BufferedImage sprite = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    BufferedImage canvas = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);

    drawOutline(canvas, sprite);
    assertEquals(0x00000000, canvas.getRGB(0, 1));

    sprite.setRGB(0, 0, 0xFF000000);
    drawOutline(canvas, sprite);

    assertEquals(0xFFFF0000, canvas.getRGB(0, 1));
  }

  private static void drawOutline(final BufferedImage canvas, final BufferedImage sprite) {
    Graphics2D graphics = canvas.createGraphics();
    try {
      graphics.setBackground(new Color(0, 0, 0, 0));
      graphics.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
      ImageEffects.drawOutlinedSprite(graphics, sprite, 1, 1, 1, 1, Color.RED, 1);
    } finally {
      graphics.dispose();
    }
  }
}
