package feature.prefabs;

import engine.utils.Point;
import java.util.Objects;

/**
 * An axis-aligned region defined by its lower-left and upper-right world points.
 *
 * <p>Bounds are normalized when constructed, so callers may supply the corners in either order.
 * Zero-width and zero-height regions are valid.
 *
 * @param bottomLeft normalized lower-left point
 * @param topRight normalized upper-right point
 */
public record Region(Point bottomLeft, Point topRight) {

  /** Creates a region, normalizing its bounds and rejecting non-finite coordinates. */
  public Region {
    Objects.requireNonNull(bottomLeft, "bottomLeft");
    Objects.requireNonNull(topRight, "topRight");
    if (!Float.isFinite(bottomLeft.x())
        || !Float.isFinite(bottomLeft.y())
        || !Float.isFinite(topRight.x())
        || !Float.isFinite(topRight.y())) {
      throw new IllegalArgumentException("Region bounds must contain finite coordinates");
    }
    Point first = bottomLeft;
    Point second = topRight;
    bottomLeft =
        new Point(Math.min(first.x(), second.x()), Math.min(first.y(), second.y()));
    topRight =
        new Point(Math.max(first.x(), second.x()), Math.max(first.y(), second.y()));
  }
}
