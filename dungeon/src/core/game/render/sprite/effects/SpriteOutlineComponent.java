package core.game.render.sprite.effects;

import core.Component;
import java.awt.Color;
import java.util.Objects;

/**
 * A component that defines visual outline effect properties for entities.
 *
 * <p>SpriteOutlineComponent encapsulates all parameters needed to render an outline effect around
 * a sprite, including width, color, and optional animations. It supports pulsing effects and
 * rainbow color cycling.
 *
 * <p>Properties:
 * <ul>
 *   <li>width - The base thickness of the outline in pixels (minimum 1)
 *   <li>color - The outline color (or base color for rainbow mode)
 *   <li>beatSpeed - The animation speed for pulsing and rainbow effects (default 1.0)
 *   <li>beatIntensity - The intensity of pulsing animation, 0 for no pulsing (default 0.0)
 *   <li>rainbow - Whether to animate the color through the hue spectrum (default false)
 * </ul>
 *
 * <p>This component follows the fluent builder pattern, allowing method chaining for convenient
 * configuration.
 */
public final class SpriteOutlineComponent implements Component {

  private int width;
  private Color color;
  private float beatSpeed;
  private float beatIntensity;
  private boolean rainbow;

  /**
   * Constructs an OutlineEffectComponent with all parameters specified.
   *
   * @param width the base outline thickness in pixels (must be at least 1)
   * @param color the outline color (must not be null)
   * @param beatSpeed the animation speed multiplier for pulsing and rainbow effects
   * @param beatIntensity the intensity of the pulsing effect, clamped to [0, ∞) (0 disables pulsing)
   * @throws NullPointerException if color is null
   */
  public SpriteOutlineComponent(
    final int width, final Color color, final float beatSpeed, final float beatIntensity) {
    this.width = Math.max(1, width);
    this.color = Objects.requireNonNull(color, "color");
    this.beatSpeed = beatSpeed;
    this.beatIntensity = Math.max(0f, beatIntensity);
    this.rainbow = false;
  }

  /**
   * Constructs an OutlineEffectComponent with default animation parameters.
   *
   * <p>Uses default values: beatSpeed = 1.0, beatIntensity = 0 (no pulsing), rainbow = false.
   *
   * @param width the base outline thickness in pixels (must be at least 1)
   * @param color the outline color (must not be null)
   * @throws NullPointerException if color is null
   */
  public SpriteOutlineComponent(final int width, final Color color) {
    this(width, color, 1.0f, 0f);
  }

  /**
   * Constructs an OutlineEffectComponent with a white outline and default parameters.
   *
   * <p>Uses default values: color = white, beatSpeed = 1.0, beatIntensity = 0, rainbow = false.
   *
   * @param width the base outline thickness in pixels (must be at least 1)
   */
  public SpriteOutlineComponent(final int width) {
    this(width, Color.WHITE);
  }

  /**
   * Returns the outline width in pixels.
   *
   * @return the current outline width (minimum 1)
   */
  public int width() {
    return width;
  }

  /**
   * Sets the outline width.
   *
   * @param width the outline thickness in pixels (will be clamped to at least 1)
   * @return this component for method chaining
   */
  public SpriteOutlineComponent width(final int width) {
    this.width = Math.max(1, width);
    return this;
  }

  /**
   * Returns the outline color.
   *
   * @return the current outline color
   */
  public Color color() {
    return color;
  }

  /**
   * Sets the outline color.
   *
   * @param color the new outline color (must not be null)
   * @return this component for method chaining
   * @throws NullPointerException if color is null
   */
  public SpriteOutlineComponent color(final Color color) {
    this.color = Objects.requireNonNull(color, "color");
    return this;
  }

  /**
   * Returns the animation speed multiplier.
   *
   * @return the current beat speed for animation
   */
  public float beatSpeed() {
    return beatSpeed;
  }

  /**
   * Sets the animation speed multiplier.
   *
   * <p>Controls the frequency of pulsing and rainbow color cycling effects.
   *
   * @param beatSpeed the animation speed multiplier
   * @return this component for method chaining
   */
  public SpriteOutlineComponent beatSpeed(final float beatSpeed) {
    this.beatSpeed = beatSpeed;
    return this;
  }

  /**
   * Returns the beat intensity (pulsing effect strength).
   *
   * @return the current beat intensity (0 means no pulsing)
   */
  public float beatIntensity() {
    return beatIntensity;
  }

  /**
   * Sets the beat intensity for the pulsing effect.
   *
   * <p>A value of 0 disables pulsing. Higher values create stronger pulsing animations.
   * Negative values are clamped to 0.
   *
   * @param beatIntensity the pulsing effect intensity (will be clamped to at least 0)
   * @return this component for method chaining
   */
  public SpriteOutlineComponent beatIntensity(final float beatIntensity) {
    this.beatIntensity = Math.max(0f, beatIntensity);
    return this;
  }

  /**
   * Returns whether rainbow color cycling is enabled.
   *
   * @return true if rainbow mode is active, false otherwise
   */
  public boolean rainbow() {
    return rainbow;
  }

  /**
   * Sets whether to enable rainbow color cycling animation.
   *
   * <p>When enabled, the outline color cycles through the hue spectrum at a rate
   * controlled by beatSpeed.
   *
   * @param rainbow true to enable rainbow animation, false to disable
   * @return this component for method chaining
   */
  public SpriteOutlineComponent rainbow(final boolean rainbow) {
    this.rainbow = rainbow;
    return this;
  }
}
