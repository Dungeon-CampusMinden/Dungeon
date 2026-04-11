package core.platform.litiengine.render.effects;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remaps one hue range to another hue in the LITIENGINE Graphics2D render path.
 *
 * <p>Hue values are normalized to {@code [0, 1]}.
 */
public final class LitiengineHueRemapEffect implements LitiengineSpriteEffect {

  private static final Map<CacheKey, BufferedImage> CACHE = new ConcurrentHashMap<>();

  private float startingHue;
  private float targetHue;
  private float tolerance;
  private boolean enabled = true;

  public LitiengineHueRemapEffect(float startingHue, float targetHue, float tolerance) {
    this.startingHue = normalizeHue(startingHue);
    this.targetHue = normalizeHue(targetHue);
    this.tolerance = clamp01(tolerance);
  }

  public LitiengineHueRemapEffect(float startingHue, float targetHue) {
    this(startingHue, targetHue, 0.05f);
  }

  public float startingHue() {
    return startingHue;
  }

  public LitiengineHueRemapEffect startingHue(float startingHue) {
    this.startingHue = normalizeHue(startingHue);
    return this;
  }

  public float targetHue() {
    return targetHue;
  }

  public LitiengineHueRemapEffect targetHue(float targetHue) {
    this.targetHue = normalizeHue(targetHue);
    return this;
  }

  public float tolerance() {
    return tolerance;
  }

  public LitiengineHueRemapEffect tolerance(float tolerance) {
    this.tolerance = clamp01(tolerance);
    return this;
  }

  public LitiengineHueRemapEffect enabled(boolean enabled) {
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
