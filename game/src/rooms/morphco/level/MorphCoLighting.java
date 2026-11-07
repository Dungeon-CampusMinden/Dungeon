package rooms.morphco.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.TimeUtils;
import engine.Game;
import engine.components.PositionComponent;
import engine.level.Tile;
import engine.rendering.Atmosphere;
import engine.rendering.CeilingLights;
import engine.rendering.Light;
import engine.utils.Point;
import java.util.List;

/**
 * Lighting of the factory, shared by the server level (single player, level editor) and the client
 * level.
 *
 * <p>Neutral ceiling tubes light the halls. While the alarm runs, a red light pulses over the
 * console in the control room; it disappears together with the synchronized alarm light entity.
 */
public final class MorphCoLighting {

  /** Name of the entity whose presence means that the alarm is active. */
  public static final String ALARM_LIGHT = "morph-co-alarm-light";

  private static final Atmosphere ATMOSPHERE =
      Atmosphere.NEUTRAL.withBrightness(0.55f).withLantern(0.6f);
  private static final Color TUBE = new Color(1f, 0.96f, 0.88f, 1f);
  private static final Color ALARM = new Color(1f, 0.12f, 0.08f, 1f);

  private MorphCoLighting() {}

  /**
   * Returns the atmosphere of the factory.
   *
   * @return atmosphere
   */
  public static Atmosphere atmosphere() {
    return ATMOSPHERE;
  }

  /**
   * Adds ceiling tubes and, while the alarm runs, the pulsing alarm light.
   *
   * @param layout level layout
   * @param out receives the lights
   */
  public static void lights(Tile[][] layout, List<Light> out) {
    CeilingLights.add(layout, TUBE, 0.8f, (x, y) -> true, out);
    Game.levelEntities()
        .filter(entity -> ALARM_LIGHT.equals(entity.name()))
        .findFirst()
        .flatMap(entity -> entity.fetch(PositionComponent.class))
        .ifPresent(
            position -> {
              float pulse = 0.5f + 0.5f * (float) Math.sin(TimeUtils.millis() / 180.0);
              Point center = position.position().translate(0.2f, 0.4f);
              out.add(new Light(center, ALARM, 4.5f, 0.6f + 0.9f * pulse, 0f));
            });
  }
}
