package rooms.systemRecovery.modules.display;

import engine.utils.Rectangle;
import engine.utils.components.draw.shader.AbstractShader;
import java.util.List;

/** Recolors only the green/cyan lettering of Computer_1, preserving its neutral housing. */
public final class DisplayTextStatusShader extends AbstractShader {
  private boolean completed;

  /** Creates the client-side display effect without changing the sprite bounds. */
  public DisplayTextStatusShader(boolean completed) {
    super("shaders/passthrough.vert", "shaders/systemRecovery/display_text_status.frag");
    this.completed = completed;
  }

  /** Selects red lettering when unsolved and the original green lettering when completed. */
  public void completed(boolean completed) {
    this.completed = completed;
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
}
