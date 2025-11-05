package core.utils.components.draw.shader;

import java.util.List;

public class HueRemapShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/hue_remap.frag";

  private float startingHue;
  private float targetHue;
  private float tolerance;

  public HueRemapShader(float startingHue, float targetHue, float tolerance) {
    super(VERT_PATH, FRAG_PATH);
    this.startingHue = startingHue;
    this.targetHue = targetHue;
    this.tolerance = tolerance;
  }

  public HueRemapShader(float startingHue, float targetHue) {
    this(startingHue, targetHue, 0.05f);
  }

  @Override
  protected List<UniformBinding> getUniforms() {
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

  public float startingHue() {
    return startingHue;
  }

  public HueRemapShader setStartingHue(float startingHue) {
    this.startingHue = startingHue;
    return this;
  }

  public float targetHue() {
    return targetHue;
  }

  public HueRemapShader setTargetHue(float targetHue) {
    this.targetHue = targetHue;
    return this;
  }

  public float tolerance() {
    return tolerance;
  }

  public HueRemapShader setTolerance(float tolerance) {
    this.tolerance = tolerance;
    return this;
  }
}
