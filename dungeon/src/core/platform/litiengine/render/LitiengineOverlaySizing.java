package core.platform.litiengine.render;

/**
 * Shared tile-pixel dependent sizing helpers for LITIENGINE overlays.
 *
 * <p>This utility centralizes small overlay sizing rules that previously lived directly inside
 * editor/debug classes. The helpers operate purely on the active tile size in pixels and keep
 * minimum / clamp rules in one place.
 */
public final class LitiengineOverlaySizing {

  private LitiengineOverlaySizing() {}

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
}
