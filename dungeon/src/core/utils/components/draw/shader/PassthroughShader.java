package core.utils.components.draw.shader;

import java.util.List;

public class PassthroughShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/passthrough.frag";

  public PassthroughShader() {
    super(VERT_PATH, FRAG_PATH);
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of();
  }

  @Override
  public int getPadding() {
    return 0;
  }
}
