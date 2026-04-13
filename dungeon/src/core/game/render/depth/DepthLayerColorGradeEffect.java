package core.game.render.depth;

import core.camera.LitiengineCameraState;
import core.camera.LitiengineCameraViews;
import core.utils.Point;
import core.utils.Rectangle;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Applies HSV-style color grading to one rendered entity depth layer only.
 *
 * <p>This is the depth-layer counterpart to the existing scene-pass and level-pass color-grade
 * effects.
 *
 * <p>It restores the missing world-space part of the old color-grade semantics for the depth pass:
 *
 * <ul>
 *   <li>optional hue override ({@code hue < 0} keeps the original hue)
 *   <li>saturation multiplier
 *   <li>value/brightness multiplier
 *   <li>optional world-space region
 *   <li>optional transition size around the region
 * </ul>
 *
 * <p>If no region is configured, the effect applies to the whole rendered depth layer. If a region
 * is configured, the effect is fully active inside that region and fades out smoothly across the
 * configured transition band outside the region.
 */
public final class DepthLayerColorGradeEffect
  implements DepthLayerEffectRegistry.ToggleableDepthLayerEffect {

  private float hue = -1.0f;
  private float saturationMultiplier = 1.0f;
  private float valueMultiplier = 1.0f;
  private Rectangle region = null;
  private float transitionSize = 2.0f;
  private boolean enabled = true;

  public DepthLayerColorGradeEffect() {}

  public DepthLayerColorGradeEffect(
    float hue, float saturationMultiplier, float valueMultiplier) {
    hue(hue);
    saturationMultiplier(saturationMultiplier);
    valueMultiplier(valueMultiplier);
  }

  public float hue() {
    return hue;
  }

  public DepthLayerColorGradeEffect hue(float hue) {
    this.hue = hue < 0f ? -1.0f : normalizeHue(hue);
    return this;
  }

  public float saturationMultiplier() {
    return saturationMultiplier;
  }

  public DepthLayerColorGradeEffect saturationMultiplier(float saturationMultiplier) {
    this.saturationMultiplier = Math.max(0f, saturationMultiplier);
    return this;
  }

  public float valueMultiplier() {
    return valueMultiplier;
  }

  public DepthLayerColorGradeEffect valueMultiplier(float valueMultiplier) {
    this.valueMultiplier = Math.max(0f, valueMultiplier);
    return this;
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
   * @param region region in world coordinates; {@code null} means full-depth-layer effect
   * @return this effect for chaining
   */
  public DepthLayerColorGradeEffect region(Rectangle region) {
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
  public DepthLayerColorGradeEffect transitionSize(float transitionSize) {
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
  public BufferedImage apply(BufferedImage input, int depthLayer, long nowMs) {
    if (input == null || !enabled) {
      return input;
    }

    BufferedImage output =
      new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_ARGB);

    Point focus = LitiengineCameraState.focusPosition();

    for (int y = 0; y < input.getHeight(); y++) {
      for (int x = 0; x < input.getWidth(); x++) {
        int argb = input.getRGB(x, y);
        int alpha = (argb >>> 24) & 0xFF;

        if (alpha == 0) {
          output.setRGB(x, y, 0);
          continue;
        }

        float influence = effectInfluenceAt(x, y, input.getWidth(), input.getHeight(), focus);
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

  private float effectInfluenceAt(
    int screenX, int screenY, int screenWidth, int screenHeight, Point focus) {
    if (region == null) {
      return 1f;
    }

    Point world =
      LitiengineCameraViews.screenToWorld(
        new Point((float) screenX, (float) screenY), focus, screenWidth, screenHeight);

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
