package core.utils.components.draw.shader;

import com.badlogic.gdx.graphics.Color;
import java.util.List;

/**
 * A shader that creates a shining effect with rotating slices of light. The effect parameters such
 * as slice count, gap size, rotation speed, and shine color can be customized.
 */
public class ShineShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/shine.frag";

  private int padding = 20;

  private int sliceCount;
  private float gapSize;
  private float rotationSpeed;
  private Color shineColor;

  /** Creates a ShineShader instance with default parameters. */
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
        new ColorUniform("u_shineColor", shineColor));
  }

  @Override
  public int getPadding() {
    return padding;
  }

  /**
   * Sets the padding for the shader.
   *
   * @param padding the padding value
   * @return the ShineShader instance for chaining
   */
  public ShineShader padding(int padding) {
    this.padding = padding;
    return this;
  }

  // Getters and Setters

  /**
   * Gets the number of slices in the shine effect.
   *
   * @return the slice count
   */
  public int sliceCount() {
    return sliceCount;
  }

  /**
   * Sets the number of slices in the shine effect.
   *
   * @param sliceCount the slice count
   * @return the ShineShader instance for chaining
   */
  public ShineShader sliceCount(int sliceCount) {
    this.sliceCount = sliceCount;
    return this;
  }

  /**
   * Gets the gap size between slices in the shine effect.
   *
   * @return the gap size
   */
  public float gapSize() {
    return gapSize;
  }

  /**
   * Sets the gap size between slices in the shine effect.
   *
   * @param gapSize the gap size
   * @return the ShineShader instance for chaining
   */
  public ShineShader gapSize(float gapSize) {
    this.gapSize = gapSize;
    return this;
  }

  /**
   * Gets the rotation speed of the shine effect.
   *
   * @return the rotation speed
   */
  public float rotationSpeed() {
    return rotationSpeed;
  }

  /**
   * Sets the rotation speed of the shine effect.
   *
   * @param rotationSpeed the rotation speed
   * @return the ShineShader instance for chaining
   */
  public ShineShader rotationSpeed(float rotationSpeed) {
    this.rotationSpeed = rotationSpeed;
    return this;
  }

  /**
   * Gets the color of the shine effect.
   *
   * @return the shine color
   */
  public Color shineColor() {
    return shineColor;
  }

  /**
   * Sets the color of the shine effect.
   *
   * @param shineColor the shine color
   * @return the ShineShader instance for chaining
   */
  public ShineShader shineColor(Color shineColor) {
    this.shineColor = shineColor;
    return this;
  }
}
