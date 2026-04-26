package core.game.render.effects;

import core.utils.Point;
import core.utils.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Shared HSV-style color grading implementation for render pass effects.
 *
 * <p>Subclasses provide only the coordinate mapping from buffer pixels to world-space positions.
 *
 * @param <T> concrete effect type for fluent setters
 */
public abstract class BaseColorGradeEffect<T extends BaseColorGradeEffect<T>> {

  private float hue = -1.0f;
  private float saturationMultiplier = 1.0f;
  private float valueMultiplier = 1.0f;
  private Rectangle region = null;
  private float transitionSize = 2.0f;
  private boolean enabled = true;

  /** Creates a neutral color-grade effect. */
  protected BaseColorGradeEffect() {}

  /**
   * Creates a color-grade effect with the given HSV parameters.
   *
   * @param hue target hue in {@code [0, 1]}; values {@code < 0} keep the original hue
   * @param saturationMultiplier saturation multiplier; negative values are clamped to 0
   * @param valueMultiplier value/brightness multiplier; negative values are clamped to 0
   */
  protected BaseColorGradeEffect(float hue, float saturationMultiplier, float valueMultiplier) {
    hue(hue);
    saturationMultiplier(saturationMultiplier);
    valueMultiplier(valueMultiplier);
  }

  /**
   * Returns the concrete subclass instance.
   *
   * @return concrete effect instance
   */
  protected abstract T self();

  /**
   * Returns the target hue for the color-grade effect.
   *
   * @return the target hue in the range [0, 1], or a value less than 0 if the original hue is
   *     retained
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
  public T hue(float hue) {
    this.hue = ColorGradeUtils.normalizeTargetHue(hue);
    return self();
  }

  /**
   * Sets the saturation multiplier.
   *
   * @param saturationMultiplier multiplier for saturation; negative values are clamped to 0
   * @return this effect for chaining
   */
  public T saturationMultiplier(float saturationMultiplier) {
    this.saturationMultiplier = ColorGradeUtils.clampMultiplier(saturationMultiplier);
    return self();
  }

  /**
   * Sets the value/brightness multiplier.
   *
   * @param valueMultiplier multiplier for value/brightness; negative values are clamped to 0
   * @return this effect for chaining
   */
  public T valueMultiplier(float valueMultiplier) {
    this.valueMultiplier = ColorGradeUtils.clampMultiplier(valueMultiplier);
    return self();
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
   * Sets the optional world-space region for this effect.
   *
   * @param region region in world coordinates; {@code null} means global effect
   * @return this effect for chaining
   */
  public T region(Rectangle region) {
    this.region = region;
    return self();
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
  public T transitionSize(float transitionSize) {
    this.transitionSize = Math.max(0f, transitionSize);
    return self();
  }

  /**
   * Checks whether the effect is enabled.
   *
   * @return true if the effect should be applied
   */
  public boolean enabled() {
    return enabled;
  }

  /**
   * Sets the enabled state.
   *
   * @param enabled true to enable the effect, false to disable it
   * @return this effect for chaining
   */
  public T enabled(boolean enabled) {
    this.enabled = enabled;
    return self();
  }

  /**
   * Applies color grading to an input image.
   *
   * @param input input image
   * @param worldPointResolver maps buffer pixels to world-space positions when a region is set
   * @return transformed image, or the original input if disabled or input is null
   */
  protected final BufferedImage applyColorGrade(
      BufferedImage input, WorldPointResolver worldPointResolver) {
    if (input == null || !enabled) {
      return input;
    }

    if (region != null) {
      Objects.requireNonNull(worldPointResolver, "worldPointResolver");
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

        float influence = effectInfluenceAt(x, y, worldPointResolver);
        if (influence <= 0f) {
          output.setRGB(x, y, argb);
          continue;
        }

        int gradedArgb =
            ColorGradeUtils.gradeArgb(argb, hue, saturationMultiplier, valueMultiplier);

        if (influence >= 1f) {
          output.setRGB(x, y, gradedArgb);
          continue;
        }

        output.setRGB(x, y, blendArgb(argb, gradedArgb, influence));
      }
    }

    return output;
  }

  /** Maps an effect buffer pixel to a world-space point. */
  @FunctionalInterface
  protected interface WorldPointResolver {
    /**
     * Resolves a buffer pixel to world-space coordinates.
     *
     * @param x buffer x-coordinate
     * @param y buffer y-coordinate
     * @return world-space point
     */
    Point worldPoint(int x, int y);
  }

  private float effectInfluenceAt(int x, int y, WorldPointResolver worldPointResolver) {
    if (region == null) {
      return 1f;
    }

    Point world = worldPointResolver.worldPoint(x, y);

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

  private static float clamp01(float value) {
    return Math.clamp(value, 0f, 1f);
  }
}
