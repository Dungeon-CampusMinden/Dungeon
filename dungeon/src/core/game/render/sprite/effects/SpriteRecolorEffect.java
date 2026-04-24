package core.game.render.sprite.effects;

import core.game.render.image.ImageEffectCache;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * A sprite recolor effect that remaps hue values in an image.
 *
 * <p>This effect allows you to change the color of pixels within a specified hue range to a target hue.
 * It uses HSB (Hue, Saturation, Brightness) color space to identify and transform colors based on
 * hue values. The effect includes a tolerance parameter to allow matching of colors close to the
 * starting hue.
 *
 * <p>Results are cached to improve performance for repeated applications of the same effect to the
 * same image.
 */
public final class SpriteRecolorEffect implements ToggleableSpriteEffect {

  private static final ImageEffectCache<BufferedImage> CACHE = new ImageEffectCache<>(16);

  private final float startingHue;
  private final float targetHue;
  private final float tolerance;
  private boolean enabled = true;

  /**
   * Creates a new HueRemapSpriteEffect with the specified hue remapping parameters.
   *
   * @param startingHue the hue value to match (0.0 to 1.0)
   * @param targetHue the hue value to remap to (0.0 to 1.0)
   * @param tolerance the acceptable distance from the starting hue (0.0 to 1.0)
   */
  public SpriteRecolorEffect(float startingHue, float targetHue, float tolerance) {
    this.startingHue = normalizeHue(startingHue);
    this.targetHue = normalizeHue(targetHue);
    this.tolerance = clamp01(tolerance);
  }

  /**
   * Sets whether this effect is enabled.
   *
   * @param enabled true to enable the effect, false to disable it
   * @return this effect for method chaining
   */
  public SpriteRecolorEffect enabled(boolean enabled) {
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
        Float.floatToIntBits(startingHue),
        Float.floatToIntBits(targetHue),
        Float.floatToIntBits(tolerance));

    return CACHE.getOrCompute(input, key, this::remap);
  }

  private BufferedImage remap(BufferedImage source) {
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
        if (circularHueDistance(hsb[0], startingHue) <= tolerance) {
          hsb[0] = targetHue;
        }

        int remappedRgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]) & 0x00FFFFFF;
        output.setRGB(x, y, (alpha << 24) | remappedRgb);
      }
    }

    return output;
  }

  private static float circularHueDistance(float a, float b) {
    float diff = Math.abs(a - b);
    return Math.min(diff, 1f - diff);
  }

  private static float normalizeHue(float hue) {
    float normalized = hue % 1f;
    return normalized < 0f ? normalized + 1f : normalized;
  }

  private static float clamp01(float value) {
    return Math.clamp(value, 0f, 1f);
  }

  private record CacheKey(
    int startingHueBits,
    int targetHueBits,
    int toleranceBits) {}
}
