package rooms.morphco.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.TimeUtils;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.Tile;
import engine.rendering.Atmosphere;
import engine.rendering.CeilingLights;
import engine.rendering.Light;
import engine.utils.Point;
import engine.utils.Vector2;
import java.util.List;
import java.util.Map;

/**
 * Lighting of the factory, shared by the server level (single player, level editor) and the client
 * level.
 *
 * <p>Neutral ceiling tubes light the halls. While the alarm runs, a red light pulses at the warning
 * light on the wall of the control room; it disappears together with the synchronized alarm light
 * entity. Once the fuse box is wired, the synchronized emergency light entity switches on a small
 * green light on the lamp of the box; until then the lamp glows faintly red. The error displays of
 * F-04 and A-12 glow red until their machine is repaired.
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
  private static final Color SCREEN_ERROR = new Color(1f, 0.2f, 0.12f, 1f);

  private static final String FUSE_BOX = "fuse-box";

  // The error displays and the fuse box by instance name, with the centre of their screen or
  // lamp. They show the error while they are off (MorphCoProps.OFF).
  private static final Map<String, Vector2> ERROR_LIGHTS =
      Map.of(
          "f04-screen",
          Vector2.of(1.25f, 1.45f),
          "a12-display",
          Vector2.of(1.5f, 0.55f),
          FUSE_BOX,
          Vector2.of(1f, 2.45f));

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
   * Adds ceiling tubes, the pulsing alarm light while the alarm runs, the emergency light once it
   * is on and the glow of the error displays.
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
        .ifPresent(position -> {
          float pulse = 0.5f + 0.5f * (float) Math.sin(TimeUtils.millis() / 180.0);
          Point center = position.position().translate(0.2f, 0.4f);
          out.add(new Light(center, ALARM, 4.5f, 0.6f + 0.9f * pulse, 0f));
        });
    if (Game.levelEntities().anyMatch(entity -> EMERGENCY_LIGHT.equals(entity.name())))
      Game.levelEntities()
          .filter(entity -> FUSE_BOX.equals(entity.name()))
          .findFirst()
          .flatMap(entity -> entity.fetch(PositionComponent.class))
          .ifPresent(position -> {
            Point lamp = position.position().translate(ERROR_LIGHTS.get(FUSE_BOX));
            out.add(new Light(lamp, EMERGENCY, 1.5f, 1.2f, 0f));
          });
    Game.levelEntities()
        .filter(entity -> ERROR_LIGHTS.containsKey(entity.name()))
        .filter(entity ->
            entity
                .fetch(DrawComponent.class)
                .map(DrawComponent::currentStateName)
                .filter(MorphCoProps.OFF::equals)
                .isPresent())
        .forEach(entity -> {
          Point position = entity.fetch(PositionComponent.class).orElseThrow().position();
          Point screen = position.translate(ERROR_LIGHTS.get(entity.name()));
          out.add(Light.steady(screen, SCREEN_ERROR, 0.5f));
        });
  }
}
