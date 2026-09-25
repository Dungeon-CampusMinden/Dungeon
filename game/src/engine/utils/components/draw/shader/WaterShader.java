package engine.utils.components.draw.shader;

import com.badlogic.gdx.graphics.Color;
import engine.utils.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * World-aligned, animated water applied to a depth layer.
 *
 * <p>Where the water is and how far each point is from the shore comes from a shore field texture:
 * a {@link #PIXELS_PER_TILE} texture covering {@link #region(Rectangle)} whose red channel stores
 * the distance to the nearest non-water pixel divided by {@link #SHORE_FIELD_MAX_PIXELS}. Zero
 * marks land.
 */
public final class WaterShader extends AbstractShader {

  /** Resolution of the water pixel grid and the shore field in pixels per world tile. */
  public static final int PIXELS_PER_TILE = 16;

  /** Shore distance in pixels that maps to the maximum shore field channel value. */
  public static final float SHORE_FIELD_MAX_PIXELS = 63.75f;

  private static final String DUDV_TEXTURE = "images/dudv-water.png";
  private static final int MAX_FOAM_WIDTH = 32;

  private Rectangle region = new Rectangle(0, 0, 0, 0);
  private String shoreField;
  private Color color = new Color(0.1f, 0.4f, 0.7f, 1f);
  private float speed = 0.15f;
  private float repeat = 1f;
  private int foamMinWidth = 1;
  private int foamMaxWidth = 3;
  private float lineInterval = 2.5f;

  /** Creates a water shader with default color and speed. */
  public WaterShader() {
    super("shaders/passthrough.vert", "shaders/water.frag");
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of(
        new ColorUniform("u_waterColor", color),
        new FloatUniform("u_speed", speed),
        new FloatUniform("u_repeat", repeat),
        new FloatUniform("u_foamMinWidth", foamMinWidth),
        new FloatUniform("u_foamMaxWidth", foamMaxWidth),
        new FloatUniform("u_lineInterval", lineInterval),
        new FloatUniform("u_pixelsPerTile", PIXELS_PER_TILE),
        new Vector4Uniform("u_fieldRegion", region),
        new TextureUniform("u_dudv", DUDV_TEXTURE, 1),
        new TextureUniform("u_shoreField", shoreField, 2));
  }

  @Override
  public int padding() {
    return 0;
  }

  @Override
  public Rectangle worldBounds() {
    return region;
  }

  /**
   * Sets the world region covered by the shore field texture.
   *
   * @param region shore field bounds
   * @return this shader
   */
  public WaterShader region(Rectangle region) {
    this.region = Objects.requireNonNull(region, "region");
    return this;
  }

  /**
   * Sets the shore field texture describing where the water is and how far it is from the shore.
   *
   * @param texturePath path of the shore field in the texture map
   * @return this shader
   */
  public WaterShader shoreField(String texturePath) {
    if (texturePath == null || texturePath.isBlank()) {
      throw new IllegalArgumentException("Water shore field path must not be blank");
    }
    this.shoreField = texturePath;
    return this;
  }

  /**
   * Sets the base water color.
   *
   * @param color water color
   * @return this shader
   */
  public WaterShader color(Color color) {
    this.color = new Color(Objects.requireNonNull(color, "color"));
    return this;
  }

  /**
   * Sets the speed of the travelling waves.
   *
   * @param speed wave speed
   * @return this shader
   */
  public WaterShader speed(float speed) {
    if (!Float.isFinite(speed)) throw new IllegalArgumentException("Water speed must be finite");
    this.speed = speed;
    return this;
  }

  /**
   * Sets the scale of the wave pattern per world tile. Higher values produce smaller waves.
   *
   * @param repeat pattern scale per tile
   * @return this shader
   */
  public WaterShader repeat(float repeat) {
    if (!Float.isFinite(repeat) || repeat < 0.1f || repeat > 64f) {
      throw new IllegalArgumentException("Water repeat must be between 0.1 and 64");
    }
    this.repeat = repeat;
    return this;
  }

  /**
   * Sets the minimum width of the foam rim along the shore.
   *
   * @param pixels minimum width in pixels
   * @return this shader
   */
  public WaterShader foamMinWidth(int pixels) {
    if (pixels < 0 || pixels > MAX_FOAM_WIDTH) {
      throw new IllegalArgumentException(
          "Water foam min width must be between 0 and " + MAX_FOAM_WIDTH);
    }
    this.foamMinWidth = pixels;
    return this;
  }

  /**
   * Sets the maximum width the foam rim randomly grows to. Values below the minimum width are
   * treated as the minimum width.
   *
   * @param pixels maximum width in pixels
   * @return this shader
   */
  public WaterShader foamMaxWidth(int pixels) {
    if (pixels < 0 || pixels > MAX_FOAM_WIDTH) {
      throw new IllegalArgumentException(
          "Water foam max width must be between 0 and " + MAX_FOAM_WIDTH);
    }
    this.foamMaxWidth = pixels;
    return this;
  }

  /**
   * Sets the time between foam lines leaving the shore. Higher values make calmer water.
   *
   * @param seconds interval in seconds
   * @return this shader
   */
  public WaterShader lineInterval(float seconds) {
    if (!Float.isFinite(seconds) || seconds < 0.1f || seconds > 60f) {
      throw new IllegalArgumentException("Water line interval must be between 0.1 and 60");
    }
    this.lineInterval = seconds;
    return this;
  }

  @Override
  protected void writeProperties(Map<String, String> properties) {
    putRectangle(properties, region);
    properties.put("shoreField", shoreField);
    putColor(properties, color);
    properties.put("speed", Float.toString(speed));
    properties.put("repeat", Float.toString(repeat));
    properties.put("foamMinWidth", Integer.toString(foamMinWidth));
    properties.put("foamMaxWidth", Integer.toString(foamMaxWidth));
    properties.put("lineInterval", Float.toString(lineInterval));
  }

  @Override
  protected void readProperties(Map<String, String> properties) {
    region(rectangleProperty(properties));
    shoreField(properties.get("shoreField"));
    color(colorProperty(properties));
    speed(floatProperty(properties, "speed"));
    repeat(floatProperty(properties, "repeat"));
    foamMinWidth(intProperty(properties, "foamMinWidth"));
    foamMaxWidth(intProperty(properties, "foamMaxWidth"));
    lineInterval(floatProperty(properties, "lineInterval"));
  }
}
