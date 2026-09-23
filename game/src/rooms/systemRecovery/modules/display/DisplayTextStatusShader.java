package rooms.systemRecovery.modules.display;

import engine.utils.Rectangle;
import engine.utils.components.draw.shader.AbstractShader;
import java.util.List;
import java.util.Map;

/** Recolors only the green/cyan lettering of Computer_1, preserving its neutral housing. */
public final class DisplayTextStatusShader extends AbstractShader {
  private boolean completed;

  /**
   * Creates the client-side display effect without changing the sprite bounds.
   *
   * @param completed whether the associated riddle is complete
   */
  public DisplayTextStatusShader(boolean completed) {
    super("shaders/passthrough.vert", "shaders/systemRecovery/display_text_status.frag");
    this.completed = completed;
  }

  /**
   * Selects red lettering when unsolved and the original green lettering when completed.
   *
   * @param completed whether the associated riddle is complete
   */
  public void completed(boolean completed) {
    this.completed = completed;
  }

  /**
   * @return whether the label currently represents a completed riddle
   */
  public boolean completed() {
    return completed;
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of(new BoolUniform("u_completed", completed));
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
  protected void writeProperties(Map<String, String> properties) {
    properties.put("completed", Boolean.toString(completed));
  }

  @Override
  protected void readProperties(Map<String, String> properties) {
    completed = booleanProperty(properties, "completed");
  }
}
