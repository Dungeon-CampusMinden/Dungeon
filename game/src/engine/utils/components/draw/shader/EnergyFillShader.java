package engine.utils.components.draw.shader;

import com.badlogic.gdx.graphics.Color;
import engine.utils.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** A shader that applies a fill effect over the bottom percentage of an entity's texture. */
public class EnergyFillShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/energy_fill.frag";

  private float fillPercentage;
  private float animMagnitude = 0.028f;
  private Color color;
  private String texturePath;

  /** Creates an EnergyFillShader with default parameters. */
  public EnergyFillShader() {
    this(0f, Color.CLEAR, null);
  }

  /**
   * Creates an EnergyFillShader with the specified fill percentage and overlay color.
   *
   * @param fillPercentage the filled percentage, from {@code 0.0} to {@code 1.0}
   * @param color the overlay color
   */
  public EnergyFillShader(float fillPercentage, Color color) {
    this(fillPercentage, color, null);
  }

  /**
   * Creates an EnergyFillShader with the specified fill percentage and overlay texture path.
   *
   * @param fillPercentage the filled percentage, from {@code 0.0} to {@code 1.0}
   * @param texturePath the path of the overlay texture, or {@code null} for no overlay texture
   */
  public EnergyFillShader(float fillPercentage, String texturePath) {
    this(fillPercentage, Color.CLEAR, texturePath);
  }

  /**
   * Creates an EnergyFillShader with an optional path-backed overlay texture.
   *
   * @param fillPercentage the filled percentage, from {@code 0.0} to {@code 1.0}
   * @param color the overlay color
   * @param texturePath the path of the overlay texture, or {@code null} for no overlay texture
   */
  public EnergyFillShader(float fillPercentage, Color color, String texturePath) {
    super(VERT_PATH, FRAG_PATH);
    this.fillPercentage = validateFillPercentage(fillPercentage);
    this.color = color;
    if (texturePath != null && texturePath.isBlank()) {
      throw new IllegalArgumentException("Texture path must not be blank.");
    }
    this.texturePath = texturePath;
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    List<UniformBinding> uniforms =
        new ArrayList<>(
            List.of(
                new FloatUniform("u_fillPercentage", fillPercentage),
                new FloatUniform("u_animMagnitude", animMagnitude),
                new ColorUniform("u_color", color),
                new BoolUniform("u_hasOverlayTexture", texturePath != null)));
    if (texturePath != null) {
      uniforms.add(new TextureUniform("u_overlayTexture", texturePath, 1));
    }
    return uniforms;
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
   * Gets the overlay texture path.
   *
   * @return the overlay texture path, or {@code null} when no overlay is configured
   */
  public String texturePath() {
    return texturePath;
  }

  /**
   * Sets the overlay texture path.
   *
   * @param texturePath the path of the overlay texture, or {@code null} to disable the overlay
   * @return this shader for chaining
   */
  public EnergyFillShader texturePath(String texturePath) {
    if (texturePath != null && texturePath.isBlank()) {
      throw new IllegalArgumentException("Texture path must not be blank.");
    }
    this.texturePath = texturePath;
    return this;
  }

  @Override
  protected void writeProperties(Map<String, String> properties) {
    properties.put("fillPercentage", Float.toString(fillPercentage));
    putColor(properties, color);
    if (texturePath != null) {
      properties.put("texturePath", texturePath);
    }
  }

  @Override
  protected void readProperties(Map<String, String> properties) {
    fillPercentage = validateFillPercentage(floatProperty(properties, "fillPercentage"));
    color = colorProperty(properties);
    texturePath(properties.get("texturePath"));
  }

  private static float validateFillPercentage(float fillPercentage) {
    if (fillPercentage < 0.0f || fillPercentage > 1.0f) {
      throw new IllegalArgumentException("Fill percentage must be between 0.0 and 1.0.");
    }
    return fillPercentage;
  }
}
