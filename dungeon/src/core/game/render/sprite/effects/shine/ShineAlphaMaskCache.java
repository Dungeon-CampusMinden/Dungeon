package core.game.render.sprite.effects.shine;

import core.game.render.image.ImageEffectCache;
import java.awt.image.BufferedImage;

/**
 * Utility class for managing and generating expanded alpha masks for images with optional padding.
 *
 * <p>This class uses an internal cache to optimize repeated computations for the same source image
 * and effect parameters, reducing redundant processing and memory usage.
 *
 * <ul>
 *   <li>An "expanded alpha mask" is a float array representing alpha channel values, calculated
 *       from the source image with added padding where necessary.
 *   <li>Padding extends the image boundaries, and the mask values in the padding areas are
 *       calculated by sampling nearby alpha channel values and applying a feathering effect.
 *   <li>The caching mechanism ensures efficient reuse of precomputed masks for identical input
 *       configurations.
 * </ul>
 *
 * Thread-safety: This class is thread-safe due to the internal synchronization in the caching
 * layer.
 */
final class ShineAlphaMaskCache {

  private static final ImageEffectCache<float[]> ALPHA_MASK_CACHE = new ImageEffectCache<>(16);

  private ShineAlphaMaskCache() {}

  static float[] expandedAlphaMask(final BufferedImage source, final int padding) {
    final MaskCacheKey key = new MaskCacheKey(padding);
    return ALPHA_MASK_CACHE.getOrCompute(
        source, key, image -> buildExpandedAlphaMask(image, padding));
  }

  private static float[] buildExpandedAlphaMask(final BufferedImage source, final int padding) {
    final int paddedWidth = source.getWidth() + 2 * padding;
    final int paddedHeight = source.getHeight() + 2 * padding;
    final float[] mask = new float[paddedWidth * paddedHeight];

    final int radius = Math.max(0, padding);
    final int radiusSq = radius * radius;

    for (int y = 0; y < paddedHeight; y++) {
      for (int x = 0; x < paddedWidth; x++) {
        final int sourceX = x - padding;
        final int sourceY = y - padding;

        final float directAlpha = alphaAt(source, sourceX, sourceY);
        if (directAlpha > 0.0f) {
          mask[y * paddedWidth + x] = directAlpha;
          continue;
        }

        if (radius == 0) {
          mask[y * paddedWidth + x] = 0.0f;
          continue;
        }

        int bestDistSq = Integer.MAX_VALUE;
        float bestAlpha = 0.0f;

        for (int dy = -radius; dy <= radius; dy++) {
          for (int dx = -radius; dx <= radius; dx++) {
            final int distSq = dx * dx + dy * dy;
            if (distSq > radiusSq) {
              continue;
            }

            final float candidateAlpha = alphaAt(source, sourceX + dx, sourceY + dy);
            if (candidateAlpha <= 0.0f) {
              continue;
            }

            if (distSq < bestDistSq) {
              bestDistSq = distSq;
              bestAlpha = candidateAlpha;
            }
          }
        }

        if (bestDistSq == Integer.MAX_VALUE) {
          mask[y * paddedWidth + x] = 0.0f;
          continue;
        }

        final float distance = (float) Math.sqrt(bestDistSq);
        final float feather = 1.0f - distance / Math.max(1.0f, radius);
        mask[y * paddedWidth + x] = clamp01(bestAlpha * feather * feather);
      }
    }

    return mask;
  }

  private static float alphaAt(final BufferedImage image, final int x, final int y) {
    if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
      return 0.0f;
    }
    return ((image.getRGB(x, y) >>> 24) & 0xFF) / 255.0f;
  }

  private static float clamp01(final float value) {
    return Math.clamp(value, 0.0f, 1.0f);
  }

  private record MaskCacheKey(int padding) {}
}
