package core.utils.components.draw.shader;

import com.badlogic.gdx.graphics.Color;
import core.utils.Rectangle;
import java.util.List;

/**
 * A shader that adds an outline effect to rendered objects. The outline can have a specified width
 * and color (or rainbow) and can pulse its width over time.
 */
public class OutlineShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/outline.frag";

  private int width;
  private Color color;
  private float beatSpeed;
  private float beatIntensity;
  private boolean isRainbow;

  /**
   * Creates an OutlineShader with the specified parameters.
   *
   * @param width The width of the outline in pixels
   * @param color The color of the outline
   * @param beatSpeed The speed at which the outline pulses
   * @param beatIntensity The intensity of the pulsing effect
   */
  public OutlineShader(int width, Color color, float beatSpeed, float beatIntensity) {
    super(VERT_PATH, FRAG_PATH);
    this.width = width;
    this.color = color;
    this.beatSpeed = beatSpeed;
    this.beatIntensity = beatIntensity;
    this.isRainbow = false;
  }

  /**
   * Creates an OutlineShader with the specified width and color.
   *
   * @param width The width of the outline in pixels
   * @param color The color of the outline
   */
  public OutlineShader(int width, Color color) {
    this(width, color, 1.0f, 0f);
  }

  /**
   * Creates an OutlineShader with the specified width and a default white color.
   *
   * @param width The width of the outline in pixels
   */
  public OutlineShader(int width) {
    this(width, Color.WHITE);
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of(
        new FloatUniform("u_width", width * ((float) actualUpscale / upscaling())),
        new ColorUniform("u_color", color),
        new FloatUniform("u_beatSpeed", beatSpeed),
        new FloatUniform("u_beatIntensity", beatIntensity),
        new BoolUniform("u_isRainbow", isRainbow));
  }

  @Override
  public int padding() {
    return width;
  }

  @Override
  public Rectangle worldBounds() {
    return null;
  }

  // Getters and Setters

  /**
   * Gets the width of the outline.
   *
   * @return The width of the outline in pixels
   */
  public int width() {
    return width;
  }

  /**
   * Gets the color of the outline.
   *
   * @return The color of the outline
   */
  public Color color() {
    return color;
  }

  /**
   * Gets the beat speed of the outline pulsing effect.
   *
   * @return The beat speed
   */
  public float beatSpeed() {
    return beatSpeed;
  }

  /**
   * Gets the beat intensity of the outline pulsing effect.
   *
   * @return The beat intensity
   */
  public float beatIntensity() {
    return beatIntensity;
  }

  /**
   * Gets whether the outline color is rainbow.
   *
   * @return true if the outline color is rainbow, false otherwise
   */
  public boolean isRainbow() {
    return isRainbow;
  }

  /**
   * Sets the width of the outline.
   *
   * @param width The width of the outline in pixels
   * @return The OutlineShader instance for chaining
   */
  public OutlineShader width(int width) {
    this.width = width;
    return this;
  }

  /**
   * Sets the color of the outline.
   *
   * @param color The color of the outline
   * @return The OutlineShader instance for chaining
   */
  public OutlineShader color(Color color) {
    this.color = color;
    return this;
  }

  /**
   * Sets the beat speed of the outline pulsing effect.
   *
   * @param beatSpeed The beat speed
   * @return The OutlineShader instance for chaining
   */
  public OutlineShader beatSpeed(float beatSpeed) {
    this.beatSpeed = beatSpeed;
    return this;
  }

  /**
   * Sets the beat intensity of the outline pulsing effect.
   *
   * @param beatIntensity The beat intensity
   * @return The OutlineShader instance for chaining
   */
  public OutlineShader beatIntensity(float beatIntensity) {
    this.beatIntensity = beatIntensity;
    return this;
  }

  /**
   * Sets whether the outline color is rainbow.
   *
   * @param isRainbow true to set the outline color to rainbow, false otherwise
   * @return The OutlineShader instance for chaining
   */
  public OutlineShader isRainbow(boolean isRainbow) {
    this.isRainbow = isRainbow;
    return this;
  }
}
