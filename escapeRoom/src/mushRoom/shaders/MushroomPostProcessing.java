package mushRoom.shaders;

import com.badlogic.gdx.math.Vector4;
import core.utils.Rectangle;
import core.utils.components.draw.shader.AbstractShader;

import java.util.List;

/**
 * MushroomPostProcessing shader that applies a visual effect based on the player's distance
 */
public class MushroomPostProcessing extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/mushroom_pp.frag";

  private Rectangle home;
  private float viewDistance = 0.2f;

  /** Constructs a MushroomPostProcessing shader with the specified home region. */
  public MushroomPostProcessing(Rectangle home) {
    super(VERT_PATH, FRAG_PATH);
    this.home = home;
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of(
        new Vector4Uniform("u_homeBounds", home),
        new FloatUniform("u_viewDistance", viewDistance));
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
   * Gets the view distance for the shader effect.
   *
   * @return The view distance.
   */
  public float viewDistance() {
    return viewDistance;
  }

  /**
   * Sets the view distance for the shader effect.
   *
   * @param viewDistance The new view distance.
   * @return The updated MushroomPostProcessing instance.
   */
  public MushroomPostProcessing viewDistance(float viewDistance) {
    this.viewDistance = viewDistance;
    return this;
  }

  /**
   * Gets the home region for the shader effect.
   *
   * @return The home region.
   */
  public Rectangle home() {
    return home;
  }

  /**
   * Sets the home region for the shader effect.
   *
   * @param home The new home region.
   * @return The updated MushroomPostProcessing instance.
   */
  public MushroomPostProcessing home(Rectangle home) {
    this.home = home;
    return this;
  }
}
