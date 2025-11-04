package core.utils.components.draw.shader;

import com.badlogic.gdx.graphics.Color;
import java.util.List;

public class OutlineShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/outline.frag";

  private int width;
  private Color color;
  private float beatSpeed;
  private float beatIntensity;

  public OutlineShader(int width, Color color, float beatSpeed, float beatIntensity) {
    super(VERT_PATH, FRAG_PATH);
    this.width = width;
    this.color = color;
    this.beatSpeed = beatSpeed;
    this.beatIntensity = beatIntensity;
  }

  public OutlineShader(int width, Color color) {
    this(width, color, 0f, 0f);
  }

  @Override
  protected List<UniformBinding> getUniforms(float deltaTime) {
    return List.of(
        new FloatUniform("u_width", width),
        new ColorUniform("u_color", color),
        new FloatUniform("u_beatSpeed", beatSpeed),
        new FloatUniform("u_beatIntensity", beatIntensity));
  }

  @Override
  public float getPadding() {
    return width;
  }
}
