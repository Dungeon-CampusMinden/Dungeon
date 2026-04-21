package core.game.render.sprite.effects;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A sprite effect that remaps hue values in an image.
 *
 * <p>This effect allows you to change the color of pixels within a specified hue range to a target hue.
 * It uses HSB (Hue, Saturation, Brightness) color space to identify and transform colors based on
 * hue values. The effect includes a tolerance parameter to allow matching of colors close to the
 * starting hue.
 *
 * <p>Results are cached to improve performance for repeated applications of the same effect to the
 * same image.
 */
public final class HueRemapSpriteEffect implements ToggleableSpriteEffect {

  private static final Map<CacheKey, BufferedImage> CACHE = new ConcurrentHashMap<>();

  private float startingHue;
  private float targetHue;
  private float tolerance;
  private boolean enabled = true;

  /**
   * Creates a new HueRemapSpriteEffect with the specified hue remapping parameters.
   *
   * @param startingHue the hue value to match (0.0 to 1.0)
   * @param targetHue the hue value to remap to (0.0 to 1.0)
   * @param tolerance the acceptable distance from the starting hue (0.0 to 1.0)
   */
  public HueRemapSpriteEffect(float startingHue, float targetHue, float tolerance) {
    this.startingHue = normalizeHue(startingHue);
    this.targetHue = normalizeHue(targetHue);
    this.tolerance = clamp01(tolerance);
  }

  /**
   * Creates a new HueRemapSpriteEffect with a default tolerance of 0.05.
   *
   * @param startingHue the hue value to match (0.0 to 1.0)
   * @param targetHue the hue value to remap to (0.0 to 1.0)
   */
  public HueRemapSpriteEffect(float startingHue, float targetHue) {
    this(startingHue, targetHue, 0.05f);
  }

  /**
   * Gets the starting hue value to match.
   *
   * @return the starting hue (0.0 to 1.0)
   */
  public float startingHue() {
    return startingHue;
  }

  /**
   * Sets the starting hue value to match.
   *
   * @param startingHue the hue value to match (0.0 to 1.0)
   * @return this effect for method chaining
   */
  public HueRemapSpriteEffect startingHue(float startingHue) {
    this.startingHue = normalizeHue(startingHue);
    return this;
  }

  /**
   * Gets the target hue value to remap to.
   *
   * @return the target hue (0.0 to 1.0)
   */
  public float targetHue() {
    return targetHue;
  }

  /**
   * Sets the target hue value to remap to.
   *
   * @param targetHue the hue value to remap to (0.0 to 1.0)
   * @return this effect for method chaining
   */
  public HueRemapSpriteEffect targetHue(float targetHue) {
    this.targetHue = normalizeHue(targetHue);
    return this;
  }

  /**
   * Gets the tolerance value for hue matching.
   *
   * @return the tolerance (0.0 to 1.0)
   */
  public float tolerance() {
    return tolerance;
  }

  /**
   * Sets the tolerance value for hue matching.
   *
   * @param tolerance the acceptable distance from the starting hue (0.0 to 1.0)
   * @return this effect for method chaining
   */
  public HueRemapSpriteEffect tolerance(float tolerance) {
    this.tolerance = clamp01(tolerance);
    return this;
  }

  /**
   * Sets whether this effect is enabled.
   *
   * @param enabled true to enable the effect, false to disable it
   * @return this effect for method chaining
   */
  public HueRemapSpriteEffect enabled(boolean enabled) {
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
        Float.floatToIntBits(startingHue),
        Float.floatToIntBits(targetHue),
        Float.floatToIntBits(tolerance));

    return CACHE.computeIfAbsent(key, ignored -> remap(input));
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
    int identityHash,
    int width,
    int height,
    int startingHueBits,
    int targetHueBits,
    int toleranceBits) {}
}
