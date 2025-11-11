package core.utils.components.draw.shader;

import java.util.List;

/**
 * A simple passthrough shader that can optionally display debug information such as pre-multiplied
 * alpha and world position.
 */
public class PassthroughShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/passthrough.frag";

  private boolean debugPMA = false;
  private boolean debugWorldPos = false;

  /** Creates a PassthroughShader instance. */
  public PassthroughShader() {
    super(VERT_PATH, FRAG_PATH);
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of(
        new BoolUniform("u_debugPMA", debugPMA), new BoolUniform("u_debugWorldPos", debugWorldPos));
  }

  @Override
  public int padding() {
    return 0;
  }

  /**
   * Checks if debug pre-multiplied alpha is enabled.
   *
   * @return true if debug PMA is enabled, false otherwise
   */
  public boolean debugPMA() {
    return debugPMA;
  }

  /**
   * Sets the debug pre-multiplied alpha flag.
   *
   * @param debugPMA true to enable debug PMA, false to disable
   * @return the PassthroughShader instance for chaining
   */
  public PassthroughShader debugPMA(boolean debugPMA) {
    this.debugPMA = debugPMA;
    return this;
  }

  /**
   * Checks if debug world position is enabled.
   *
   * @return true if debug world position is enabled, false otherwise
   */
  public boolean debugWorldPos() {
    return debugWorldPos;
  }

  /**
   * Sets the debug world position flag.
   *
   * @param debugWorldPos true to enable debug world position, false to disable
   * @return the PassthroughShader instance for chaining
   */
  public PassthroughShader debugWorldPos(boolean debugWorldPos) {
    this.debugWorldPos = debugWorldPos;
    return this;
  }
}
