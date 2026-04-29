package contrib.editor.level.mode;

import core.utils.Point;

/**
 * Enumeration of different snap modes for the level editor.
 *
 * <p>EditorSnapMode defines how cursor positions and placements are snapped to a grid or snap
 * pattern. Different modes provide varying levels of precision and flexibility for positioning game
 * elements.
 *
 * <p>Available modes:
 *
 * <ul>
 *   <li>OnGrid - Snap to full tile grid
 *   <li>QuarterGrid - Snap to 1/4 tile grid (0.25 precision)
 *   <li>PixelGrid - Snap to 1/16 tile grid (0.0625 precision, pixel-level)
 *   <li>OffGrid - No snapping, free movement
 *   <li>CheckerGridEven - Snap to even squares of a checkerboard pattern
 *   <li>CheckerGridOdd - Snap to odd squares of a checkerboard pattern
 * </ul>
 */
public enum EditorSnapMode {
  /** Snap to the nearest whole tile grid. */
  ON_GRID("OnGrid"),
  /** Snap to the nearest quarter tile grid (0.25 precision). */
  QUARTER_GRID("QuarterGrid"),
  /** Snap to the nearest pixel grid (0.0625 precision, pixel-level). */
  PIXEL_GRID("PixelGrid"),
  /** No snapping, free movement. */
  OFF_GRID("OffGrid"),
  /** Snap to even squares of a checkerboard pattern. */
  CHECKER_GRID_EVEN("CheckerGridEven"),
  /** Snap to odd squares of a checkerboard pattern. */
  CHECKER_GRID_ODD("CheckerGridOdd");

  private final String displayName;

  EditorSnapMode(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Returns the display name for this snap mode.
   *
   * @return the human-readable name of this snap mode
   */
  public String displayName() {
    return displayName;
  }

  /**
   * Returns the next snap mode in the enumeration order.
   *
   * <p>Wraps around to the first mode if called on the last mode.
   *
   * @return the next snap mode
   */
  public EditorSnapMode nextMode() {
    return values()[(this.ordinal() + 1) % values().length];
  }

  /**
   * Snaps the given position to this snap mode's grid or pattern.
   *
   * <p>The snapping behavior depends on the specific mode:
   *
   * <ul>
   *   <li>OnGrid: Floors to the nearest integer
   *   <li>QuarterGrid: Floors to nearest 0.25
   *   <li>PixelGrid: Floors to nearest 0.0625 (1/16)
   *   <li>OffGrid: Returns position unchanged
   *   <li>CheckerGridEven/CheckerGridOdd: Snaps to the nearest square of the specified checkerboard
   *       parity
   * </ul>
   *
   * @param position the world position to snap
   * @return the snapped position according to this mode's grid pattern
   */
  public Point getPosition(Point position) {
    return switch (this) {
      case ON_GRID -> new Point((float) Math.floor(position.x()), (float) Math.floor(position.y()));
      case QUARTER_GRID ->
          new Point(
              (float) Math.floor(position.x() * 4) / 4.0f,
              (float) Math.floor(position.y() * 4) / 4.0f);
      case PIXEL_GRID ->
          new Point(
              (float) Math.floor(position.x() * 16) / 16.0f,
              (float) Math.floor(position.y() * 16) / 16.0f);
      case CHECKER_GRID_EVEN, CHECKER_GRID_ODD -> {
        int parity = (this == CHECKER_GRID_EVEN) ? 0 : 1;

        float px = position.x() - 0.5f;
        float py = position.y() - 0.5f;

        float gx = (float) Math.floor(px);
        float gy = (float) Math.floor(py);

        float bestX = gx;
        float bestY = gy;
        float bestDist = Float.MAX_VALUE;

        for (int dx = 0; dx <= 1; dx++) {
          for (int dy = 0; dy <= 1; dy++) {
            float cx = gx + dx;
            float cy = gy + dy;
            if (((int) (cx + cy)) % 2 == parity) {
              float dist = (px - cx) * (px - cx) + (py - cy) * (py - cy);
              if (dist < bestDist) {
                bestDist = dist;
                bestX = cx;
                bestY = cy;
              }
            }
          }
        }

        yield new Point(bestX, bestY);
      }
      case OFF_GRID -> position;
    };
  }

  /**
   * Determines whether collision/placement validation should be performed for this snap mode.
   *
   * <p>Blocking is enabled for grid-based snap modes where snapping ensures valid placements. For
   * free-form modes like OffGrid, blocking is disabled to allow more flexibility.
   *
   * @return true if collision checking should be enabled for this snap mode, false otherwise
   */
  public boolean checkBlocked() {
    return this == ON_GRID
        || this == QUARTER_GRID
        || this == CHECKER_GRID_EVEN
        || this == CHECKER_GRID_ODD;
  }
}
