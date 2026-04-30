package core.game.render.sprite.effects;

import core.Component;
import java.awt.Color;
import java.util.Objects;

/**
 * A component that defines visual outline effect properties for entities.
 *
 * <p>SpriteOutlineComponent encapsulates all parameters needed to render an outline effect around a
 * sprite, including width, color, and optional animations. It supports pulsing effects and rainbow
 * color cycling.
 *
 * <p>Properties:
 *
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

  private final int width;
  private final Color color;
  private final float beatSpeed;
  private final float beatIntensity;
  private final boolean rainbow;

  /**
   * Constructs an OutlineEffectComponent with all parameters specified.
   *
   * @param width the base outline thickness in pixels (must be at least 1)
   * @param color the outline color (must not be null)
   * @param beatSpeed the animation speed multiplier for pulsing and rainbow effects
   * @param beatIntensity the intensity of the pulsing effect, clamped to [0, ∞) (0 disables
   *     pulsing)
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
   * Returns the outline width in pixels.
   *
   * @return the current outline width (minimum 1)
   */
  public int width() {
    return width;
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
   * Returns the animation speed multiplier.
   *
   * @return the current beat speed for animation
   */
  public float beatSpeed() {
    return beatSpeed;
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
   * Returns whether rainbow color cycling is enabled.
   *
   * @return true if rainbow mode is active, false otherwise
   */
  public boolean rainbow() {
    return rainbow;
  }
}
