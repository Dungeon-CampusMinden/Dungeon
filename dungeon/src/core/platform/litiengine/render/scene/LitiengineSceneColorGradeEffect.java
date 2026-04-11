package core.platform.litiengine.render.scene;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Applies HSV-style color grading to the fully rendered LITIENGINE scene image.
 *
 * <p>This is the scene-pass counterpart to the existing sprite-local color grade effect.
 *
 * <p>It restores the globally applicable part of the former shader semantics:
 *
 * <ul>
 *   <li>optional hue override ({@code hue < 0} keeps the original hue)
 *   <li>saturation multiplier
 *   <li>value/brightness multiplier
 * </ul>
 *
 * <p>The old libGDX ColorGradeShader also supported world-space region and transition semantics.
 * Those are intentionally not part of this first scene-pass commit and should be modeled
 * separately once the global pass pipeline is in place.
 */
public final class LitiengineSceneColorGradeEffect
  implements LitiengineSceneEffects.ToggleableSceneEffect {

  private float hue = -1.0f;
  private float saturationMultiplier = 1.0f;
  private float valueMultiplier = 1.0f;
  private boolean enabled = true;

  /** Creates a neutral scene color-grade effect that leaves the scene unchanged. */
  public LitiengineSceneColorGradeEffect() {}

  /**
   * Creates a scene color-grade effect with the given parameters.
   *
   * @param hue target hue in {@code [0, 1]}; values {@code < 0} keep the original hue
   * @param saturationMultiplier multiplier for saturation
   * @param valueMultiplier multiplier for value/brightness
   */
  public LitiengineSceneColorGradeEffect(
    float hue, float saturationMultiplier, float valueMultiplier) {
    hue(hue);
    saturationMultiplier(saturationMultiplier);
    valueMultiplier(valueMultiplier);
  }

  /** @return target hue, or a negative value if the original hue should be preserved */
  public float hue() {
    return hue;
  }

  /**
   * Sets the target hue.
   *
   * @param hue target hue in {@code [0, 1]}; values {@code < 0} keep the original hue
   * @return this effect for chaining
   */
  public LitiengineSceneColorGradeEffect hue(float hue) {
    this.hue = hue < 0f ? -1.0f : normalizeHue(hue);
    return this;
  }

  /** @return saturation multiplier */
  public float saturationMultiplier() {
    return saturationMultiplier;
  }

  /**
   * Sets the saturation multiplier.
   *
   * @param saturationMultiplier multiplier for saturation; negative values are clamped to 0
   * @return this effect for chaining
   */
  public LitiengineSceneColorGradeEffect saturationMultiplier(float saturationMultiplier) {
    this.saturationMultiplier = Math.max(0f, saturationMultiplier);
    return this;
  }

  /** @return value/brightness multiplier */
  public float valueMultiplier() {
    return valueMultiplier;
  }

  /**
   * Sets the value/brightness multiplier.
   *
   * @param valueMultiplier multiplier for value/brightness; negative values are clamped to 0
   * @return this effect for chaining
   */
  public LitiengineSceneColorGradeEffect valueMultiplier(float valueMultiplier) {
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
