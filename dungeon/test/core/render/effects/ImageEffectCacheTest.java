package core.render.effects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

import core.game.render.image.ImageEffectCache;
import org.junit.jupiter.api.Test;

/** Tests for the shared image effect cache. */
class ImageEffectCacheTest {

  @Test
  void unchangedSourceAndEffectKeyReusesCachedResult() {
    ImageEffectCache<BufferedImage> cache = new ImageEffectCache<>(16);
    BufferedImage source = image(0xFFFF0000);
    AtomicInteger calls = new AtomicInteger();

    BufferedImage first = cache.getOrCompute(source, "effect", image -> copyPixel(image, calls));
    BufferedImage second = cache.getOrCompute(source, "effect", image -> copyPixel(image, calls));

    assertSame(first, second);
    assertEquals(1, calls.get());
  }

  @Test
  void sourcePixelChangesInvalidateCachedResult() {
    ImageEffectCache<BufferedImage> cache = new ImageEffectCache<>(16);
    BufferedImage source = image(0xFFFF0000);
    AtomicInteger calls = new AtomicInteger();

    BufferedImage first = cache.getOrCompute(source, "effect", image -> copyPixel(image, calls));
    source.setRGB(0, 0, 0xFF00FF00);
    BufferedImage second = cache.getOrCompute(source, "effect", image -> copyPixel(image, calls));

    assertNotSame(first, second);
    assertEquals(0xFFFF0000, first.getRGB(0, 0));
    assertEquals(0xFF00FF00, second.getRGB(0, 0));
    assertEquals(2, calls.get());
  }

  @Test
  void perSourceEntriesAreBounded() {
    ImageEffectCache<BufferedImage> cache = new ImageEffectCache<>(1);
    BufferedImage source = image(0xFFFF0000);
    AtomicInteger calls = new AtomicInteger();

    BufferedImage first = cache.getOrCompute(source, "first", image -> copyPixel(image, calls));
    cache.getOrCompute(source, "second", image -> copyPixel(image, calls));
    BufferedImage recomputed = cache.getOrCompute(source, "first", image -> copyPixel(image, calls));

    assertNotSame(first, recomputed);
    assertEquals(3, calls.get());
  }

  private static BufferedImage image(final int argb) {
    BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    image.setRGB(0, 0, argb);
    return image;
  }

  private static BufferedImage copyPixel(
    final BufferedImage source, final AtomicInteger calls) {
    calls.incrementAndGet();
    return image(source.getRGB(0, 0));
  }
}
