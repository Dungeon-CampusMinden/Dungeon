package rooms.mushroom.shaders;

import engine.utils.Rectangle;
import engine.utils.components.draw.shader.AbstractShader;
import java.util.List;

/** MushroomPostProcessing shader that applies a visual effect based on the player's distance. */
public class MagicLensPpShader extends AbstractShader {

  private static final String VERT_PATH = "shaders/passthrough.vert";
  private static final String FRAG_PATH = "shaders/mushroom_magic_lens_pp.frag";
  private static final String MAGNIFYING_GLASS_TEXTURE = "images/magnifying_glass.png";

  private float lensRadius = 0.1f;

  /** Constructs a MushroomPostProcessing shader with the specified home region. */
  public MagicLensPpShader() {
    super(VERT_PATH, FRAG_PATH);
  }

  @Override
  protected List<UniformBinding> getUniforms(int actualUpscale) {
    return List.of(
        new FloatUniform("u_lensRadius", lensRadius),
        new TextureUniform("u_magnifyingGlassTex", MAGNIFYING_GLASS_TEXTURE, 1),
        new TextureSizeUniform("u_magnifyingGlassTexSize", MAGNIFYING_GLASS_TEXTURE));
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
  public MagicLensPpShader lensRadius(float radius) {
    this.lensRadius = radius;
    return this;
  }
}
