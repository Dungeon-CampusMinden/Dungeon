package engine.rendering;

/**
 * How the {@link AtmosphereRenderer} smooths the edges of the scene. The scene is pixel art drawn
 * at a scale that is rarely a whole number of screen pixels per art pixel, so without smoothing art
 * pixels differ in width and rotated sprites show steps.
 */
public enum Antialiasing {
  /** Every screen pixel shows the art pixel at its centre. */
  OFF("Off", 1),
  /**
   * Renders the scene with a whole number of texels per art pixel, so all art pixels are equally
   * wide, and scales that to the screen by the area each texel covers of a pixel. Costs little and
   * only softens the borders between art pixels.
   */
  PIXEL_ART("Pixel-Art", 1),
  /** Blurs along the edges found in the finished image; cheap, softens the pixel art. */
  FXAA("FXAA", 1),
  /**
   * Shifts the view by a part of a pixel every frame and averages the frames; costs little, but
   * what moves or flickers trails a little.
   */
  TAA("TAA", 1),
  /** Renders the scene at twice the width and height and averages 2 x 2 texels per pixel. */
  SSAA_4X("SSAA 4x", 2),
  /** Renders the scene at four times the width and height and averages 4 x 4 texels per pixel. */
  SSAA_16X("SSAA 16x", 4);

  private final String label;
  private final int scale;

  Antialiasing(String label, int scale) {
    this.label = label;
    this.scale = scale;
  }

  /**
   * The name shown in the settings.
   *
   * @return the name
   */
  public String label() {
    return label;
  }

  /**
   * How many texels wide and high the scene buffers are per screen pixel.
   *
   * @return the scale
   */
  public int scale() {
    return scale;
  }
}
