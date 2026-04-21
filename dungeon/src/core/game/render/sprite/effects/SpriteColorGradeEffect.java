package core.game.render.sprite.effects;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a sprite effect that applies a color grading transformation to the input image.
 * The effect modifies the hue, saturation, and brightness of a sprite based on configurable
 * parameters. The changes are applied only if the effect is enabled.
 *
 * <p>This effect supports chaining methods for configuring their parameters and uses a caching
 * mechanism to avoid redundant calculations.
 *
 * <p>It is backed by the {@link SpriteEffect} interface, making it compatible with the LITIENGINE
 * Graphics2D render path.
 */
public final class SpriteColorGradeEffect implements ToggleableSpriteEffect {

  private static final Map<CacheKey, BufferedImage> CACHE = new ConcurrentHashMap<>();

  private float hue = -1.0f;
  private float saturationMultiplier = 1.0f;
  private float valueMultiplier = 1.0f;
  private boolean enabled = true;

  /** Creates a neutral color-grade effect that leaves the sprite unchanged. */
  public SpriteColorGradeEffect() {}

  /**
   * Creates a color-grade effect with the given parameters.
   *
   * @param hue target hue in {@code [0, 1]}; values {@code < 0} keep the original hue
   * @param saturationMultiplier multiplier for saturation
   * @param valueMultiplier multiplier for value/brightness
   */
  public SpriteColorGradeEffect(
    float hue, float saturationMultiplier, float valueMultiplier) {
    this.hue(hue);
    this.saturationMultiplier(saturationMultiplier);
    this.valueMultiplier(valueMultiplier);
  }

  /**
   * Returns the target hue.
   *
   * @return target hue, or a negative value if the original hue should be preserved
   */
  public float hue() {
    return hue;
  }

  /**
   * Sets the target hue.
   *
   * @param hue target hue in {@code [0, 1]}; values {@code < 0} keep the original hue
   * @return this effect for chaining
   */
  public SpriteColorGradeEffect hue(float hue) {
    this.hue = hue < 0f ? -1.0f : normalizeHue(hue);
    return this;
  }

  /**
   * Returns the saturation multiplier.
   *
   * @return saturation multiplier
   */
  public float saturationMultiplier() {
    return saturationMultiplier;
  }

  /**
   * Sets the saturation multiplier.
   *
   * @param saturationMultiplier multiplier for saturation; negative values are clamped to 0
   * @return this effect for chaining
   */
  public SpriteColorGradeEffect saturationMultiplier(float saturationMultiplier) {
    this.saturationMultiplier = Math.max(0f, saturationMultiplier);
    return this;
  }

  /**
   * Returns the value/brightness multiplier.
   *
   * @return value multiplier
   */
  public float valueMultiplier() {
    return valueMultiplier;
  }

  /**
   * Sets the value/brightness multiplier.
   *
   * @param valueMultiplier multiplier for value/brightness; negative values are clamped to 0
   * @return this effect for chaining
   */
  public SpriteColorGradeEffect valueMultiplier(float valueMultiplier) {
    this.valueMultiplier = Math.max(0f, valueMultiplier);
    return this;
  }

  /**
   * Enables or disables this effect.
   *
   * @param enabled whether this effect should be active
   * @return this effect for chaining
   */
  public SpriteColorGradeEffect enabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  @Override
  public boolean enabled() {
    return enabled;
  }

  @Override
  public BufferedImage apply(BufferedImage input, long nowMs) {
    if (input == null || !enabled) {
      return input;
    }

    CacheKey key =
      new CacheKey(
        System.identityHashCode(input),
        input.getWidth(),
        input.getHeight(),
        Float.floatToIntBits(hue),
        Float.floatToIntBits(saturationMultiplier),
        Float.floatToIntBits(valueMultiplier));

    return CACHE.computeIfAbsent(key, ignored -> grade(input));
  }

  private BufferedImage grade(BufferedImage source) {
    BufferedImage output =
      new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);

    for (int y = 0; y < source.getHeight(); y++) {
      for (int x = 0; x < source.getWidth(); x++) {
        int argb = source.getRGB(x, y);
        int alpha = (argb >>> 24) & 0xFF;

        if (alpha == 0) {
          output.setRGB(x, y, 0);
          continue;
        }

        int r = (argb >>> 16) & 0xFF;
        int g = (argb >>> 8) & 0xFF;
        int b = argb & 0xFF;

        float[] hsb = Color.RGBtoHSB(r, g, b, null);

        if (hue >= 0f) {
          hsb[0] = hue;
        }

        hsb[1] = clamp01(hsb[1] * saturationMultiplier);
        hsb[2] = clamp01(hsb[2] * valueMultiplier);

        int gradedRgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]) & 0x00FFFFFF;
        output.setRGB(x, y, (alpha << 24) | gradedRgb);
      }
    }

    return output;
  }

  private static float normalizeHue(float hue) {
    float normalized = hue % 1f;
    return normalized < 0f ? normalized + 1f : normalized;
  }

  private static float clamp01(float value) {
    return Math.clamp(value, 0f, 1f);
  }

  private record CacheKey(
    int identityHash,
    int width,
    int height,
    int hueBits,
    int saturationMultiplierBits,
    int valueMultiplierBits) {}
}
