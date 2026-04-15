package core.game.render.overlay;

/**
 * Utility class for computing tile overlay sizes and related dimensions.
 *
 * <p>This class provides methods to scale pixel sizes, stroke widths, and insets
 * based on a tile's dimensions.
 *
 * <p>It also includes methods for calculating offsets
 * and baseline alignment for text and annotations.
 */
public final class TileOverlaySizing {

  private TileOverlaySizing() {}

  /**
   * Scales a pixel size relative to the current tile size.
   *
   * @param tilePx current tile size in pixels
   * @param factor scale factor relative to one tile
   * @param minPx minimum returned pixel size
   * @return scaled pixel size, never below {@code minPx}
   */
  public static int scaledPixels(int tilePx, float factor, int minPx) {
    int safeTilePx = Math.max(1, tilePx);
    return Math.max(minPx, Math.round(safeTilePx * factor));
  }

  /**
   * Scales a pixel size relative to the current tile size and clamps it to a maximum.
   *
   * @param tilePx current tile size in pixels
   * @param factor scale factor relative to one tile
   * @param minPx minimum returned pixel size
   * @param maxPx maximum returned pixel size
   * @return scaled and clamped pixel size
   */
  public static int scaledPixelsClamped(int tilePx, float factor, int minPx, int maxPx) {
    return Math.clamp(scaledPixels(tilePx, factor, minPx), minPx, maxPx);
  }

  /**
   * Scales a stroke width relative to the current tile size.
   *
   * @param tilePx current tile size in pixels
   * @param factor scale factor relative to one tile
   * @param minPx minimum returned stroke width
   * @return scaled stroke width
   */
  public static float scaledStroke(int tilePx, float factor, float minPx) {
    int safeTilePx = Math.max(1, tilePx);
    return Math.max(minPx, safeTilePx * factor);
  }

  /**
   * Converts a fixed pixel inset into world units for one tile-based cell.
   *
   * @param tilePx current tile size in pixels
   * @param insetPx inset in pixels
   * @return inset converted to world units
   */
  public static float worldInsetFromPixels(int tilePx, int insetPx) {
    int safeTilePx = Math.max(1, tilePx);
    return Math.max(0, insetPx) / (float) safeTilePx;
  }

  /**
   * Vertical label offset commonly used for tile annotations.
   *
   * @param tilePx current tile size in pixels
   * @return y offset in pixels
   */
  public static int tileLabelYOffset(int tilePx) {
    return scaledPixels(tilePx, 0.5f, 14);
  }

  /**
   * Baseline position for text that should sit near the bottom of a tile cell.
   *
   * @param tilePx current tile size in pixels
   * @param bottomPaddingPx padding above the tile bottom in pixels
   * @return baseline offset in pixels from the tile's top-left origin
   */
  public static int bottomAlignedLabelBaseline(int tilePx, int bottomPaddingPx) {
    int safeTilePx = Math.max(1, tilePx);
    return Math.max(0, safeTilePx - Math.max(0, bottomPaddingPx));
  }
}
