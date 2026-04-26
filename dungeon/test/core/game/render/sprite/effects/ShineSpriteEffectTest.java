package core.game.render.sprite.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import core.game.render.sprite.effects.shine.ShineSpriteEffect;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/** Tests for shine overlay cache behavior. */
class ShineSpriteEffectTest {

  @Test
  void createOverlayInvalidatesCachedMaskWhenSourceAlphaChanges() {
    ShineSpriteEffect effect = new ShineSpriteEffect();
    BufferedImage source = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);

    BufferedImage transparentOverlay = effect.createOverlay(source, 0L);

    fill(source, 0xFF000000);
    BufferedImage opaqueOverlay = effect.createOverlay(source, 0L);

    assertEquals(0, nonTransparentPixels(transparentOverlay));
    assertTrue(nonTransparentPixels(opaqueOverlay) > 0);
  }

  private static void fill(final BufferedImage image, final int argb) {
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        image.setRGB(x, y, argb);
      }
    }
  }

  private static int nonTransparentPixels(final BufferedImage image) {
    int count = 0;
    for (int y = 0; y < image.getHeight(); y++) {
      for (int x = 0; x < image.getWidth(); x++) {
        if (((image.getRGB(x, y) >>> 24) & 0xFF) > 0) {
          count++;
        }
      }
    }
    return count;
  }
}
