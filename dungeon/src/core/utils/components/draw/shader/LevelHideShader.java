package core.utils.components.draw.shader;

import com.badlogic.gdx.math.Vector4;
import core.systems.DrawSystem;
import core.utils.Rectangle;
import java.util.List;

/** Shader for remapping hues within a specified tolerance. */
public class LevelHideShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/level_hide.frag";

  private boolean hiding;
  private float startTime = -999.0f; // By default, animation is fully at the end already
  private Rectangle region;
  private float transitionSize = 0.0f;

  /**
   * Constructs a LevelHideShader.
   *
   * @param hiding whether the shader is hiding or showing
   * @param region the region of the shader effect
   */
  public LevelHideShader(boolean hiding, Rectangle region) {
    super(VERT_PATH, FRAG_PATH);
    this.hiding = hiding;
    this.region = region;
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of(
        new BoolUniform("u_hiding", hiding),
        new FloatUniform("u_startTime", startTime),
        new Vector4Uniform(
            "u_worldRegion", new Vector4(region.x(), region.y(), region.width(), region.height())),
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
  public LevelHideShader region(Rectangle region) {
    this.region = region;
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
  public LevelHideShader transitionSize(float transitionSize) {
    this.transitionSize = transitionSize;
    return this;
  }

  /**
   * Checks if the shader is currently hiding.
   *
   * @return true if hiding, false otherwise
   */
  public boolean hiding() {
    return hiding;
  }

  /**
   * Sets whether the shader is hiding or not.
   *
   * @param hiding true to hide, false to show
   * @return The LevelHideShader instance for chaining
   */
  public LevelHideShader hiding(boolean hiding) {
    this.hiding = hiding;
    this.startTime = DrawSystem.secondsElapsed();
    return this;
  }
}
