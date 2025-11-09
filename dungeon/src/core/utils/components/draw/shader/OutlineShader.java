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
  private boolean isRainbow;

  public OutlineShader(int width, Color color, float beatSpeed, float beatIntensity) {
    super(VERT_PATH, FRAG_PATH);
    this.width = width;
    this.color = color;
    this.beatSpeed = beatSpeed;
    this.beatIntensity = beatIntensity;
    this.isRainbow = false;
  }

  public OutlineShader(int width, Color color) {
    this(width, color, 1.0f, 0f);
  }

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
  public int getPadding() {
    return width;
  }

  // Getters and Setters

  public int width() {
    return width;
  }

  public Color color() {
    return color;
  }

  public float beatSpeed() {
    return beatSpeed;
  }

  public float beatIntensity() {
    return beatIntensity;
  }

  public boolean isRainbow() {
    return isRainbow;
  }

  public OutlineShader width(int width) {
    this.width = width;
    return this;
  }

  public OutlineShader color(Color color) {
    this.color = color;
    return this;
  }

  public OutlineShader beatSpeed(float beatSpeed) {
    this.beatSpeed = beatSpeed;
    return this;
  }

  public OutlineShader beatIntensity(float beatIntensity) {
    this.beatIntensity = beatIntensity;
    return this;
  }

  public OutlineShader isRainbow(boolean isRainbow) {
    this.isRainbow = isRainbow;
    return this;
  }
}
