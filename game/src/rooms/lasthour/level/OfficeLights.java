package rooms.lasthour.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.TimeUtils;
import engine.level.Tile;
import engine.level.utils.LevelElement;
import engine.rendering.Light;
import engine.utils.Point;
import java.util.List;

/**
 * The ceiling lights of the office. They come on with the room lights and flicker like fluorescent
 * tubes while they start, each tube a little after the other.
 */
final class OfficeLights {

  // A tube hangs over every fourth floor tile in both directions.
  private static final int SPACING = 4;
  private static final int OFFSET = 2;
  private static final Color TUBE = new Color(0.86f, 0.93f, 1f, 1f);
  private static final float INTENSITY = 0.85f;
  // Seconds after switching on at which a tube toggles between dark and lit; it stays lit after the
  // last toggle.
  private static final float[] STARTUP = {0.05f, 0.12f, 0.3f, 0.36f, 0.62f, 0.7f, 0.95f};
  private static final float MAX_DELAY = 0.45f;
  private static final float MILLIS = 1000f;

  private static boolean wasOn;
  private static long onSince;

  private OfficeLights() {}

  /**
   * Notes whether the room lights are on; switching them on starts the flicker.
   *
   * @param on whether the room lights are on
   */
  static void update(boolean on) {
    if (on && !wasOn) onSince = TimeUtils.millis();
    wasOn = on;
  }

  /**
   * How far the room lights are lit while they start: 0 or 1 during the flicker, 1 afterwards.
   *
   * @return the brightness factor of the room lights without delay
   */
  static float startup() {
    return lit(0f) ? 1f : 0f;
  }

  /**
   * Adds the lit ceiling tubes.
   *
   * @param layout the level layout, indexed [y][x]
   * @param out receives the lights
   */
  static void add(Tile[][] layout, List<Light> out) {
    for (int y = OFFSET; y < layout.length; y += SPACING) {
      for (int x = OFFSET; x < layout[y].length; x += SPACING) {
        if (layout[y][x].levelElement() != LevelElement.FLOOR || !lit(delay(x, y))) continue;
        out.add(Light.steady(new Point(x + 0.5f, y + 0.5f), TUBE, INTENSITY));
      }
    }
  }

  private static boolean lit(float delay) {
    float t = (TimeUtils.millis() - onSince) / MILLIS - delay;
    int toggles = 0;
    while (toggles < STARTUP.length && STARTUP[toggles] <= t) toggles++;
    return toggles % 2 == 1;
  }

  private static float delay(int x, int y) {
    // Positional hash, so the tubes start in the same order on every client.
    int hash = (x * 73856093) ^ (y * 19349663);
    return (hash & 0xff) / 255f * MAX_DELAY;
  }
}
