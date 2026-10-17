package engine.rendering;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import java.util.List;

/**
 * Derives the lights of entities from what the client already renders.
 *
 * <p>Players carry a lantern as bright as the level's {@link Atmosphere#lantern()}; sprites that
 * show fire, magic or lamps glow in a matching colour. Because the lights depend only on
 * synchronised draw state, single player and every multiplayer client light the scene identically.
 */
final class SceneLights {

  private record Preset(
      String pathPart, Color color, float radius, float intensity, float flicker, boolean trail) {}

  private static final List<Preset> EMISSIVE =
      List.of(
          new Preset("objects/torch", new Color(1f, 0.56f, 0.24f, 1f), 6.5f, 0.9f, 0.14f, false),
          new Preset("fireball", new Color(1f, 0.5f, 0.16f, 1f), 4.5f, 1.4f, 0.3f, true),
          new Preset("magic_kettle", new Color(0.62f, 0.35f, 1f, 1f), 4f, 1f, 0.08f, false),
          new Preset("cauldron", new Color(0.35f, 1f, 0.45f, 1f), 3.5f, 0.9f, 0.08f, false),
          new Preset("lamp", new Color(1f, 0.9f, 0.72f, 1f), 5.5f, 1f, 0f, false),
          new Preset("lava", new Color(1f, 0.42f, 0.12f, 1f), 3f, 0.9f, 0.15f, false));

  private static final Color LANTERN = new Color(1f, 0.84f, 0.62f, 1f);

  private SceneLights() {}

  /**
   * Adds the light the entity emits, if any.
   *
   * @param entity the drawn entity
   * @param dc its draw component
   * @param pc its position component
   * @param lantern intensity of the players' lantern, 0 for none
   * @param out receives the light
   * @param flames receives the flame tips of fire lights, for ember particles
   * @param trails receives the centres of flying fire, which leaves a trail of sparks
   * @return true if the entity is a glowing object (fire, magic, lamp); false for players and
   *     entities without light
   */
  static boolean collect(
      Entity entity,
      DrawComponent dc,
      PositionComponent pc,
      float lantern,
      List<Light> out,
      List<Point> flames,
      List<Point> trails) {
    // A fully transparent tint hides the entity, for example while fog of war covers it.
    if (!dc.isVisible() || (dc.tintColor() & 0xff) == 0) return false;
    float w = dc.getWidth() * pc.scale().x();
    float h = dc.getHeight() * pc.scale().y();
    Point origin = pc.position();

    if (entity.isPresent(PlayerComponent.class)) {
      if (lantern > 0) {
        out.add(new Light(origin.translate(w / 2f, h * 0.45f), LANTERN, 7f, lantern, 0.04f));
      }
      return false;
    }

    String path =
        dc.currentAnimation().sourcePath().map(p -> p.pathString().toLowerCase()).orElse("");
    if (path.isEmpty() || "off".equals(dc.currentStateName())) return false;
    for (Preset preset : EMISSIVE) {
      if (path.contains(preset.pathPart())) {
        Point center = origin.translate(w / 2f, h * 0.6f);
        out.add(
            new Light(
                center, preset.color(), preset.radius(), preset.intensity(), preset.flicker()));
        if (preset.trail()) trails.add(origin.translate(w / 2f, h / 2f));
        else if (preset.flicker() > 0.1f) flames.add(origin.translate(w / 2f, h * 0.85f));
        return true;
      }
    }
    return false;
  }
}
