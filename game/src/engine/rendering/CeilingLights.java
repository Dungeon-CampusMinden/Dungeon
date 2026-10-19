package engine.rendering;

import com.badlogic.gdx.graphics.Color;
import engine.level.Tile;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import java.util.List;
import java.util.function.BiPredicate;

/**
 * Lamps hung from the ceiling over a level's floor, for rooms lit like offices or facilities. The
 * level is split into blocks of four by four tiles; every block with floor gets one lamp, over its
 * centre where that is floor, else over the floor tile closest to it, so small rooms and corridors
 * get lamps too.
 */
public final class CeilingLights {

  private static final int BLOCK = 4;
  private static final int CENTER = 2;

  private CeilingLights() {}

  /**
   * Adds a steady lamp over the floor of every block, if it is on.
   *
   * @param layout the level layout, indexed [y][x]
   * @param color the lamps' colour
   * @param intensity the lamps' brightness, see {@link Light#steady}
   * @param on whether the lamp over the floor tile at x, y is on; lamps can come on one after
   *     another
   * @param out receives the lights
   */
  public static void add(
      Tile[][] layout,
      Color color,
      float intensity,
      BiPredicate<Integer, Integer> on,
      List<Light> out) {
    for (int by = 0; by < layout.length; by += BLOCK) {
      for (int bx = 0; bx < layout[by].length; bx += BLOCK) {
        int lampX = -1;
        int lampY = -1;
        int best = Integer.MAX_VALUE;
        for (int y = by; y < Math.min(by + BLOCK, layout.length); y++) {
          for (int x = bx; x < Math.min(bx + BLOCK, layout[y].length); x++) {
            Tile t = layout[y][x];
            if (t == null || t.levelElement() != LevelElement.FLOOR) continue;
            int dx = x - bx - CENTER;
            int dy = y - by - CENTER;
            if (dx * dx + dy * dy < best) {
              best = dx * dx + dy * dy;
              lampX = x;
              lampY = y;
            }
          }
        }
        if (lampX < 0 || !on.test(lampX, lampY)) continue;
        out.add(Light.steady(new Point(lampX + 0.5f, lampY + 0.5f), color, intensity));
      }
    }
  }
}
