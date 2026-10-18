package engine.rendering;

import com.badlogic.gdx.graphics.Color;
import engine.level.Tile;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Lamps hung from the ceiling in a regular grid over a level's floor, for rooms lit like offices or
 * facilities.
 */
public final class CeilingLights {

  // A lamp hangs over every fourth floor tile in both directions.
  private static final int SPACING = 4;
  private static final int OFFSET = 2;

  private CeilingLights() {}

  /**
   * Adds a steady lamp over every fourth floor tile in both directions.
   *
   * @param layout the level layout, indexed [y][x]
   * @param color the lamps' colour
   * @param intensity the lamps' brightness, see {@link Light#steady}
   * @param out receives the lights
   */
  public static void add(Tile[][] layout, Color color, float intensity, List<Light> out) {
    add(layout, color, intensity, (x, y) -> true, out);
  }

  /**
   * Adds the lamps that are on, for lamps that come on one after another.
   *
   * @param layout the level layout, indexed [y][x]
   * @param color the lamps' colour
   * @param intensity the lamps' brightness, see {@link Light#steady}
   * @param on whether the lamp over the floor tile at x, y is on
   * @param out receives the lights
   */
  public static void add(
      Tile[][] layout,
      Color color,
      float intensity,
      BiPredicate<Integer, Integer> on,
      List<Light> out) {
    for (int y = OFFSET; y < layout.length; y += SPACING) {
      for (int x = OFFSET; x < layout[y].length; x += SPACING) {
        if (layout[y][x].levelElement() != LevelElement.FLOOR || !on.test(x, y)) continue;
        out.add(Light.steady(new Point(x + 0.5f, y + 0.5f), color, intensity));
      }
    }
  }
}
