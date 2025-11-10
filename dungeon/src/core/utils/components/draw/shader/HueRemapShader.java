package core.utils.components.draw.shader;

import java.util.List;

/** Shader for remapping hues within a specified tolerance. */
public class HueRemapShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/hue_remap.frag";

  private float startingHue;
  private float targetHue;
  private float tolerance;

  /**
   * Constructs a HueRemapShader with specified starting hue, target hue, and tolerance.
   *
   * @param startingHue The hue to be remapped
   * @param targetHue The hue to map to
   * @param tolerance The tolerance for hue matching
   */
  public HueRemapShader(float startingHue, float targetHue, float tolerance) {
    super(VERT_PATH, FRAG_PATH);
    this.startingHue = startingHue;
    this.targetHue = targetHue;
    this.tolerance = tolerance;
  }

  /**
   * Constructs a HueRemapShader with specified starting hue and target hue, using default
   * tolerance.
   *
   * @param startingHue The hue to be remapped
   * @param targetHue The hue to map to
   */
  public HueRemapShader(float startingHue, float targetHue) {
    this(startingHue, targetHue, 0.05f);
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of(
        new FloatUniform("u_startingHue", startingHue),
        new FloatUniform("u_targetHue", targetHue),
        new FloatUniform("u_tolerance", tolerance));
  }

  @Override
  public int getPadding() {
    return 0;
  }

  // Getters and Setters

  /**
   * Gets the starting hue.
   *
   * @return The starting hue
   */
  public float startingHue() {
    return startingHue;
  }

  /**
   * Sets the starting hue.
   *
   * @param startingHue The starting hue to set
   * @return The HueRemapShader instance for chaining
   */
  public HueRemapShader setStartingHue(float startingHue) {
    this.startingHue = startingHue;
    return this;
  }

  /**
   * Gets the target hue.
   *
   * @return The target hue
   */
  public float targetHue() {
    return targetHue;
  }

  /**
   * Sets the target hue.
   *
   * @param targetHue The target hue to set
   * @return The HueRemapShader instance for chaining
   */
  public HueRemapShader setTargetHue(float targetHue) {
    this.targetHue = targetHue;
    return this;
  }

  /**
   * Gets the tolerance.
   *
   * @return The tolerance
   */
  public float tolerance() {
    return tolerance;
  }

  /**
   * Sets the tolerance.
   *
   * @param tolerance The tolerance to set
   * @return The HueRemapShader instance for chaining
   */
  public HueRemapShader setTolerance(float tolerance) {
    this.tolerance = tolerance;
    return this;
  }
}
