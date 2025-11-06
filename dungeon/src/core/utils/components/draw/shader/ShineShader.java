package core.utils.components.draw.shader;

import com.badlogic.gdx.graphics.Color;

import java.util.List;

public class ShineShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/shine.frag";

  private int padding = 20;

  private int sliceCount;
  private float gapSize;
  private float rotationSpeed;
  private Color shineColor;

  public ShineShader() {
    super(VERT_PATH, FRAG_PATH);
    sliceCount = 4;
    gapSize = 0.2f;
    rotationSpeed = 0.2f;
    shineColor = new Color(1, 1, 0.5f, 1f);
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
        return List.of(
          new FloatUniform("u_sliceCount", sliceCount),
          new FloatUniform("u_gapSize", gapSize),
          new FloatUniform("u_rotationSpeed", rotationSpeed),
          new ColorUniform("u_shineColor", shineColor)
        );
  }

  @Override
  public int getPadding() {
    return padding;
  }

  public ShineShader padding(int padding) {
    this.padding = padding;
    return this;
  }

  // Getters and Setters

  public int sliceCount() {
    return sliceCount;
  }

  public ShineShader sliceCount(int sliceCount) {
    this.sliceCount = sliceCount;
    return this;
  }

  public float gapSize() {
    return gapSize;
  }

  public ShineShader gapSize(float gapSize) {
    this.gapSize = gapSize;
    return this;
  }

  public float rotationSpeed() {
    return rotationSpeed;
  }

  public ShineShader rotationSpeed(float rotationSpeed) {
    this.rotationSpeed = rotationSpeed;
    return this;
  }

  public Color shineColor() {
    return shineColor;
  }

  public ShineShader shineColor(Color shineColor) {
    this.shineColor = shineColor;
    return this;
  }
}
