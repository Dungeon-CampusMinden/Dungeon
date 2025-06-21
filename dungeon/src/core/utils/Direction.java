package core.utils;

import com.badlogic.gdx.math.Vector2;
import core.level.utils.Coordinate;
import java.util.Random;
import java.util.Vector;

/**
 * The {@code Direction} enum represents the four cardinal directions (UP, RIGHT, DOWN, LEFT) and a
 * NONE direction.
 *
 * <p>This enum provides methods to manipulate directions, such as turning left or right, applying
 * relative transformations, and translating coordinates or points based on the direction. It also
 * includes utility methods for random direction selection and converting string representations to
 * {@code Direction} values.
 */
public enum Direction {
  /** The constant representing the upward direction. */
  UP,
  /** The constant representing the rightward direction. */
  RIGHT,
  /** The constant representing the downward direction. */
  DOWN,
  /** The constant representing the leftward direction. */
  LEFT,
  /** The constant representing no direction. */
  NONE;

  private static final Random RANDOM = new Random();

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
   * Translates the given coordinate by the direction vector of this Direction.
   *
   * <p>This method calculates the new coordinate by adding the direction vector (derived from this
   * Direction) to the provided coordinate.
   *
   * @param coordinate The original coordinate to be translated.
   * @return A new Coordinate object representing the translated position.
   */
  public Coordinate translate(Coordinate coordinate) {
    Vector<Integer> dirVec = directionVector();
    return coordinate.add(new Coordinate(dirVec.getFirst(), dirVec.getLast()));
  }

  /**
   * Translates the given point by the direction vector of this Direction.
   *
   * <p>This method calculates the new point by adding the direction vector (derived from this
   * Direction) to the provided point.
   *
   * @param point The original point to be translated.
   * @return A new Point object representing the translated position.
   */
  public Point translate(Point point) {
    Vector<Integer> dirVec = directionVector();
    return point.add(new Point(dirVec.getFirst(), dirVec.getLast()));
  }

  /**
   * Returns a vector representation of the direction.
   *
   * <p>This method converts the direction into a {@link Vector2} where:
   *
   * <ul>
   *   <li>UP is represented as (0, -1)
   *   <li>DOWN is represented as (0, 1)
   *   <li>LEFT is represented as (-1, 0)
   *   <li>RIGHT is represented as (1, 0)
   *   <li>NONE is represented as (0, 0)
   * </ul>
   *
   * @return A {@link Vector2} representing the direction.
   */
  private Vector<Integer> directionVector() {
    int x = 0;
    int y = 0;
    switch (this) {
      case UP -> y = -1;
      case DOWN -> y = 1;
      case LEFT -> x = -1;
      case RIGHT -> x = 1;
      case NONE -> {
        // No change to x and y
      }
    }
    return new Vector<>(x, y);
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
