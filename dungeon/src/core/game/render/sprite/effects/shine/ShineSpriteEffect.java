package core.game.render.sprite.effects.shine;

import core.game.render.effects.ToggleableEffect;
import core.game.render.sprite.effects.SpriteEffect;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * A sprite effect that renders an animated shine overlay on sprites.
 *
 * <p>This effect creates a rotating light shine effect that moves across the sprite surface.
 * It supports both small and large sprites with different rendering strategies:
 * <ul>
 *   <li>Small sprites use a single diagonal sweep</li>
 *   <li>Large sprites use repeated rotating slices</li>
 * </ul>
 *
 * <p>The effect includes configurable parameters such as padding, slice count, gap size, rotation speed,
 * and shine color. Results are cached for improved performance.
 */
public final class ShineSpriteEffect
  implements SpriteEffect, ToggleableEffect<ShineSpriteEffect> {

  private int padding = 20;
  private int sliceCount = 4;
  private float gapSize = 0.2f;
  private float rotationSpeed = 0.2f;
  private Color shineColor = new Color(255, 255, 128, 255);
  private boolean enabled = true;
  private final ShineAnimationClock animationClock = new ShineAnimationClock();

  /**
   * Creates a shine effect with default parameters.
   */
  public ShineSpriteEffect() {}

  /**
   * Gets the padding around the sprite for the shine effect.
   *
   * @return the padding in pixels
   */
  public int padding() {
    return padding;
  }

  /**
   * Sets the padding around the sprite for the shine effect.
   *
   * @param padding the padding in pixels (negative values are clamped to 0)
   * @return this effect for method chaining
   */
  public ShineSpriteEffect padding(int padding) {
    this.padding = Math.max(0, padding);
    return this;
  }

  /**
   * Sets the number of shine slices for large sprites.
   *
   * @param sliceCount the number of slices (minimum 1)
   * @return this effect for method chaining
   */
  public ShineSpriteEffect sliceCount(int sliceCount) {
    this.sliceCount = Math.max(1, sliceCount);
    return this;
  }

  /**
   * Sets the gap size between shine slices.
   *
   * @param gapSize the gap size in the range [0, 1]
   * @return this effect for method chaining
   */
  public ShineSpriteEffect gapSize(float gapSize) {
    this.gapSize = clamp01(gapSize);
    return this;
  }

  /**
   * Sets the rotation speed of the shine effect.
   *
   * @param rotationSpeed the rotation speed in rotations per second
   * @return this effect for method chaining
   */
  public ShineSpriteEffect rotationSpeed(float rotationSpeed) {
    this.rotationSpeed = rotationSpeed;
    return this;
  }

  /**
   * Sets the color of the shine overlay.
   *
   * @param shineColor the shine color (must not be null)
   * @return this effect for method chaining
   * @throws IllegalArgumentException if shineColor is null
   */
  public ShineSpriteEffect shineColor(Color shineColor) {
    if (shineColor == null) {
      throw new IllegalArgumentException("shineColor must not be null");
    }
    this.shineColor = shineColor;
    return this;
  }

  /**
   * Sets whether this effect is enabled.
   *
   * @param enabled true to enable the effect, false to disable it
   * @return this effect for method chaining
   */
  public ShineSpriteEffect enabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  @Override
  public boolean enabled() {
    return enabled;
  }

  /**
   * Shine is rendered as a separate overlay image at draw time.
   */
  @Override
  public BufferedImage apply(BufferedImage input, long nowMs) {
    return input;
  }

  /**
   * Creates the animated shine overlay for the given sprite.
   *
   * <p>The returned image can be larger than the input sprite because the old GDX shader also
   * supported explicit padding around the rendered effect.
   *
   * @param source already prepared sprite image (after tint / other pixel effects)
   * @param nowMs current timestamp
   * @return transparent overlay image containing only the shine bands
   */
  public BufferedImage createOverlay(BufferedImage source, long nowMs) {
    if (source == null || !enabled) {
      return null;
    }

    return ShineOverlayRenderer.render(
      source, renderConfig(), animationClock.elapsedSeconds(nowMs));
  }

  private static float clamp01(float value) {
    return Math.clamp(value, 0.0f, 1.0f);
  }

  private ShineRenderConfig renderConfig() {
    return new ShineRenderConfig(padding, sliceCount, gapSize, rotationSpeed, shineColor);
  }
}
