package engine.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import engine.utils.Point;

/**
 * A point light of the dynamic lighting pass.
 *
 * <p>The renderer collects lights anew every frame from the wall decoration, the drawn entities and
 * {@link engine.level.elements.ILevel#lights}; they carry no identity.
 *
 * @param position light centre in world units
 * @param color light colour (alpha ignored)
 * @param radius reach in tiles; the light fades to zero at this distance
 * @param intensity brightness multiplier; 1 lights a surface to its full texture colour
 * @param flicker strength of the fire-like brightness wobble, 0 for a steady light
 * @param motes whether magic motes in the light's colour rise from it
 */
public record Light(
    Point position, Color color, float radius, float intensity, float flicker, boolean motes) {

  /**
   * A point light without motes.
   *
   * @param position light centre in world units
   * @param color light colour (alpha ignored)
   * @param radius reach in tiles; the light fades to zero at this distance
   * @param intensity brightness multiplier; 1 lights a surface to its full texture colour
   * @param flicker strength of the fire-like brightness wobble, 0 for a steady light
   */
  public Light(Point position, Color color, float radius, float intensity, float flicker) {
    this(position, color, radius, intensity, flicker, false);
  }

  /**
   * A steady light whose reach grows with its strength, for status lamps and screens.
   *
   * @param position light centre in world units
   * @param color light colour
   * @param intensity brightness multiplier; the light reaches five tiles per unit of intensity
   * @return the light
   */
  public static Light steady(Point position, Color color, float intensity) {
    return new Light(position, color, 5 * intensity, intensity, 0f);
  }

  /**
   * The fire-like wobble of a flame at the given point: two slow waves, about once and twice a
   * second, so flames breathe rather than vibrate. Nearby flames are out of phase.
   *
   * @param position the flame or light position, seeds the phase
   * @param time seconds since start
   * @return the wobble, between -1 and 1
   */
  static float wobble(Point position, float time) {
    float seed = position.x() * 12.9898f + position.y() * 78.233f;
    return 0.6f * MathUtils.sin(time * 6.3f + seed)
        + 0.4f * MathUtils.sin(time * 13.1f + seed * 2.1f);
  }
}
