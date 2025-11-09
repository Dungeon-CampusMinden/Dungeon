package core.utils.components.draw;

import com.badlogic.gdx.graphics.Color;

public class ColorUtil {

  public static Color pmaColor(Color color) {
    return new Color(color.r * color.a, color.g * color.a, color.b * color.a, color.a);
  }

  public static Color pmaColor(int rgba8888) {
    return pmaColor(new Color(rgba8888));
  }
}
