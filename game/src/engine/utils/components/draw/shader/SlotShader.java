package engine.utils.components.draw.shader;

import com.badlogic.gdx.graphics.Color;
import engine.utils.Rectangle;
import java.util.List;
import java.util.Map;

/**
 * A shader that replaces non-transparent texture pixels with a static color and adds a static
 * outline around the texture.
 */
public class SlotShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/slot.frag";
  private static final Color DEFAULT_OUTLINE_COLOR = Color.valueOf("333333");
  private static final Color DEFAULT_TEXTURE_COLOR = Color.valueOf("000000BB");

  private int width;
  private Color color;
  private Color textureColor;

  /** Creates a SlotShader with default parameters. */
  public SlotShader() {
    this(1, DEFAULT_OUTLINE_COLOR, DEFAULT_TEXTURE_COLOR);
  }

  /**
   * Creates a SlotShader with the specified width, outline color, and texture color.
   *
   * @param width the width of the outline in pixels
   * @param color the color of the outline
   * @param textureColor the color applied to non-transparent texture pixels
   */
  public SlotShader(int width, Color color, Color textureColor) {
    super(VERT_PATH, FRAG_PATH);
    this.width = width;
    this.color = color;
    this.textureColor = textureColor;
  }

  /**
   * Creates a SlotShader with the specified width and outline color.
   *
   * @param width the width of the outline in pixels
   * @param color the color of the outline
   */
  public SlotShader(int width, Color color) {
    this(width, color, new Color(DEFAULT_TEXTURE_COLOR));
  }

  /**
   * Creates a SlotShader with the specified width and a default white outline color.
   *
   * @param width the width of the outline in pixels
   */
  public SlotShader(int width) {
    this(width, Color.WHITE);
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of(
        new FloatUniform("u_width", width * ((float) actualUpscale / upscaling())),
        new ColorUniform("u_color", color),
        new ColorUniform("u_textureColor", textureColor));
  }

  @Override
  public int padding() {
    return width;
  }

  @Override
  public Rectangle worldBounds() {
    return null;
  }

  /**
   * Gets the width of the outline.
   *
   * @return the width of the outline in pixels
   */
  public int width() {
    return width;
  }

  /**
   * Sets the width of the outline.
   *
   * @param width the width of the outline in pixels
   * @return this shader for chaining
   */
  public SlotShader width(int width) {
    this.width = width;
    return this;
  }

  /**
   * Gets the color of the outline.
   *
   * @return the color of the outline
   */
  public Color color() {
    return color;
  }

  /**
   * Sets the color of the outline.
   *
   * @param color the color of the outline
   * @return this shader for chaining
   */
  public SlotShader color(Color color) {
    this.color = color;
    return this;
  }

  /**
   * Gets the color applied to non-transparent texture pixels.
   *
   * @return the texture color
   */
  public Color textureColor() {
    return textureColor;
  }

  /**
   * Sets the color applied to non-transparent texture pixels.
   *
   * @param textureColor the texture color
   * @return this shader for chaining
   */
  public SlotShader textureColor(Color textureColor) {
    this.textureColor = textureColor;
    return this;
  }

  @Override
  protected void writeProperties(Map<String, String> properties) {
    properties.put("width", Integer.toString(width));
    putColor(properties, color);
    putColor(properties, "textureColor", textureColor);
  }

  @Override
  protected void readProperties(Map<String, String> properties) {
    width = intProperty(properties, "width");
    color = colorProperty(properties);
    textureColor = colorProperty(properties, "textureColor");
  }
}
