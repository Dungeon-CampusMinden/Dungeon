package rooms.morphco.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.TimeUtils;
import engine.Game;
import engine.components.PositionComponent;
import engine.level.Tile;
import engine.level.elements.tile.DoorTile;
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
 * Once the fuse box is wired, the synchronized emergency light entity switches on green lights over
 * every door and on the lamps of the box.
 */
public final class MorphCoLighting {

  /** Name of the entity whose presence means that the alarm is active. */
  public static final String ALARM_LIGHT = "morph-co-alarm-light";

  /** Name of the entity at the fuse box whose presence means that the emergency lighting is on. */
  public static final String EMERGENCY_LIGHT = "morph-co-emergency-light";

  private static final Atmosphere ATMOSPHERE =
      Atmosphere.NEUTRAL.withBrightness(0.55f).withLantern(0.6f);
  private static final Color TUBE = new Color(1f, 0.96f, 0.88f, 1f);
  private static final Color ALARM = new Color(1f, 0.12f, 0.08f, 1f);
  private static final Color EMERGENCY = new Color(0.3f, 1f, 0.45f, 1f);

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
   * Adds ceiling tubes, the pulsing alarm light while the alarm runs and the emergency lighting
   * once it is on.
   *
   * @param layout level layout
   * @param doors door tiles of the level
   * @param out receives the lights
   */
  public static void lights(Tile[][] layout, List<DoorTile> doors, List<Light> out) {
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
    Game.levelEntities()
        .filter(entity -> EMERGENCY_LIGHT.equals(entity.name()))
        .findFirst()
        .flatMap(entity -> entity.fetch(PositionComponent.class))
        .ifPresent(
            position -> {
              out.add(
                  new Light(position.position().translate(-0.5f, 0.6f), EMERGENCY, 1.5f, 1.2f, 0f));
              for (DoorTile door : doors) {
                out.add(new Light(door.position().translate(0.5f, 0.9f), EMERGENCY, 2.2f, 1f, 0f));
              }
            });
  }
}
