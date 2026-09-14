package rooms.programming.level;

import engine.utils.Rectangle;
import engine.utils.components.draw.shader.AbstractShader;
import java.util.List;
import java.util.Map;

/** Local, screen-space magic for the remote view of Nox. */
final class ProgrammingObservationShader extends AbstractShader {
  private float age;
  private float strength;

  ProgrammingObservationShader() {
    super("shaders/passthrough.vert", "shaders/programming-observation.frag");
  }

  void advance(float delta, float visibility) {
    age += delta;
    strength = Math.min(1, age / 0.65f) * visibility;
  }

  float strength() {
    return strength;
  }

  void strength(float value) {
    strength = value;
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of(new FloatUniform("u_age", age), new FloatUniform("u_strength", strength));
  }

  @Override
  protected void writeProperties(Map<String, String> properties) {
    // The observation dialog owns this transient effect, not the synchronized world.
  }

  @Override
  protected void readProperties(Map<String, String> properties) {}

  @Override
  public int padding() {
    return 0;
  }

  @Override
  public Rectangle worldBounds() {
    return null;
  }
}
