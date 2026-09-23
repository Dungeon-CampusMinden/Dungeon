package rooms.systemRecovery.util.shaders;

import engine.utils.Rectangle;
import engine.utils.components.draw.shader.AbstractShader;
import java.util.List;
import java.util.Map;

/** Full-scene red alarm filter used after the system-core access module is accepted. */
public final class SystemRecoveryAlarmShader extends AbstractShader {
  /** Creates the alarm post-processing shader. */
  public SystemRecoveryAlarmShader() {
    super("shaders/passthrough.vert", "shaders/systemRecovery/alarm.frag");
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of();
  }

  @Override
  public int padding() {
    return 0;
  }

  @Override
  public Rectangle worldBounds() {
    return null;
  }

  @Override
  protected void writeProperties(Map<String, String> properties) {}

  @Override
  protected void readProperties(Map<String, String> properties) {}
}
