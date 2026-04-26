package core.game.render.effects;

import java.awt.Color;

/** Utility methods for HSV-style color grading transformations. */
public final class ColorGradeUtils {

  private static final float KEEP_ORIGINAL_HUE = -1.0f;

  private ColorGradeUtils() {}

  /**
   * Normalizes a target hue value.
   *
   * @param hue target hue; values less than 0 keep the original hue
   * @return normalized target hue in {@code [0, 1]}, or {@code -1.0f} to keep the original hue
   */
  public static float normalizeTargetHue(float hue) {
    return hue < 0f ? KEEP_ORIGINAL_HUE : normalizeHue(hue);
  }

  /**
   * Clamps a saturation or value multiplier.
   *
   * @param multiplier multiplier value
   * @return multiplier clamped to a minimum of 0
   */
  public static float clampMultiplier(float multiplier) {
    return Math.max(0f, multiplier);
  }

  /**
   * Applies HSV-style color grading to an ARGB pixel.
   *
   * @param argb source pixel in ARGB format
   * @param hue target hue in {@code [0, 1]}; values {@code < 0} keep the original hue
   * @param saturationMultiplier saturation multiplier; negative values are clamped to 0
   * @param valueMultiplier value/brightness multiplier; negative values are clamped to 0
   * @return graded pixel in ARGB format
   */
  public static int gradeArgb(
      int argb, float hue, float saturationMultiplier, float valueMultiplier) {
    int alpha = (argb >>> 24) & 0xFF;
    if (alpha == 0) {
      return 0;
    }

    int r = (argb >>> 16) & 0xFF;
    int g = (argb >>> 8) & 0xFF;
    int b = argb & 0xFF;

    float[] hsb = Color.RGBtoHSB(r, g, b, null);
    float targetHue = normalizeTargetHue(hue);

    if (targetHue >= 0f) {
      hsb[0] = targetHue;
    }

    hsb[1] = clamp01(hsb[1] * clampMultiplier(saturationMultiplier));
    hsb[2] = clamp01(hsb[2] * clampMultiplier(valueMultiplier));

    int gradedRgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]) & 0x00FFFFFF;
    return (alpha << 24) | gradedRgb;
  }

  private static float normalizeHue(float hue) {
    float normalized = hue % 1f;
    return normalized < 0f ? normalized + 1f : normalized;
  }

  private static float clamp01(float value) {
    return Math.clamp(value, 0f, 1f);
  }
}
