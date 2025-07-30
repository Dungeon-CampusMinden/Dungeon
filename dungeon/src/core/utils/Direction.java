package core.utils;

import java.util.Random;

/**
 * The {@code Direction} enum represents the four cardinal directions (UP, RIGHT, DOWN, LEFT) and a
 * NONE direction.
 *
 * <p>A direction can also be interpreted as a unit {@link Vector2} in a 2D space, where:
 *
 * <ul>
 *   <li>UP corresponds to (0, 1)
 *   <li>RIGHT corresponds to (1, 0)
 *   <li>DOWN corresponds to (0, -1)
 *   <li>LEFT corresponds to (-1, 0)
 *   <li>NONE corresponds to (0, 0)
 * </ul>
 *
 * <p>This enum provides methods to manipulate directions, such as turning left or right and
 * applying relative transformations. It also includes utility methods for random direction
 * selection and converting from string representations.
 *
 * @see Vector2
 */
public enum Direction implements Vector2 {
  /** Represents the upward direction with a unit vector of (0, 1). */
  UP(0, 1),
  /** Represents the rightward direction with a unit vector of (1, 0). */
  RIGHT(1, 0),
  /** Represents the downward direction with a unit vector of (0, -1). */
  DOWN(0, -1),
  /** Represents the leftward direction with a unit vector of (-1, 0). */
  LEFT(-1, 0),
  /** Represents no direction with a unit vector of (0, 0). */
  NONE(0, 0);

  private static final Random RANDOM = new Random();

  private final float x;
  private final float y;

  Direction(float x, float y) {
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
    Direction[] cardinalDirections = {UP, RIGHT, DOWN, LEFT};
    return cardinalDirections[RANDOM.nextInt(cardinalDirections.length)];
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

  @Override
  public float x() {
    return x;
  }

  @Override
  public float y() {
    return y;
  }
}
