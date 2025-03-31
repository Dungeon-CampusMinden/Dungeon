package utils;

import core.components.PositionComponent;
import core.utils.Point;

/** Direction enum for the four cardinal directions. */
public enum Direction {
  /** The direction up. */
  UP("oben", 0, 1),
  /** The direction down. */
  DOWN("unten", 0, -1),
  /** The direction left. */
  LEFT("links", -1, 0),
  /** The direction right. */
  RIGHT("rechts", 1, 0),

  /** The current Position. */
  HERE("hier", 0, 0);

  private final String dirName;
  private final int x;
  private final int y;

  Direction(String name, int x, int y) {
    this.dirName = name;
    this.x = x;
    this.y = y;
  }

  /**
   * Get the name of the direction.
   *
   * @return The name of the direction.
   */
  public String dirName() {
    return dirName;
  }

  /**
   * Get the x direction of the direction.
   *
   * <p>X represents the horizontal direction, where -1 is left, 0 is no movement in the x
   * direction, and 1 is right.
   *
   * @return The x direction.
   */
  public int x() {
    return x;
  }

  /**
   * Get the y direction of the direction.
   *
   * <p>Y represents the vertical direction, where -1 is down, 0 is no movement in the y direction,
   * and 1 is up.
   *
   * @return The y direction.
   */
  public int y() {
    return y;
  }

  /**
   * Convert this direction to a {@link Point}.
   *
   * @return Point with the x and y from this direction
   */
  public Point toPoint() {
    return new Point(x, y);
  }

  /**
   * Get the direction from a string.
   *
   * @param direction Direction as string.
   * @return The direction.
   */
  public static Direction fromString(String direction) {
    direction = direction.toLowerCase();

    for (Direction dir : Direction.values()) {
      if (dir.dirName.equals(direction)) {
        return dir;
      }
    }
    throw new IllegalArgumentException("Invalid direction: " + direction);
  }

  /**
   * Converts a {@link Direction} into a {@link PositionComponent.Direction}.
   *
   * @param viewDirection Direction to convert.
   * @return Converted direction.
   */
  public static PositionComponent.Direction toPositionCompDirection(Direction viewDirection) {
    return switch (viewDirection) {
      case LEFT -> PositionComponent.Direction.LEFT;
      case RIGHT -> PositionComponent.Direction.RIGHT;
      case UP -> PositionComponent.Direction.UP;
      default -> PositionComponent.Direction.DOWN;
    };
  }

  /**
   * Converts a {@link PositionComponent.Direction} into a {@link Direction}.
   *
   * @param viewDirection Direction to convert.
   * @return Converted direction.
   */
  public static Direction fromPositionCompDirection(PositionComponent.Direction viewDirection) {
    return switch (viewDirection) {
      case LEFT -> Direction.LEFT;
      case RIGHT -> Direction.RIGHT;
      case UP -> Direction.UP;
      default -> Direction.DOWN;
    };
  }
}
