package rooms.programming.level;

import com.badlogic.gdx.math.Vector3;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.components.draw.shader.AbstractShader;
import feature.systems.LevelEditorSystem;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Local light follows the same visible objects and torch states on every client. */
final class ProgrammingLightingShader extends AbstractShader {
  private static final float AMBIENT_LIGHT = 0.82f;
  // Match programming-lighting.frag's capacity. Filter extinguished sources before allocating
  // slots.
  private static final int MAX_LIGHTS = 100;

  ProgrammingLightingShader() {
    super("shaders/passthrough.vert", "shaders/programming-lighting.frag");
  }

  @Override
  public boolean enabled() {
    return super.enabled() && !LevelEditorSystem.active();
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    List<Vector3> positions = new ArrayList<>();
    List<Vector3> colors = new ArrayList<>();
    Game.levelEntities()
        .filter(ProgrammingLightingShader::lightSource)
        .filter(ProgrammingLightingShader::illuminated)
        .sorted(Comparator.comparing(Entity::name))
        .limit(MAX_LIGHTS)
        .forEach(
            entity -> {
              PositionComponent position = entity.fetch(PositionComponent.class).orElseThrow();
              boolean golem = entity.name().equals("programming-variables-golem");
              boolean kettle = entity.name().startsWith("programming-prop-forge-kettle-");
              Point at = position.position();
              // z controls the radius; the flame sits above the wooden torch holder.
              positions.add(
                  new Vector3(
                      at.x() + (golem ? 2.5f : 0.5f),
                      at.y() + (golem ? 2.5f : kettle ? 0.7f : 1.25f),
                      golem ? 1.3f : kettle ? 0.7f : 1.0f));
              colors.add(
                  golem
                      ? new Vector3(0.24f, 0.10f, 0.48f)
                      : kettle
                          ? new Vector3(0.30f, 0.18f, 0.06f)
                          : new Vector3(0.90f, 0.50f, 0.16f));
            });
    return List.of(
        new FloatUniform("u_ambientLight", AMBIENT_LIGHT),
        new Vector3ArrayUniform("u_lightSources", positions),
        new Vector3ArrayUniform("u_lightColors", colors),
        new Vector3ArrayUniform(
            "u_steamSources",
            Game.currentLevel()
                .map(
                    level -> {
                      List<Vector3> steam = new ArrayList<>();
                      // Native pipe rupture (4, 11) in an 8x24 sprite, drawn 0.65 tiles wide.
                      for (Point at :
                          ProgrammingMazeWorld.steamOutlets(level.namedPoints().get("maze-origin")))
                        steam.add(new Vector3(at.x() + .325f, at.y() + 1.05625f, 1));
                      Point sluice = level.namedPoints().get("loop-departure");
                      steam.add(new Vector3(sluice.x() - 1, sluice.y(), 1.6f));
                      return steam;
                    })
                .orElse(List.of())));
  }

  private static boolean lightSource(Entity entity) {
    String name = entity.name();
    return name.equals("programming-variables-golem")
        || name.startsWith("programming-prop-forge-kettle-")
        || name.startsWith("programming-prop-torch-");
  }

  private static boolean illuminated(Entity entity) {
    DrawComponent draw = entity.fetch(DrawComponent.class).orElse(null);
    return draw != null
        && entity.fetch(PositionComponent.class).isPresent()
        && draw.isVisible()
        && (draw.tintColor() & 0xFF) != 0
        && (entity.name().equals("programming-variables-golem")
            || entity.name().startsWith("programming-prop-forge-kettle-")
            || draw.stateMachine().getCurrentStateName().equals("on"));
  }

  @Override
  public int padding() {
    return 0;
  }

  @Override
  public Rectangle worldBounds() {
    return null;
  }
}
