package core.game.render.sprite.effects;

import core.game.render.effects.ColorGradeUtils;
import core.game.render.effects.ToggleableEffect;
import core.game.render.image.ImageEffectCache;
import java.awt.image.BufferedImage;

/**
 * Represents a sprite effect that applies a color grading transformation to the input image. The
 * effect modifies the hue, saturation, and brightness of a sprite based on configurable parameters.
 * The changes are applied only if the effect is enabled.
 *
 * <p>This effect supports chaining methods for configuring their parameters and uses a caching
 * mechanism to avoid redundant calculations.
 *
 * <p>It is backed by the {@link SpriteEffect} interface, making it compatible with the LITIENGINE
 * Graphics2D render path.
 */
public final class SpriteColorGradeEffect
    implements SpriteEffect, ToggleableEffect<SpriteColorGradeEffect> {

  private static final ImageEffectCache<BufferedImage> CACHE = new ImageEffectCache<>(16);

  private float hue = -1.0f;
  private float saturationMultiplier = 1.0f;
  private float valueMultiplier = 1.0f;
  private boolean enabled = true;

  /**
   * Creates a color-grade effect with the given parameters.
   *
   * @param hue target hue in {@code [0, 1]}; values {@code < 0} keep the original hue
   * @param saturationMultiplier multiplier for saturation
   * @param valueMultiplier multiplier for value/brightness
   */
  public SpriteColorGradeEffect(float hue, float saturationMultiplier, float valueMultiplier) {
    this.hue(hue);
    this.saturationMultiplier(saturationMultiplier);
    this.valueMultiplier(valueMultiplier);
  }

  /**
   * Sets the target hue.
   *
   * @param hue target hue in {@code [0, 1]}; values {@code < 0} keep the original hue
   * @return this effect for chaining
   */
  public SpriteColorGradeEffect hue(float hue) {
    this.hue = ColorGradeUtils.normalizeTargetHue(hue);
    return this;
  }

  /**
   * Sets the saturation multiplier.
   *
   * @param saturationMultiplier multiplier for saturation; negative values are clamped to 0
   */
  public void saturationMultiplier(float saturationMultiplier) {
    this.saturationMultiplier = ColorGradeUtils.clampMultiplier(saturationMultiplier);
  }

  /**
   * Sets the value/brightness multiplier.
   *
   * @param valueMultiplier multiplier for value/brightness; negative values are clamped to 0
   */
  public void valueMultiplier(float valueMultiplier) {
    this.valueMultiplier = ColorGradeUtils.clampMultiplier(valueMultiplier);
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
  public BufferedImage apply(BufferedImage input) {
    if (input == null || !enabled) {
      return input;
    }

    CacheKey key =
        new CacheKey(
            Float.floatToIntBits(hue),
            Float.floatToIntBits(saturationMultiplier),
            Float.floatToIntBits(valueMultiplier));

    return CACHE.getOrCompute(input, key, this::grade);
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

        output.setRGB(
            x, y, ColorGradeUtils.gradeArgb(argb, hue, saturationMultiplier, valueMultiplier));
      }
    }

    return output;
  }

  private record CacheKey(int hueBits, int saturationMultiplierBits, int valueMultiplierBits) {}
}
