package core.utils.components.draw.shader;

import java.util.List;

public class PassthroughShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/passthrough.frag";

  private boolean debugPMA = false;
  private boolean debugWorldPos = false;

  public PassthroughShader() {
    super(VERT_PATH, FRAG_PATH);
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of(
        new BoolUniform("u_debugPMA", debugPMA), new BoolUniform("u_debugWorldPos", debugWorldPos));
  }

  @Override
  public int getPadding() {
    return 0;
  }

  public boolean debugPMA() {
    return debugPMA;
  }

  public PassthroughShader debugPMA(boolean debugPMA) {
    this.debugPMA = debugPMA;
    return this;
  }

  public boolean debugWorldPos() {
    return debugWorldPos;
  }

  public PassthroughShader debugWorldPos(boolean debugWorldPos) {
    this.debugWorldPos = debugWorldPos;
    return this;
  }
}
