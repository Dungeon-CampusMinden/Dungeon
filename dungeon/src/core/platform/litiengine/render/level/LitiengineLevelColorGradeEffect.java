package core.platform.litiengine.render.level;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Applies HSV-style color grading to the rendered level layer only.
 *
 * <p>This is the level-pass counterpart to the existing scene and sprite-local color-grade effects.
 */
public final class LitiengineLevelColorGradeEffect
  implements LitiengineLevelEffects.ToggleableLevelEffect {

  private float hue = -1.0f;
  private float saturationMultiplier = 1.0f;
  private float valueMultiplier = 1.0f;
  private boolean enabled = true;

  public LitiengineLevelColorGradeEffect() {}

  public LitiengineLevelColorGradeEffect(
    float hue, float saturationMultiplier, float valueMultiplier) {
    hue(hue);
    saturationMultiplier(saturationMultiplier);
    valueMultiplier(valueMultiplier);
  }

  public float hue() {
    return hue;
  }

  public LitiengineLevelColorGradeEffect hue(float hue) {
    this.hue = hue < 0f ? -1.0f : normalizeHue(hue);
    return this;
  }

  public float saturationMultiplier() {
    return saturationMultiplier;
  }

  public LitiengineLevelColorGradeEffect saturationMultiplier(float saturationMultiplier) {
    this.saturationMultiplier = Math.max(0f, saturationMultiplier);
    return this;
  }

  public float valueMultiplier() {
    return valueMultiplier;
  }

  public LitiengineLevelColorGradeEffect valueMultiplier(float valueMultiplier) {
    this.valueMultiplier = Math.max(0f, valueMultiplier);
    return this;
  }

  @Override
  public boolean enabled() {
    return enabled;
  }

  @Override
  public void enabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  public BufferedImage apply(BufferedImage input, long nowMs) {
    if (input == null || !enabled) {
      return input;
    }

    BufferedImage output =
      new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);

    for (int y = 0; y < input.getHeight(); y++) {
      for (int x = 0; x < input.getWidth(); x++) {
        int argb = input.getRGB(x, y);
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
}
