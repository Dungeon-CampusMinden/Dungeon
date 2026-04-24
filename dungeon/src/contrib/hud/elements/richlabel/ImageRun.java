package contrib.hud.elements.richlabel;

/**
 * An inline image reference with optional shake effect and optional spritesheet sub-region.
 *
 * <p>When {@code regionW} and {@code regionH} are both positive, the run renders the rectangular
 * sub-region {@code (regionX, regionY, regionW, regionH)} of the texture at {@code path} instead of
 * the full texture. Use this to display individual cells of a spritesheet (for example the
 * input-prompt sheet used by the {@code [key]} markup tag).
 *
 * <p>{@code scale} is a multiplier on top of the font-derived image height. {@code 1.0f} (the
 * default) renders the image roughly the height of the surrounding text; values above {@code 1.0f}
 * make it proportionally larger while preserving the aspect ratio.
 *
 * @param path the asset path of the image
 * @param shake the shake effect parameters, or null for no shake
 * @param sizeOverride font size override active when the image was parsed (used to scale the image
 *     height to match surrounding text), or {@code -1} to use the default font size
 * @param scale extra size multiplier applied on top of the font-derived image height
 * @param regionX the x coordinate of the sub-region in pixels, or {@code -1} for the full texture
 * @param regionY the y coordinate of the sub-region in pixels, or {@code -1} for the full texture
 * @param regionW the width of the sub-region in pixels, or {@code -1} for the full texture
 * @param regionH the height of the sub-region in pixels, or {@code -1} for the full texture
 * @param noGapLeft if {@code true}, suppresses the additional inline-image gap on the left side
 *     (the natural word-space contributed by surrounding whitespace is unaffected). Has no effect
 *     when the image already has no left gap (e.g. it is the first run on its line).
 * @param noGapRight if {@code true}, suppresses the additional inline-image gap on the right side
 *     (the natural word-space contributed by surrounding whitespace is unaffected). Has no effect
 *     when the image already has no right gap (e.g. it is the last run).
 */
public record ImageRun(
    String path,
    ShakeEffect shake,
    int sizeOverride,
    float scale,
    int regionX,
    int regionY,
    int regionW,
    int regionH,
    boolean noGapLeft,
    boolean noGapRight)
    implements Run {

  /**
   * Convenience factory for an inline image without a sub-region and at default scale.
   *
   * @param path the asset path of the image
   * @param shake the shake effect parameters, or null for no shake
   * @param sizeOverride font size override, or {@code -1} to use the default font size
   * @return a new ImageRun referencing the entire texture at {@code path}
   */
  public static ImageRun fullTexture(String path, ShakeEffect shake, int sizeOverride) {
    return new ImageRun(path, shake, sizeOverride, 1f, -1, -1, -1, -1, false, false);
  }

  /**
   * Returns whether this run targets a sub-region rather than the full texture.
   *
   * @return true if both {@code regionW} and {@code regionH} are positive
   */
  public boolean hasSubRegion() {
    return regionW > 0 && regionH > 0;
  }
}
