package core.game.render.sprite.effects;

import core.game.render.effects.ToggleableEffect;
import core.game.render.image.ImageEffectCache;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * A sprite effect that renders an animated shine overlay on sprites.
 *
 * <p>This effect creates a rotating light shine effect that moves across the sprite surface.
 * It supports both small and large sprites with different rendering strategies:
 * <ul>
 *   <li>Small sprites use a single diagonal sweep</li>
 *   <li>Large sprites use repeated rotating slices</li>
 * </ul>
 *
 * <p>The effect includes configurable parameters such as padding, slice count, gap size, rotation speed,
 * and shine color. Results are cached for improved performance.
 */
public final class ShineSpriteEffect
  implements SpriteEffect, ToggleableEffect<ShineSpriteEffect> {

  private static final double TWO_PI = Math.PI * 2.0;
  private static final int SMALL_SPRITE_MAX_DIM = 48;

  private static final ImageEffectCache<float[]> ALPHA_MASK_CACHE = new ImageEffectCache<>(16);

  private int padding = 20;
  private int sliceCount = 4;
  private float gapSize = 0.2f;
  private float rotationSpeed = 0.2f;
  private Color shineColor = new Color(255, 255, 128, 255);
  private boolean enabled = true;
  private long animationStartMs = -1L;

  /**
   * Creates a shine effect with default parameters.
   */
  public ShineSpriteEffect() {}

  /**
   * Gets the padding around the sprite for the shine effect.
   *
   * @return the padding in pixels
   */
  public int padding() {
    return padding;
  }

  /**
   * Sets the padding around the sprite for the shine effect.
   *
   * @param padding the padding in pixels (negative values are clamped to 0)
   * @return this effect for method chaining
   */
  public ShineSpriteEffect padding(int padding) {
    this.padding = Math.max(0, padding);
    return this;
  }

  /**
   * Sets the number of shine slices for large sprites.
   *
   * @param sliceCount the number of slices (minimum 1)
   * @return this effect for method chaining
   */
  public ShineSpriteEffect sliceCount(int sliceCount) {
    this.sliceCount = Math.max(1, sliceCount);
    return this;
  }

  /**
   * Sets the gap size between shine slices.
   *
   * @param gapSize the gap size in the range [0, 1]
   * @return this effect for method chaining
   */
  public ShineSpriteEffect gapSize(float gapSize) {
    this.gapSize = clamp01(gapSize);
    return this;
  }

  /**
   * Sets the rotation speed of the shine effect.
   *
   * @param rotationSpeed the rotation speed in rotations per second
   * @return this effect for method chaining
   */
  public ShineSpriteEffect rotationSpeed(float rotationSpeed) {
    this.rotationSpeed = rotationSpeed;
    return this;
  }

  /**
   * Sets the color of the shine overlay.
   *
   * @param shineColor the shine color (must not be null)
   * @return this effect for method chaining
   * @throws IllegalArgumentException if shineColor is null
   */
  public ShineSpriteEffect shineColor(Color shineColor) {
    if (shineColor == null) {
      throw new IllegalArgumentException("shineColor must not be null");
    }
    this.shineColor = shineColor;
    return this;
  }

  /**
   * Sets whether this effect is enabled.
   *
   * @param enabled true to enable the effect, false to disable it
   * @return this effect for method chaining
   */
  public ShineSpriteEffect enabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  @Override
  public boolean enabled() {
    return enabled;
  }

  /**
   * Shine is rendered as a separate overlay image at draw time.
   */
  @Override
  public BufferedImage apply(BufferedImage input, long nowMs) {
    return input;
  }

  /**
   * Creates the animated shine overlay for the given sprite.
   *
   * <p>The returned image can be larger than the input sprite because the old GDX shader also
   * supported explicit padding around the rendered effect.
   *
   * @param source already prepared sprite image (after tint / other pixel effects)
   * @param nowMs current timestamp
   * @return transparent overlay image containing only the shine bands
   */
  public BufferedImage createOverlay(BufferedImage source, long nowMs) {
    if (source == null || !enabled) {
      return null;
    }

    if (animationStartMs < 0L) {
      animationStartMs = nowMs;
    }

    double elapsedSeconds = Math.max(0.0, (nowMs - animationStartMs) / 1000.0);
    return renderOverlay(source, elapsedSeconds);
  }

  private BufferedImage renderOverlay(BufferedImage source, double nowSeconds) {
    int paddedWidth = source.getWidth() + 2 * padding;
    int paddedHeight = source.getHeight() + 2 * padding;

    float[] alphaMask = expandedAlphaMask(source, padding);

    if (isSmallSprite(source)) {
      return renderSingleSweepOverlay(
        paddedWidth, paddedHeight, alphaMask, nowSeconds);
    }

    return renderRepeatedSlicesOverlay(
      paddedWidth, paddedHeight, alphaMask, nowSeconds);
  }

  private boolean isSmallSprite(BufferedImage source) {
    return Math.max(source.getWidth(), source.getHeight()) <= SMALL_SPRITE_MAX_DIM;
  }

  private BufferedImage renderSingleSweepOverlay(
    int paddedWidth, int paddedHeight, float[] alphaMask, double nowSeconds) {
    BufferedImage output =
      new BufferedImage(paddedWidth, paddedHeight, BufferedImage.TYPE_INT_ARGB);

    float cx = (paddedWidth - 1) / 2.0f;
    float cy = (paddedHeight - 1) / 2.0f;

    // For small sprites we deliberately use a fixed diagonal sweep direction.
    // This is visually much easier to perceive than a subtle rotating projection model.
    float dirX = 1.0f;
    float dirY = -0.65f;

    float dirLength = (float) Math.hypot(dirX, dirY);
    dirX /= dirLength;
    dirY /= dirLength;

    float maxProjection =
      Math.max(1.0f, Math.abs(cx * dirX) + Math.abs(cy * dirY));

    // Very wide and explicit band for tiny sprites.
    float bandWidth = clamp(0.42f - gapSize * 0.18f);
    float bandHalfWidth = bandWidth * 0.5f;

    float phase = (float) normalizePhase(nowSeconds * rotationSpeed);
    float sweepCenter = -bandHalfWidth + phase * (1.0f + bandWidth);

    float shineAlpha = shineColor.getAlpha() / 255.0f;

    for (int y = 0; y < paddedHeight; y++) {
      for (int x = 0; x < paddedWidth; x++) {
        float maskAlpha = alphaMask[y * paddedWidth + x];
        if (maskAlpha <= 0.0f) {
          output.setRGB(x, y, 0);
          continue;
        }

        float relX = x - cx;
        float relY = y - cy;

        float projected = (relX * dirX + relY * dirY) / maxProjection;
        float u = projected * 0.5f + 0.5f;

        float distance = Math.abs(u - sweepCenter);
        if (distance > bandHalfWidth) {
          output.setRGB(x, y, 0);
          continue;
        }

        float normalized = 1.0f - (distance / Math.max(0.0001f, bandHalfWidth));

        // Strong explicit band with soft center falloff.
        float bandIntensity = smoothPulse(normalized);

        float radialDistance =
          (float) Math.hypot(relX, relY) / Math.max(1.0f, Math.max(cx, cy));
        float radialFade = 1.0f - 0.15f * clamp01(radialDistance);

        float overlayStrength = clamp01(maskAlpha * bandIntensity * radialFade);
        int overlayAlpha = toChannel(shineAlpha * overlayStrength);

        if (overlayAlpha <= 0) {
          output.setRGB(x, y, 0);
          continue;
        }

        int overlayArgb =
          (overlayAlpha << 24)
            | (shineColor.getRed() << 16)
            | (shineColor.getGreen() << 8)
            | shineColor.getBlue();

        output.setRGB(x, y, overlayArgb);
      }
    }

    return output;
  }

  private BufferedImage renderRepeatedSlicesOverlay(
    int paddedWidth, int paddedHeight, float[] alphaMask, double nowSeconds) {
    BufferedImage output =
      new BufferedImage(paddedWidth, paddedHeight, BufferedImage.TYPE_INT_ARGB);

    float cx = (paddedWidth - 1) / 2.0f;
    float cy = (paddedHeight - 1) / 2.0f;

    double baseAngle = normalizePhase(nowSeconds * rotationSpeed) * TWO_PI;
    float cos = (float) Math.cos(baseAngle);
    float sin = (float) Math.sin(baseAngle);

    float maxProjection = Math.max(1.0f, Math.abs(cx * cos) + Math.abs(cy * sin));
    float visibleFraction = Math.max(0.06f, 1.0f - gapSize);

    float sweepPhase = (float) normalizePhase(nowSeconds * rotationSpeed * 1.8);
    float pulse =
      0.80f
        + 0.20f
        * (0.5f
        + 0.5f
        * (float) Math.sin(nowSeconds * rotationSpeed * TWO_PI));

    float shineAlpha = shineColor.getAlpha() / 255.0f;

    for (int y = 0; y < paddedHeight; y++) {
      for (int x = 0; x < paddedWidth; x++) {
        float maskAlpha = alphaMask[y * paddedWidth + x];
        if (maskAlpha <= 0.0f) {
          output.setRGB(x, y, 0);
          continue;
        }

        float relX = x - cx;
        float relY = y - cy;

        float projected = (relX * cos + relY * sin) / maxProjection;
        float u = projected * 0.5f + 0.5f;

        float localSlice = fract(u * sliceCount - sweepPhase);
        float bandIntensity = shineBandIntensity(localSlice, visibleFraction);
        if (bandIntensity <= 0.0f) {
          output.setRGB(x, y, 0);
          continue;
        }

        float radialDistance =
          (float) Math.hypot(relX, relY) / Math.max(1.0f, Math.max(cx, cy));
        float radialFade = 1.0f - 0.30f * clamp01(radialDistance);

        float overlayStrength = clamp01(maskAlpha * bandIntensity * radialFade * pulse);
        int overlayAlpha = toChannel(shineAlpha * overlayStrength);

        if (overlayAlpha <= 0) {
          output.setRGB(x, y, 0);
          continue;
        }

        int overlayArgb =
          (overlayAlpha << 24)
            | (shineColor.getRed() << 16)
            | (shineColor.getGreen() << 8)
            | shineColor.getBlue();

        output.setRGB(x, y, overlayArgb);
      }
    }

    return output;
  }

  private static float[] expandedAlphaMask(BufferedImage source, int padding) {
    MaskCacheKey key = new MaskCacheKey(padding);

    return ALPHA_MASK_CACHE.getOrCompute(
      source, key, image -> buildExpandedAlphaMask(image, padding));
  }

  private static float[] buildExpandedAlphaMask(BufferedImage source, int padding) {
    int paddedWidth = source.getWidth() + 2 * padding;
    int paddedHeight = source.getHeight() + 2 * padding;
    float[] mask = new float[paddedWidth * paddedHeight];

    int radius = Math.max(0, padding);
    int radiusSq = radius * radius;

    for (int y = 0; y < paddedHeight; y++) {
      for (int x = 0; x < paddedWidth; x++) {
        int sourceX = x - padding;
        int sourceY = y - padding;

        float directAlpha = alphaAt(source, sourceX, sourceY);
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
            int distSq = dx * dx + dy * dy;
            if (distSq > radiusSq) {
              continue;
            }

            float candidateAlpha = alphaAt(source, sourceX + dx, sourceY + dy);
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

        float distance = (float) Math.sqrt(bestDistSq);
        float feather = 1.0f - distance / Math.max(1.0f, radius);
        mask[y * paddedWidth + x] = clamp01(bestAlpha * feather * feather);
      }
    }

    return mask;
  }

  private static float alphaAt(BufferedImage image, int x, int y) {
    if (x < 0 || y < 0 || x >= image.getWidth() || y >= image.getHeight()) {
      return 0.0f;
    }
    return ((image.getRGB(x, y) >>> 24) & 0xFF) / 255.0f;
  }

  private static float shineBandIntensity(float localSlice, float visibleFraction) {
    if (localSlice >= visibleFraction) {
      return 0.0f;
    }

    float normalized = localSlice / visibleFraction;
    return smoothPulse(normalized);
  }

  private static float smoothPulse(float value) {
    float centerDistance = Math.abs(value - 0.5f) * 2.0f;
    float base = 1.0f - centerDistance;
    return clamp01(base * base * (3.0f - 2.0f * base));
  }

  private static int toChannel(float value) {
    return Math.clamp(Math.round(clamp01(value) * 255.0f), 0, 255);
  }

  private static double normalizePhase(double value) {
    double normalized = value % 1.0;
    return normalized < 0.0 ? normalized + 1.0 : normalized;
  }

  private static float fract(float value) {
    return value - (float) Math.floor(value);
  }

  private static float clamp01(float value) {
    return Math.clamp(value, 0.0f, 1.0f);
  }

  private static float clamp(float value) {
    return Math.clamp(value, (float) 0.22, (float) 0.42);
  }

  private record MaskCacheKey(int padding) {}
}
