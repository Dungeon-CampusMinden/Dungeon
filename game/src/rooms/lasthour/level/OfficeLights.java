package rooms.lasthour.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.TimeUtils;
import engine.level.Tile;
import engine.rendering.CeilingLights;
import engine.rendering.Light;
import engine.utils.settings.ClientSettings;
import java.util.List;

/**
 * The ceiling lights of the office. They come on with the room lights and flicker like fluorescent
 * tubes while they start, each tube a little after the other; with reduced effects they come on at
 * once.
 */
final class OfficeLights {

  private static final Color TUBE = new Color(0.86f, 0.93f, 1f, 1f);
  private static final float INTENSITY = 0.85f;
  // Seconds after switching on at which a tube toggles between dark and lit; it stays lit after the
  // last toggle.
  private static final float[] STARTUP = {0.1f, 0.3f, 0.5f};
  private static final float MAX_DELAY = 0.2f;
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
    CeilingLights.add(layout, TUBE, INTENSITY, (x, y) -> lit(delay(x, y)), out);
  }

  private static boolean lit(float delay) {
    if (ClientSettings.reduceEffects()) return true;
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
