package core.game.render.scene;

import core.camera.CameraState;
import core.camera.CameraViewportState;
import core.utils.Point;
import core.utils.Rectangle;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Represents a color-grading effect applied to a scene.
 * This effect allows manipulation of hue, saturation, and brightness values of the scene's visuals.
 *
 * <p>Additionally, it supports applying the effect to specific regions of the scene with optional transition areas.
 *
 * <p>This class implements the {@link SceneEffectRegistry.ToggleableSceneEffect} interface, enabling
 * toggling of the effect on or off.
 */
public final class SceneColorGradeEffect
  implements SceneEffectRegistry.ToggleableSceneEffect {

  private float hue = -1.0f;
  private float saturationMultiplier = 1.0f;
  private float valueMultiplier = 1.0f;
  private float transitionSize = 2.0f;

  private Rectangle region = null;
  private boolean enabled = true;

  /** Creates a neutral scene color-grade effect that leaves the scene unchanged. */
  public SceneColorGradeEffect() {}

  /**
   * Creates a scene color-grade effect with the given parameters.
   *
   * @param hue target hue in {@code [0, 1]}; values {@code < 0} keep the original hue
   * @param saturationMultiplier multiplier for saturation
   * @param valueMultiplier multiplier for value/brightness
   */
  public SceneColorGradeEffect(
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
  public SceneColorGradeEffect hue(float hue) {
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
   */
  public void saturationMultiplier(float saturationMultiplier) {
    this.saturationMultiplier = Math.max(0f, saturationMultiplier);
  }

  /** @return value/brightness multiplier */
  public float valueMultiplier() {
    return valueMultiplier;
  }

  /**
   * Sets the value/brightness multiplier.
   *
   * @param valueMultiplier multiplier for value/brightness; negative values are clamped to 0
   */
  public void valueMultiplier(float valueMultiplier) {
    this.valueMultiplier = Math.max(0f, valueMultiplier);
  }

  /**
   * Returns the configured world-space region.
   *
   * @return region, or {@code null} if the effect applies globally
   */
  public Rectangle region() {
    return region;
  }

  /**
   * Sets the optional world-space region for the effect.
   *
   * @param region region in world coordinates; {@code null} means full-scene effect
   * @return this effect for chaining
   */
  public SceneColorGradeEffect region(Rectangle region) {
    this.region = region;
    return this;
  }

  /**
   * Returns the transition size around the configured region.
   *
   * @return transition size in world units
   */
  public float transitionSize() {
    return transitionSize;
  }

  /**
   * Sets the transition size around the configured region.
   *
   * @param transitionSize transition size in world units; negative values are clamped to 0
   * @return this effect for chaining
   */
  public SceneColorGradeEffect transitionSize(float transitionSize) {
    this.transitionSize = Math.max(0f, transitionSize);
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

    Point focus = CameraState.focusPosition();

    for (int y = 0; y < input.getHeight(); y++) {
      for (int x = 0; x < input.getWidth(); x++) {
        int argb = input.getRGB(x, y);
        int alpha = (argb >>> 24) & 0xFF;

        if (alpha == 0) {
          output.setRGB(x, y, 0);
          continue;
        }

        float influence = effectInfluenceAt(x, y, focus);
        if (influence <= 0f) {
          output.setRGB(x, y, argb);
          continue;
        }

        int gradedArgb = gradeArgb(argb);

        if (influence >= 1f) {
          output.setRGB(x, y, gradedArgb);
          continue;
        }

        output.setRGB(x, y, blendArgb(argb, gradedArgb, influence));
      }
    }

    return output;
  }

  private float effectInfluenceAt(int screenX, int screenY, Point focus) {
    if (region == null) {
      return 1f;
    }

    Point world =
      CameraViewportState.screenToWorld(
        new Point((float) screenX, (float) screenY), focus);

    if (region.contains(world)) {
      return 1f;
    }

    if (transitionSize <= 0f) {
      return 0f;
    }

    Rectangle expanded = region.expand(transitionSize);
    if (!expanded.contains(world)) {
      return 0f;
    }

    float dx = axisOutsideDistance(world.x(), region.x(), region.x() + region.width());
    float dy = axisOutsideDistance(world.y(), region.y(), region.y() + region.height());
    float outsideDistance = (float) Math.hypot(dx, dy);

    return clamp01(1f - outsideDistance / transitionSize);
  }

  private int gradeArgb(int argb) {
    int alpha = (argb >>> 24) & 0xFF;
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
    return (alpha << 24) | gradedRgb;
  }

  private static int blendArgb(int originalArgb, int gradedArgb, float influence) {
    float w = clamp01(influence);

    int oa = (originalArgb >>> 24) & 0xFF;
    int or = (originalArgb >>> 16) & 0xFF;
    int og = (originalArgb >>> 8) & 0xFF;
    int ob = originalArgb & 0xFF;

    int ga = (gradedArgb >>> 24) & 0xFF;
    int gr = (gradedArgb >>> 16) & 0xFF;
    int gg = (gradedArgb >>> 8) & 0xFF;
    int gb = gradedArgb & 0xFF;

    int a = mixChannel(oa, ga, w);
    int r = mixChannel(or, gr, w);
    int g = mixChannel(og, gg, w);
    int b = mixChannel(ob, gb, w);

    return (a << 24) | (r << 16) | (g << 8) | b;
  }

  private static int mixChannel(int original, int graded, float influence) {
    return Math.clamp(Math.round(original * (1f - influence) + graded * influence), 0, 255);
  }

  private static float axisOutsideDistance(float value, float min, float max) {
    if (value < min) {
      return min - value;
    }
    if (value > max) {
      return value - max;
    }
    return 0f;
  }

  private static float normalizeHue(float hue) {
    float normalized = hue % 1f;
    return normalized < 0f ? normalized + 1f : normalized;
  }

  private static float clamp01(float value) {
    return Math.clamp(value, 0f, 1f);
  }
}
