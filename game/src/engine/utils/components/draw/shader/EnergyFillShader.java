package engine.utils.components.draw.shader;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import engine.utils.Rectangle;
import engine.utils.components.draw.TextureGenerator;
import java.util.List;

/** A shader that applies a fill effect over the bottom percentage of an entity's texture. */
public class EnergyFillShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/energy_fill.frag";

  private float fillPercentage;
  private float animMagnitude = 0.028f;
  private Color color;
  private Texture texture;

  /**
   * Creates an EnergyFillShader with the specified fill percentage and overlay color.
   *
   * @param fillPercentage the filled percentage, from {@code 0.0} to {@code 1.0}
   * @param color the overlay color
   */
  public EnergyFillShader(float fillPercentage, Color color) {
    this(fillPercentage, color, TextureGenerator.generateColorTexture(1, 1, Color.CLEAR));
  }

  /**
   * Creates an EnergyFillShader with the specified fill percentage and overlay texture.
   *
   * @param fillPercentage the filled percentage, from {@code 0.0} to {@code 1.0}
   * @param texture the overlay texture
   */
  public EnergyFillShader(float fillPercentage, Texture texture) {
    this(fillPercentage, Color.CLEAR, texture);
  }

  /**
   * Creates an EnergyFillShader with the specified fill percentage, overlay color, and texture.
   *
   * @param fillPercentage the filled percentage, from {@code 0.0} to {@code 1.0}
   * @param color the overlay color
   * @param texture the overlay texture
   */
  public EnergyFillShader(float fillPercentage, Color color, Texture texture) {
    super(VERT_PATH, FRAG_PATH);
    this.fillPercentage = validateFillPercentage(fillPercentage);
    this.color = color;
    this.texture = texture;
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of(
        new FloatUniform("u_fillPercentage", fillPercentage),
        new FloatUniform("u_animMagnitude", animMagnitude),
        new ColorUniform("u_color", color),
        new TextureUniform("u_overlayTexture", texture, 1));
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
   * Gets the animation magnitude.
   *
   * @return the animation magnitude
   */
  public float animMagnitude() {
    return animMagnitude;
  }

  /**
   * Sets the animation magnitude.
   *
   * @param animMagnitude the animation magnitude
   * @return this shader for chaining
   */
  public EnergyFillShader animMagnitude(float animMagnitude) {
    this.animMagnitude = animMagnitude;
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

  /**
   * Gets the overlay texture.
   *
   * @return the overlay texture
   */
  public Texture texture() {
    return texture;
  }

  /**
   * Sets the overlay texture.
   *
   * @param texture the overlay texture
   * @return this shader for chaining
   */
  public EnergyFillShader texture(Texture texture) {
    this.texture = texture;
    return this;
  }

  private static float validateFillPercentage(float fillPercentage) {
    if (fillPercentage < 0.0f || fillPercentage > 1.0f) {
      throw new IllegalArgumentException("Fill percentage must be between 0.0 and 1.0.");
    }
    return fillPercentage;
  }
}
