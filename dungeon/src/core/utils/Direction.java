package core.utils;

import core.level.utils.Coordinate;
import java.util.Random;

/**
 * Represents a 2D direction as a vector with x and y components.
 *
 * <p>This record provides constants for the four cardinal directions and a 'NONE' direction for no
 * movement. It handles relative directions (left, right, back) through transformation methods like
 * {@link #turnLeft()}, {@link #turnRight()}, {@link #opposite()} and {@link #applyRelative(Direction)}.
 *
 * @param dx The change in the x-coordinate.
 * @param dy The change in the y-coordinate.
 * @param name The name of the direction, e.g., "up", "down", "left", "right", or "none".
 */
public record Direction(int dx, int dy, String name) {

  /** The constant representing the upward direction (0, 1). */
  public static final Direction UP = new Direction(0, 1, "up");

  /** The constant representing the rightward direction (1, 0). */
  public static final Direction RIGHT = new Direction(1, 0, "right");

  /** The constant representing the downward direction (0, -1). */
  public static final Direction DOWN = new Direction(0, -1, "down");

  /** The constant representing the leftward direction (-1, 0). */
  public static final Direction LEFT = new Direction(-1, 0, "left");

  /** The constant representing no direction (0, 0). */
  public static final Direction NONE = new Direction(0, 0, "none");

  private static final Direction[] VALUES = {UP, RIGHT, DOWN, LEFT};
  private static final Random RANDOM = new Random();

  private Direction(int dx, int dy) {
    this(
        dx,
        dy,
        switch (dx) {
          case 0 -> (dy > 0) ? "up" : (dy < 0) ? "down" : "none";
          case 1 -> "right";
          case -1 -> "left";
          default -> "none"; // For dx = 0 and dy = 0
        });
  }

  /**
   * Returns the opposite direction.
   *
   * @return The opposite direction.
   */
  public Direction opposite() {
    return new Direction(-dx, -dy);
  }

  /**
   * Returns the direction after turning 90 degrees to the left (relative 'LEFT').
   *
   * @return The new direction after a left turn.
   */
  public Direction turnLeft() {
    return new Direction(-dy, dx);
  }

  /**
   * Returns the direction after turning 90 degrees to the right (relative 'RIGHT').
   *
   * @return The new direction after a right turn.
   */
  public Direction turnRight() {
    return new Direction(dy, -dx);
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
    if (relative.equals(DOWN)) {
      return this.opposite();
    } else if (relative.equals(LEFT)) {
      return this.turnLeft();
    } else if (relative.equals(RIGHT)) {
      return this.turnRight();
    }
    return this; // If NONE or UP
  }

  /**
   * Returns the name of this direction.
   *
   * @return The name of the direction, e.g., "up", "down", "left", "right", or "none".
   */
  public String name() {
    return name;
  }

  /**
   * Returns an array of all cardinal directions.
   *
   * @return An array containing the directions: UP, RIGHT, DOWN, LEFT.
   */
  public static Direction[] values() {
    return VALUES.clone();
  }

  /**
   * Returns the integer value representing this direction.
   *
   * <p>Returns:
   *
   * <ul>
   *   <li>0 for UP
   *   <li>1 for RIGHT
   *   <li>2 for DOWN
   *   <li>3 for LEFT
   *   <li>-1 for NONE (not a cardinal direction)
   * </ul>
   *
   * @return The integer value of the direction.
   */
  public int value() {
    if (this.equals(UP)) {
      return 0;
    } else if (this.equals(RIGHT)) {
      return 1;
    } else if (this.equals(DOWN)) {
      return 2;
    } else if (this.equals(LEFT)) {
      return 3;
    } else {
      return -1; // NONE is not a cardinal direction
    }
  }

  /**
   * Returns a random cardinal direction.
   *
   * @return A random direction from NORTH, EAST, SOUTH, WEST.
   */
  public static Direction random() {
    return VALUES[RANDOM.nextInt(VALUES.length)];
  }

  /**
   * Converts this direction to a {@link Point}.
   *
   * @return A new Point with the dx and dy from this direction.
   */
  public Point toPoint() {
    return new Point(dx, dy);
  }

  /**
   * Converts this direction to a {@link Coordinate}.
   *
   * @return A new Coordinate with the dx and dy from this direction.
   */
  public Coordinate toCoordinate() {
    return new Coordinate(dx, dy);
  }

  /**
   * Converts a string representation to a {@link Direction}.
   *
   * @param direction The string representation of the direction, e.g., "up", "down", "left",
   *     "right".
   * @return The corresponding {@link Direction} value.
   * @throws IllegalArgumentException if the string does not match any direction.
   */
  public static Direction fromString(String direction) {
    direction = direction.toLowerCase();

    for (Direction dir : Direction.values()) {
      if (dir.name.equals(direction)) {
        return dir;
      }
    }
    throw new IllegalArgumentException("Invalid direction: " + direction);
  }

  @Override
  public String toString() {
    return "Direction{" + "dx=" + dx + ", dy=" + dy + ", name='" + name + '\'' + '}';
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (!(other instanceof Direction(int otherDx, int otherDy, String otherName))) return false;
    return dx == otherDx && dy == otherDy && name.equals(otherName);
  }
}
