package core.utils.components.draw.shader;

import com.badlogic.gdx.math.Vector4;
import core.utils.Rectangle;
import java.util.List;

/** Shader for remapping hues within a specified tolerance. */
public class ColorGradeShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/color_grade.frag";

  private Rectangle region;
  private float hue = -1.0f;
  private float saturationMultiplier = 1.0f;
  private float valueMultiplier = 1.0f;
  private float transitionSize = 0.0f;

  /** Constructs a ColorGradeShader. */
  public ColorGradeShader() {
    super(VERT_PATH, FRAG_PATH);
  }

  /**
   * Constructs a ColorGradeShader with specified parameters.
   *
   * @param hue The target hue to remap to
   * @param saturationMultiplier The multiplier for saturation
   * @param valueMultiplier The multiplier for value (brightness)
   */
  public ColorGradeShader(float hue, float saturationMultiplier, float valueMultiplier) {
    super(VERT_PATH, FRAG_PATH);
    this.hue = hue;
    this.saturationMultiplier = saturationMultiplier;
    this.valueMultiplier = valueMultiplier;
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of(
        new Vector4Uniform(
            "u_worldRegion", new Vector4(region.x(), region.y(), region.width(), region.height())),
        new FloatUniform("u_hue", hue),
        new FloatUniform("u_saturationMult", saturationMultiplier),
        new FloatUniform("u_valueMult", valueMultiplier),
        new FloatUniform("u_transitionSize", transitionSize));
  }

  @Override
  public int padding() {
    return 0;
  }

  @Override
  public Rectangle worldBounds() {
    if (region == null) return null;
    return region.expand(transitionSize);
  }

  /**
   * Gets the region of the shader effect.
   *
   * @return The region as a Rectangle
   */
  public Rectangle region() {
    return region;
  }

  /**
   * Sets the region of the shader effect.
   *
   * @param region The region as a Rectangle
   * @return The ColorGradeShader instance for chaining
   */
  public ColorGradeShader region(Rectangle region) {
    this.region = region;
    return this;
  }

  /**
   * Gets the target hue for remapping.
   *
   * @return The target hue
   */
  public float hue() {
    return hue;
  }

  /**
   * Sets the target hue for remapping.
   *
   * @param hue The target hue
   * @return The ColorGradeShader instance for chaining
   */
  public ColorGradeShader hue(float hue) {
    this.hue = hue;
    return this;
  }

  /**
   * Gets the saturation multiplier.
   *
   * @return The saturation multiplier
   */
  public float saturationMultiplier() {
    return saturationMultiplier;
  }

  /**
   * Sets the saturation multiplier.
   *
   * @param saturationMultiplier The saturation multiplier
   * @return The ColorGradeShader instance for chaining
   */
  public ColorGradeShader saturationMultiplier(float saturationMultiplier) {
    this.saturationMultiplier = saturationMultiplier;
    return this;
  }

  /**
   * Gets the value (brightness) multiplier.
   *
   * @return The value multiplier
   */
  public float valueMultiplier() {
    return valueMultiplier;
  }

  /**
   * Sets the value (brightness) multiplier.
   *
   * @param valueMultiplier The value multiplier
   * @return The ColorGradeShader instance for chaining
   */
  public ColorGradeShader valueMultiplier(float valueMultiplier) {
    this.valueMultiplier = valueMultiplier;
    return this;
  }

  /**
   * Gets the transition size for hue remapping.
   *
   * @return The transition size
   */
  public float transitionSize() {
    return transitionSize;
  }

  /**
   * Sets the transition size for hue remapping.
   *
   * @param transitionSize The transition size
   * @return The ColorGradeShader instance for chaining
   */
  public ColorGradeShader transitionSize(float transitionSize) {
    this.transitionSize = transitionSize;
    return this;
  }
}
