package utils;

import core.utils.Point;

/** Direction enum for the four cardinal directions. */
public enum Direction {
  /**
   * The direction up.
   */
  UP(0, 1),
  /**
   * The direction down.
   */
  DOWN(0, -1),
  /**
   * The direction left.
   */
  LEFT(-1, 0),
  /**
   * The direction right.
   */
  RIGHT(1, 0);

  private final int x;
  private final int y;

  Direction(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Get the x coordinate of the direction.
   *
   * @return The x coordinate.
   */
  public int x() {
    return x;
  }

  /**
   * Get the y coordinate of the direction.
   *
   * @return The y coordinate.
   */
  public int y() {
    return y;
  }

  /**
   * Apply the direction to a point.
   *
   * <p>When the x coordinate of the direction is not 0, the x coordinate of the point will be moved
   * by the maximum integer value. When the y coordinate of the direction is not 0, the y coordinate
   * of the point will be moved by the maximum integer value.
   *
   * @param point Point that should be moved in the direction.
   * @return The new point after applying the direction.
   * @see Integer#MAX_VALUE
   */
  public Point applyDirection(Point point) {
    float newX = this.x == 0 ? point.x : this.x * Integer.MAX_VALUE;
    float newY = this.y == 0 ? point.y : this.y * Integer.MAX_VALUE;
    return new Point(newX, newY);
  }

  /**
   * Get the direction from a string.
   *
   * @param direction Direction as string.
   * @return The direction.
   */
  public static Direction fromString(String direction) {
    direction = direction.toLowerCase();
    return switch (direction) {
      case "oben" -> UP;
      case "unten" -> DOWN;
      case "links" -> LEFT;
      case "rechts" -> RIGHT;
      default -> throw new IllegalArgumentException("Invalid direction: " + direction);
    };
  }
}
