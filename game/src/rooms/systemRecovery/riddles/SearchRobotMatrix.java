package rooms.systemRecovery.riddles;

import engine.utils.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Tile-aligned matrix generated from two opposite corner points.
 *
 * <p>The first point is the first cell in row-major scan order and the second point is the last
 * cell. This keeps the order intuitive even when the level designer places the corners in a
 * different direction.
 *
 * @param rows number of matrix rows
 * @param columns number of matrix columns
 * @param points row-major cell positions
 */
record SearchRobotMatrix(int rows, int columns, List<Point> points) {

  /** Creates an inclusive matrix between two tile points. */
  static SearchRobotMatrix between(Point start, Point end) {
    int columnDirection = direction(start.x(), end.x());
    int rowDirection = direction(start.y(), end.y());
    int columns = tileDistance(start.x(), end.x(), "x") + 1;
    int rows = tileDistance(start.y(), end.y(), "y") + 1;

    List<Point> points = new ArrayList<>(rows * columns);
    for (int row = 0; row < rows; row++) {
      for (int column = 0; column < columns; column++) {
        points.add(new Point(start.x() + column * columnDirection, start.y() + row * rowDirection));
      }
    }
    return new SearchRobotMatrix(rows, columns, List.copyOf(points));
  }

  /** Returns the point at the given row and column. */
  Point pointAt(int row, int column) {
    return points.get(row * columns + column);
  }

  /** Returns the point at the given row-major index. */
  Point pointAt(int index) {
    return points.get(index);
  }

  /** Returns the number of cells in the matrix. */
  int size() {
    return points.size();
  }

  private static int direction(float from, float to) {
    if (from == to) return 0;
    return to > from ? 1 : -1;
  }

  private static int tileDistance(float from, float to, String axis) {
    float distance = Math.abs(to - from);
    int roundedDistance = Math.round(distance);
    if (Math.abs(distance - roundedDistance) > 0.001f) {
      throw new IllegalArgumentException(
          "Search robot matrix " + axis + " points must be aligned to whole tiles");
    }
    return roundedDistance;
  }
}
