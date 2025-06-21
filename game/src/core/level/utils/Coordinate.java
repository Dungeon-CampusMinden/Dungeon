package core.level.utils;

import core.utils.Point;
import core.utils.Vector2;

/**
 * Coordinate in the dungeon, based on array index.
 *
 * <p>No getter needed. All attributes are public.
 */
public class Coordinate {

  /** The x-Coordinate. */
  public int x;

  /** The y-Coordinate. */
  public int y;

  /**
   * Create a new Coordinate.
   *
   * @param x x-Coordinate
   * @param y y-Coordinate
   */
  public Coordinate(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Copy a coordinate.
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
   * Convert Coordinate to Point.
   *
   * @return Coordinate converted to a point;
   */
  public Point toPoint() {
    return new Point(x, y);
  }

  /**
   * Convert Coordinate to Point centered in the tile.
   *
   * @return Coordinate converted to a point;
   */
  public Point toCenteredPoint() {
    return new Point(x + 0.5f, y + 0.5f);
  }

  /**
   * Creates a new Coordinate which has the sum of the Coordinates.
   *
   * @param other which Coordinate to add
   * @return Coordinate where the values for x and y are added
   * @deprecated A coordinate should not be added to another coordinate. Use {@link #add(Vector2)} to
   *     move a coordinate or {@link #vectorTo(Coordinate)} to get the vector between two
   *     coordinates.
   */
  @Deprecated
  public Coordinate add(final Coordinate other) {
    return new Coordinate(this.x + other.x, this.y + other.y);
  }

  /**
   * Moves this coordinate by a vector.
   *
   * @param vector The vector to move the coordinate by.
   * @return A new coordinate that is the sum of this coordinate and the given vector.
   */
  public Coordinate add(final Vector2 vector) {
    return new Coordinate(this.x + (int) vector.x(), this.y + (int) vector.y());
  }

  /**
   * Calculates the vector from this coordinate to another coordinate.
   *
   * @param other The other coordinate.
   * @return The vector from this coordinate to the other coordinate.
   */
  public Vector2 vectorTo(final Coordinate other) {
    return new Vector2(other.x - this.x, other.y - this.y);
  }

  @Override
  public String toString() {
    return "Coordinate{" + "x=" + x + ", y=" + y + '}';
  }

  /**
   * Calculates the distance between this Coordinate and another Coordinate.
   *
   * @param other The other Coordinate to calculate the distance to.
   * @return The distance between this Coordinate and the other Coordinate.
   */
  public int distance(Coordinate other) {
    return Math.abs(x - other.x) + Math.abs(y - other.y);
  }
}
