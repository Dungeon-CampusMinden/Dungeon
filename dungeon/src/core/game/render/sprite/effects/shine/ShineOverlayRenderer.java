package core.game.render.sprite.effects.shine;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * The ShineOverlayRenderer class provides rendering methods for generating animated shine overlay
 * effects on an input image.
 *
 * <p>The implementation supports two rendering strategies: single-sweep overlays for small sprites
 * and repeated slice overlays for larger images.
 *
 * <p>The rendered overlay is affected by various parameters such as padding, slice count, gap size,
 * rotation speed, and shine color, provided through a {@code ShineRenderConfig} configuration
 * object.
 *
 * <p>The rendering process adapts intensity, transparency, and radial fade effects based on the
 * mask alpha, geometry, and time-dependent animations. The resulting output is a padded {@code
 * BufferedImage} with added shine overlay effects.
 *
 * <p>This class operates as a final utility and cannot be instantiated.
 */
final class ShineOverlayRenderer {

  private static final double TWO_PI = Math.PI * 2.0;
  private static final int SMALL_SPRITE_MAX_DIM = 48;
  private static final float MIN_BAND_WIDTH = 0.22f;
  private static final float MAX_BAND_WIDTH = 0.42f;

  private ShineOverlayRenderer() {}

  static BufferedImage render(
      final BufferedImage source, final ShineRenderConfig config, final double nowSeconds) {
    final int paddedWidth = source.getWidth() + 2 * config.padding();
    final int paddedHeight = source.getHeight() + 2 * config.padding();
    final float[] alphaMask = ShineAlphaMaskCache.expandedAlphaMask(source, config.padding());

    if (isSmallSprite(source)) {
      return renderSingleSweepOverlay(paddedWidth, paddedHeight, alphaMask, config, nowSeconds);
    }

    return renderRepeatedSlicesOverlay(paddedWidth, paddedHeight, alphaMask, config, nowSeconds);
  }

  private static boolean isSmallSprite(final BufferedImage source) {
    return Math.max(source.getWidth(), source.getHeight()) <= SMALL_SPRITE_MAX_DIM;
  }

  private static BufferedImage renderSingleSweepOverlay(
      final int paddedWidth,
      final int paddedHeight,
      final float[] alphaMask,
      final ShineRenderConfig config,
      final double nowSeconds) {
    final BufferedImage output =
        new BufferedImage(paddedWidth, paddedHeight, BufferedImage.TYPE_INT_ARGB);

    final float cx = (paddedWidth - 1) / 2.0f;
    final float cy = (paddedHeight - 1) / 2.0f;

    float dirX = 1.0f;
    float dirY = -0.65f;

    final float dirLength = (float) Math.hypot(dirX, dirY);
    dirX /= dirLength;
    dirY /= dirLength;

    final float maxProjection = Math.max(1.0f, Math.abs(cx * dirX) + Math.abs(cy * dirY));
    final float bandWidth = clampBandWidth(0.42f - config.gapSize() * 0.18f);
    final float bandHalfWidth = bandWidth * 0.5f;
    final float phase = (float) normalizePhase(nowSeconds * config.rotationSpeed());
    final float sweepCenter = -bandHalfWidth + phase * (1.0f + bandWidth);
    final Color shineColor = config.shineColor();
    final float shineAlpha = shineColor.getAlpha() / 255.0f;

    for (int y = 0; y < paddedHeight; y++) {
      for (int x = 0; x < paddedWidth; x++) {
        final float maskAlpha = alphaMask[y * paddedWidth + x];
        if (maskAlpha <= 0.0f) {
          output.setRGB(x, y, 0);
          continue;
        }

        final float relX = x - cx;
        final float relY = y - cy;
        final float projected = (relX * dirX + relY * dirY) / maxProjection;
        final float u = projected * 0.5f + 0.5f;
        final float distance = Math.abs(u - sweepCenter);

        if (distance > bandHalfWidth) {
          output.setRGB(x, y, 0);
          continue;
        }

        final float normalized = 1.0f - (distance / Math.max(0.0001f, bandHalfWidth));
        final float bandIntensity = smoothPulse(normalized);
        final float radialDistance =
            (float) Math.hypot(relX, relY) / Math.max(1.0f, Math.max(cx, cy));
        final float radialFade = 1.0f - 0.15f * clamp01(radialDistance);
        final float overlayStrength = clamp01(maskAlpha * bandIntensity * radialFade);
        final int overlayAlpha = toChannel(shineAlpha * overlayStrength);

        output.setRGB(x, y, overlayArgb(shineColor, overlayAlpha));
      }
    }

    return output;
  }

  private static BufferedImage renderRepeatedSlicesOverlay(
      final int paddedWidth,
      final int paddedHeight,
      final float[] alphaMask,
      final ShineRenderConfig config,
      final double nowSeconds) {
    final BufferedImage output =
        new BufferedImage(paddedWidth, paddedHeight, BufferedImage.TYPE_INT_ARGB);

    final float cx = (paddedWidth - 1) / 2.0f;
    final float cy = (paddedHeight - 1) / 2.0f;
    final double baseAngle = normalizePhase(nowSeconds * config.rotationSpeed()) * TWO_PI;
    final float cos = (float) Math.cos(baseAngle);
    final float sin = (float) Math.sin(baseAngle);
    final float maxProjection = Math.max(1.0f, Math.abs(cx * cos) + Math.abs(cy * sin));
    final float visibleFraction = Math.max(0.06f, 1.0f - config.gapSize());
    final float sweepPhase = (float) normalizePhase(nowSeconds * config.rotationSpeed() * 1.8);
    final float pulse =
        0.80f
            + 0.20f
                * (0.5f + 0.5f * (float) Math.sin(nowSeconds * config.rotationSpeed() * TWO_PI));
    final Color shineColor = config.shineColor();
    final float shineAlpha = shineColor.getAlpha() / 255.0f;

    for (int y = 0; y < paddedHeight; y++) {
      for (int x = 0; x < paddedWidth; x++) {
        final float maskAlpha = alphaMask[y * paddedWidth + x];
        if (maskAlpha <= 0.0f) {
          output.setRGB(x, y, 0);
          continue;
        }

        final float relX = x - cx;
        final float relY = y - cy;
        final float projected = (relX * cos + relY * sin) / maxProjection;
        final float u = projected * 0.5f + 0.5f;
        final float localSlice = fract(u * config.sliceCount() - sweepPhase);
        final float bandIntensity = shineBandIntensity(localSlice, visibleFraction);

        if (bandIntensity <= 0.0f) {
          output.setRGB(x, y, 0);
          continue;
        }

        final float radialDistance =
            (float) Math.hypot(relX, relY) / Math.max(1.0f, Math.max(cx, cy));
        final float radialFade = 1.0f - 0.30f * clamp01(radialDistance);
        final float overlayStrength = clamp01(maskAlpha * bandIntensity * radialFade * pulse);
        final int overlayAlpha = toChannel(shineAlpha * overlayStrength);

        output.setRGB(x, y, overlayArgb(shineColor, overlayAlpha));
      }
    }

    return output;
  }

  private static int overlayArgb(final Color shineColor, final int overlayAlpha) {
    if (overlayAlpha <= 0) {
      return 0;
    }

    return (overlayAlpha << 24)
        | (shineColor.getRed() << 16)
        | (shineColor.getGreen() << 8)
        | shineColor.getBlue();
  }

  private static float shineBandIntensity(final float localSlice, final float visibleFraction) {
    if (localSlice >= visibleFraction) {
      return 0.0f;
    }

    final float normalized = localSlice / visibleFraction;
    return smoothPulse(normalized);
  }

  private static float smoothPulse(final float value) {
    final float centerDistance = Math.abs(value - 0.5f) * 2.0f;
    final float base = 1.0f - centerDistance;
    return clamp01(base * base * (3.0f - 2.0f * base));
  }

  private static int toChannel(final float value) {
    return Math.clamp(Math.round(clamp01(value) * 255.0f), 0, 255);
  }

  private static double normalizePhase(final double value) {
    final double normalized = value % 1.0;
    return normalized < 0.0 ? normalized + 1.0 : normalized;
  }

  private static float fract(final float value) {
    return value - (float) Math.floor(value);
  }

  private static float clamp01(final float value) {
    return Math.clamp(value, 0.0f, 1.0f);
  }

  private static float clampBandWidth(final float value) {
    return Math.clamp(value, MIN_BAND_WIDTH, MAX_BAND_WIDTH);
  }
}
