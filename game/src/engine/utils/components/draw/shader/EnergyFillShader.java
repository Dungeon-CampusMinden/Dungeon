package engine.utils.components.draw.shader;

import com.badlogic.gdx.graphics.Color;
import engine.utils.Rectangle;
import java.util.List;

/** A shader that overlays a color over the bottom percentage of an entity's texture. */
public class EnergyFillShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/energy_fill.frag";

  private float fillPercentage;
  private Color color;

  /** Creates an EnergyFillShader with no fill and a bright red overlay. */
  public EnergyFillShader() {
    this(0.0f, Color.RED);
  }

  /**
   * Creates an EnergyFillShader with the specified fill percentage and overlay color.
   *
   * @param fillPercentage the filled percentage, from {@code 0.0} to {@code 1.0}
   * @param color the overlay color
   */
  public EnergyFillShader(float fillPercentage, Color color) {
    super(VERT_PATH, FRAG_PATH);
    this.fillPercentage = validateFillPercentage(fillPercentage);
    this.color = color;
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of(
        new FloatUniform("u_fillPercentage", fillPercentage), new ColorUniform("u_color", color));
  }

  @Override
  public int padding() {
    return 0;
  }

  @Override
  public Rectangle worldBounds() {
    return null;
  }

  /**
   * Gets the fill percentage.
   *
   * @return the fill percentage, from {@code 0.0} to {@code 1.0}
   */
  public float fillPercentage() {
    return fillPercentage;
  }

  /**
   * Sets the fill percentage.
   *
   * @param fillPercentage the filled percentage, from {@code 0.0} to {@code 1.0}
   * @return this shader for chaining
   */
  public EnergyFillShader fillPercentage(float fillPercentage) {
    this.fillPercentage = validateFillPercentage(fillPercentage);
    return this;
  }

  /**
   * Gets the overlay color.
   *
   * @return the overlay color
   */
  public Color color() {
    return color;
  }

  /**
   * Sets the overlay color.
   *
   * @param color the overlay color
   * @return this shader for chaining
   */
  public EnergyFillShader color(Color color) {
    this.color = color;
    return this;
  }

  private static float validateFillPercentage(float fillPercentage) {
    if (fillPercentage < 0.0f || fillPercentage > 1.0f) {
      throw new IllegalArgumentException("Fill percentage must be between 0.0 and 1.0.");
    }
    return fillPercentage;
  }
}
