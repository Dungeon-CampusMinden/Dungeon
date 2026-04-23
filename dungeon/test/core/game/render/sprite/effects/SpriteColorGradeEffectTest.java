package core.game.render.sprite.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/** Tests for sprite color grading cache behavior. */
class SpriteColorGradeEffectTest {

  @Test
  void applyInvalidatesCachedResultWhenSourcePixelsChange() {
    SpriteColorGradeEffect effect = new SpriteColorGradeEffect(-1.0f, 1.0f, 1.0f);
    BufferedImage source = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    source.setRGB(0, 0, 0xFFFF0000);

    BufferedImage first = effect.apply(source, 0L);
    source.setRGB(0, 0, 0xFF0000FF);
    BufferedImage second = effect.apply(source, 0L);

    assertEquals(0xFFFF0000, first.getRGB(0, 0));
    assertEquals(0xFF0000FF, second.getRGB(0, 0));
  }
}
