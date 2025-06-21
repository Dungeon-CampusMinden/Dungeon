package core.utils;

import core.level.utils.Coordinate;
import java.util.Random;

/**
 * Represents a 2D direction as a vector with x and y components.
 *
 * <p>This enum provides constants for the four cardinal directions (UP, RIGHT, DOWN, LEFT) and a
 * 'NONE' direction for no movement. It handles relative directions (left, right, back) through
 * transformation methods like {@link #turnLeft()}, {@link #turnRight()}, {@link #opposite()} and
 * {@link #applyRelative(Direction)}.
 */
public enum Direction {
  /** The constant representing the upward direction (0, 1). */
  UP(0, 1),
  /** The constant representing the rightward direction (1, 0). */
  RIGHT(1, 0),
  /** The constant representing the downward direction (0, -1). */
  DOWN(0, -1),
  /** The constant representing the leftward direction (-1, 0). */
  LEFT(-1, 0),
  /** The constant representing no direction (0, 0). */
  NONE(0, 0);

  private final int x;
  private final int y;

  private static final Direction[] CARDINAL_DIRECTIONS = {UP, RIGHT, DOWN, LEFT};
  private static final Random RANDOM = new Random();

  /**
   * Constructs a new Direction.
   *
   * @param x The change in the x-coordinate.
   * @param y The change in the y-coordinate.
   */
  Direction(int x, int y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Returns the opposite direction.
   *
   * @return The opposite direction.
   */
  public Direction opposite() {
    return switch (this) {
      case UP -> DOWN;
      case DOWN -> UP;
      case LEFT -> RIGHT;
      case RIGHT -> LEFT;
      default -> NONE;
    };
  }

  /**
   * Returns the direction after turning 90 degrees to the left (relative 'LEFT').
   *
   * @return The new direction after a left turn.
   */
  public Direction turnLeft() {
    return switch (this) {
      case UP -> LEFT;
      case LEFT -> DOWN;
      case DOWN -> RIGHT;
      case RIGHT -> UP;
      default -> NONE;
    };
  }

  /**
   * Returns the direction after turning 90 degrees to the right (relative 'RIGHT').
   *
   * @return The new direction after a right turn.
   */
  public Direction turnRight() {
    return switch (this) {
      case UP -> RIGHT;
      case RIGHT -> DOWN;
      case DOWN -> LEFT;
      case LEFT -> UP;
      default -> NONE;
    };
  }

  /**
   * Applies a relative direction transformation to this direction.
   *
   * <p>This method allows you to apply a relative direction (like LEFT, RIGHT, or BACK) to the
   * current direction. It returns a new direction based on the transformation:
   *
   * <ul>
   *   <li>DOWN: Returns the opposite direction.
   *   <li>LEFT: Returns the direction after a left turn.
   *   <li>RIGHT: Returns the direction after a right turn.
   *   <li>NONE or UP: Returns the current direction unchanged.
   * </ul>
   *
   * @param relative The relative direction to apply.
   * @return The new direction after applying the relative transformation.
   */
  public Direction applyRelative(Direction relative) {
    return switch (relative) {
      case DOWN -> this.opposite();
      case LEFT -> this.turnLeft();
      case RIGHT -> this.turnRight();
      default -> this; // UP or NONE
    };
  }

  /**
   * Returns a random cardinal direction.
   *
   * @return A random direction from UP, RIGHT, DOWN, LEFT.
   */
  public static Direction random() {
    return CARDINAL_DIRECTIONS[RANDOM.nextInt(CARDINAL_DIRECTIONS.length)];
  }

  /**
   * Converts this direction to a {@link Point}.
   *
   * @return A new Point with the x and y from this direction.
   */
  public Point toPoint() {
    return new Point(x, y);
  }

  /**
   * Converts this direction to a {@link Coordinate}.
   *
   * @return A new Coordinate with the x and y from this direction.
   */
  public Coordinate toCoordinate() {
    return new Coordinate(x, y);
  }

  /**
   * Converts a string representation to a {@link Direction}.
   *
   * @param direction The string representation of the direction, e.g., "up", "down", "left",
   *     "right", "none".
   * @return The corresponding {@link Direction} ordinal.
   * @throws IllegalArgumentException if the string does not match any direction.
   */
  public static Direction fromString(String direction) {
    String lowerCaseDirection = direction.toLowerCase();
    for (Direction dir : values()) {
      if (dir.name().toLowerCase().equals(lowerCaseDirection)) {
        return dir;
      }
    }
    throw new IllegalArgumentException("Invalid direction: " + direction);
  }
}
