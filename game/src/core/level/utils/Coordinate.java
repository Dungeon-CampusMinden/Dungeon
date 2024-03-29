package core.level.utils;

import core.utils.Point;

/**
 * Coordinate in the dungeon, based on array index.
 *
 * <p>No getter needed. All attributes are public.
 */
public class Coordinate {

  public int x;
  public int y;

  /**
   * Create a new Coordinate
   *
   * @param x x-Coordinate
   * @param y y-Coordinate
   */
  public Coordinate(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Copy a coordinate
   *
   * @param copyFrom Coordinate to copy
   */
  public Coordinate(final Coordinate copyFrom) {
    x = copyFrom.x;
    y = copyFrom.y;
  }

  @Override
  public boolean equals(final Object o) {
    if (!(o instanceof Coordinate other)) {
      return false;
    }
    return x == other.x && y == other.y;
  }

  @Override
  public int hashCode() {
    return x + y;
  }

  /**
   * Convert Coordinate to Point
   *
   * @return Coordinate converted to a point;
   */
  public Point toPoint() {
    return new Point(x, y);
  }

  /**
   * Creates a new Coordinate which has the sum of the Coordinates
   *
   * @param other which Coordinate to add
   * @return Coordinate where the values for x and y are added
   */
  public Coordinate add(final Coordinate other) {
    return new Coordinate(this.x + other.x, this.y + other.y);
  }
}
