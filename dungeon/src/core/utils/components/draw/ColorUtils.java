package core.utils.components.draw;

import com.badlogic.gdx.graphics.Color;

/** Utility class for color operations. */
public class ColorUtils {

  /**
   * Converts a color to premultiplied alpha (PMA) format.
   *
   * @param color The original color.
   * @return The color in premultiplied alpha format.
   */
  public static Color pmaColor(Color color) {
    return new Color(color.r * color.a, color.g * color.a, color.b * color.a, color.a);
  }

  /**
   * Converts an RGBA8888 integer to premultiplied alpha (PMA) format.
   *
   * @param rgba8888 The color in RGBA8888 integer format.
   * @return The color in premultiplied alpha format.
   */
  public static Color pmaColor(int rgba8888) {
    return pmaColor(new Color(rgba8888));
  }
}
