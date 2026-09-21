package engine.utils.components.draw.shader;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import engine.utils.Rectangle;
import java.util.List;
import java.util.Map;

/** Projects the rendered scene onto a glowing sphere over a configurable background texture. */
public class MagicBallShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/magic_ball.frag";
  private static final String DEFAULT_BG_TEXTURE = "animation/missing_texture.png";
  private static final Rectangle FULL_TEXTURE_REGION = new Rectangle(1.0f, 1.0f, 0.0f, 0.0f);

  private String bgTexture;
  private float ballSize;
  private float curvedEdgeWidth;
  private Vector2 ballOffset;
  private Rectangle textureRegion;
  private Color ballColor;
  private float glowStrength;
  private Color glowColor;

  /** Creates a MagicBallShader with defaults suitable for deserialization. */
  public MagicBallShader() {
    this(DEFAULT_BG_TEXTURE, 0.8f, new Vector2(), Color.WHITE, 1.0f, Color.WHITE);
  }

  /**
   * Creates a MagicBallShader.
   *
   * @param bgTexture texture shown behind the ball and glow
   * @param ballSize diameter of the ball as a fraction of the shorter screen dimension
   * @param ballOffset screen-relative offset from the center
   * @param ballColor color used for transparent areas on the ball
   * @param glowStrength multiplier applied to the default glow
   * @param glowColor color of the animated edge glow
   */
  public MagicBallShader(
      String bgTexture,
      float ballSize,
      Vector2 ballOffset,
      Color ballColor,
      float glowStrength,
      Color glowColor) {
    this(bgTexture, ballSize, ballOffset, ballColor, glowStrength, glowColor, null);
  }

  /**
   * Creates a MagicBallShader with a configurable projection region.
   *
   * @param bgTexture texture shown behind the ball and glow
   * @param ballSize diameter of the ball as a fraction of the shorter screen dimension
   * @param ballOffset screen-relative offset from the center
   * @param ballColor color used for transparent areas on the ball
   * @param glowStrength multiplier applied to the default glow
   * @param glowColor color of the animated edge glow
   * @param textureRegion normalized region of the rendered texture to project, or null for the
   *     whole texture
   */
  public MagicBallShader(
      String bgTexture,
      float ballSize,
      Vector2 ballOffset,
      Color ballColor,
      float glowStrength,
      Color glowColor,
      Rectangle textureRegion) {
    super(VERT_PATH, FRAG_PATH);
    this.bgTexture = validateTexturePath(bgTexture);
    this.ballSize = validateBallSize(ballSize);
    this.ballOffset = new Vector2(validateBallOffset(ballOffset));
    this.textureRegion = textureRegion;
    this.ballColor = validateColor(ballColor, "Ball");
    this.glowStrength = validateGlowStrength(glowStrength);
    this.glowColor = validateColor(glowColor, "Glow");
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of(
        new TextureUniform("u_bgTexture", bgTexture, 1),
        new FloatUniform("u_ballSize", ballSize),
        new FloatUniform("u_curvedEdgeWidth", curvedEdgeWidth * actualUpscale),
        new Vector2Uniform("u_ballOffset", ballOffset),
        new Vector4Uniform(
            "u_textureRegion", textureRegion == null ? FULL_TEXTURE_REGION : textureRegion),
        new ColorUniform("u_ballColor", ballColor),
        new FloatUniform("u_glowStrength", glowStrength),
        new ColorUniform("u_glowColor", glowColor));
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
   * Returns the background texture path.
   *
   * @return the background texture path
   */
  public String bgTexture() {
    return bgTexture;
  }

  /**
   * Sets the background texture path.
   *
   * @param bgTexture the background texture path
   * @return this shader for chaining
   */
  public MagicBallShader bgTexture(String bgTexture) {
    this.bgTexture = validateTexturePath(bgTexture);
    return this;
  }

  /**
   * Returns the ball diameter as a fraction of the shorter screen dimension.
   *
   * @return the ball diameter
   */
  public float ballSize() {
    return ballSize;
  }

  /**
   * Sets the ball diameter as a fraction of the shorter screen dimension.
   *
   * @param ballSize the ball diameter
   * @return this shader for chaining
   */
  public MagicBallShader ballSize(float ballSize) {
    this.ballSize = validateBallSize(ballSize);
    return this;
  }

  /**
   * Returns the width of the curved rim around the flat scene view.
   *
   * @return rim width in pixels, or 0 to project the entire scene onto the sphere
   */
  public float curvedEdgeWidth() {
    return curvedEdgeWidth;
  }

  /**
   * Keeps the scene flat except for a smooth blend into the sphere at its rim.
   *
   * @param pixels rim width in pixels, or 0 to use the full spherical projection
   * @return this shader for chaining
   */
  public MagicBallShader curvedEdgeWidth(float pixels) {
    if (!Float.isFinite(pixels) || pixels < 0)
      throw new IllegalArgumentException("Curved edge width must be finite and non-negative.");
    curvedEdgeWidth = pixels;
    return this;
  }

  /**
   * Returns a copy of the screen-relative ball offset.
   *
   * @return the ball offset
   */
  public Vector2 ballOffset() {
    return new Vector2(ballOffset);
  }

  /**
   * Sets the screen-relative ball offset.
   *
   * @param ballOffset the ball offset
   * @return this shader for chaining
   */
  public MagicBallShader ballOffset(Vector2 ballOffset) {
    this.ballOffset = new Vector2(validateBallOffset(ballOffset));
    return this;
  }

  /**
   * Returns the normalized region of the rendered texture projected onto the ball.
   *
   * @return the texture region, or null when the whole texture is projected
   */
  public Rectangle textureRegion() {
    return textureRegion;
  }

  /**
   * Sets the normalized region of the rendered texture projected onto the ball.
   *
   * @param textureRegion the texture region, or null to project the whole texture
   * @return this shader for chaining
   */
  public MagicBallShader textureRegion(Rectangle textureRegion) {
    this.textureRegion = textureRegion;
    return this;
  }

  /**
   * Returns the color used for transparent areas on the ball.
   *
   * @return the ball color
   */
  public Color ballColor() {
    return ballColor;
  }

  /**
   * Sets the color used for transparent areas on the ball.
   *
   * @param ballColor the ball color
   * @return this shader for chaining
   */
  public MagicBallShader ballColor(Color ballColor) {
    this.ballColor = validateColor(ballColor, "Ball");
    return this;
  }

  /**
   * Returns the glow strength multiplier.
   *
   * @return the glow strength multiplier
   */
  public float glowStrength() {
    return glowStrength;
  }

  /**
   * Sets the glow strength multiplier.
   *
   * @param glowStrength the glow strength multiplier
   * @return this shader for chaining
   */
  public MagicBallShader glowStrength(float glowStrength) {
    this.glowStrength = validateGlowStrength(glowStrength);
    return this;
  }

  /**
   * Returns the glow color.
   *
   * @return the glow color
   */
  public Color glowColor() {
    return glowColor;
  }

  /**
   * Sets the glow color.
   *
   * @param glowColor the glow color
   * @return this shader for chaining
   */
  public MagicBallShader glowColor(Color glowColor) {
    this.glowColor = validateColor(glowColor, "Glow");
    return this;
  }

  @Override
  protected void writeProperties(Map<String, String> properties) {
    properties.put("bgTexture", bgTexture);
    properties.put("ballSize", Float.toString(ballSize));
    properties.put("curvedEdgeWidth", Float.toString(curvedEdgeWidth));
    properties.put("ballOffsetX", Float.toString(ballOffset.x));
    properties.put("ballOffsetY", Float.toString(ballOffset.y));
    properties.put("glowStrength", Float.toString(glowStrength));
    if (textureRegion != null) {
      putRectangle(properties, textureRegion);
    }
    putColor(properties, "ballColor", ballColor);
    putColor(properties, "glowColor", glowColor);
  }

  @Override
  protected void readProperties(Map<String, String> properties) {
    bgTexture(property(properties, "bgTexture"));
    ballSize(floatProperty(properties, "ballSize"));
    curvedEdgeWidth(floatProperty(properties, "curvedEdgeWidth"));
    ballOffset(
        new Vector2(
            floatProperty(properties, "ballOffsetX"), floatProperty(properties, "ballOffsetY")));
    textureRegion(properties.containsKey("width") ? rectangleProperty(properties) : null);
    ballColor(colorProperty(properties, "ballColor"));
    glowStrength(floatProperty(properties, "glowStrength"));
    glowColor(colorProperty(properties, "glowColor"));
  }

  private static String validateTexturePath(String texturePath) {
    if (texturePath == null || texturePath.isBlank()) {
      throw new IllegalArgumentException("Background texture path must not be blank.");
    }
    return texturePath;
  }

  private static float validateBallSize(float ballSize) {
    if (ballSize <= 0.0f || ballSize > 1.0f) {
      throw new IllegalArgumentException("Ball size must be greater than 0.0 and at most 1.0.");
    }
    return ballSize;
  }

  private static Vector2 validateBallOffset(Vector2 ballOffset) {
    if (ballOffset == null) {
      throw new IllegalArgumentException("Ball offset must not be null.");
    }
    return ballOffset;
  }

  private static float validateGlowStrength(float glowStrength) {
    if (glowStrength < 0.0f) {
      throw new IllegalArgumentException("Glow strength must not be negative.");
    }
    return glowStrength;
  }

  private static Color validateColor(Color color, String name) {
    if (color == null) {
      throw new IllegalArgumentException(name + " color must not be null.");
    }
    return color;
  }
}
