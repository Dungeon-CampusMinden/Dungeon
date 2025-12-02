package mushRoom.shaders;

import com.badlogic.gdx.graphics.Texture;
import core.utils.Rectangle;
import core.utils.components.draw.TextureMap;
import core.utils.components.draw.shader.AbstractShader;
import core.utils.components.path.SimpleIPath;

import java.util.List;

/** MushroomPostProcessing shader that applies a visual effect based on the player's distance */
public class MagicLensLayerShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/mushroom_magic_lens_layer.frag";

  private float lensRadius = 0.1f;
  private boolean active = false;

  /** Constructs a MushroomPostProcessing shader with the specified home region. */
  public MagicLensLayerShader() {
    super(VERT_PATH, FRAG_PATH);
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    Texture mag = TextureMap.instance().textureAt(new SimpleIPath("images/magnifying_glass.png"));
    return List.of(
      new FloatUniform("u_lensRadius", lensRadius), new BoolUniform("u_active", active),
      new TextureUniform("u_magnifyingGlassTex", mag, 1),
      new Vector2Uniform("u_magnifyingGlassTexSize", mag.getWidth(), mag.getHeight())
    );
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
   * Gets the radius of the magic lens effect.
   *
   * @return the lens radius
   */
  public float lensRadius() {
    return lensRadius;
  }

  /**
   * Sets the radius of the magic lens effect.
   *
   * @param radius the radius to set
   * @return the current MagicLensLayer instance
   */
  public MagicLensLayerShader lensRadius(float radius) {
    this.lensRadius = radius;
    return this;
  }

  /**
   * Checks if the magic lens effect is enabled.
   *
   * @return true if enabled, false otherwise
   */
  public boolean active() {
    return active;
  }

  /**
   * Enables or disables the magic lens effect.
   *
   * @param active true to enable, false to disable
   * @return the current MagicLensLayer instance
   */
  public MagicLensLayerShader active(boolean active) {
    this.active = active;
    return this;
  }
}
