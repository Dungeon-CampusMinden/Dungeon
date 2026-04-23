package core.game.render.sprite.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/** Tests for hue remap cache behavior. */
class HueRemapSpriteEffectTest {

  @Test
  void applyInvalidatesCachedResultWhenSourcePixelsChange() {
    HueRemapSpriteEffect effect = new HueRemapSpriteEffect(0.0f, 1.0f / 3.0f, 0.01f);
    BufferedImage source = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    source.setRGB(0, 0, 0xFFFF0000);

    BufferedImage first = effect.apply(source, 0L);
    source.setRGB(0, 0, 0xFF0000FF);
    BufferedImage second = effect.apply(source, 0L);

    assertEquals(0xFF00FF00, first.getRGB(0, 0));
    assertEquals(0xFF0000FF, second.getRGB(0, 0));
  }
}
