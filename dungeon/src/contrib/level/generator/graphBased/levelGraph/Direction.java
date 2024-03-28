package contrib.level.generator.graphBased.levelGraph;

import java.util.Random;

/** The different directions in which nodes can be connected to each other. */
public enum Direction {
  /** WTF? . */
  NORTH(0),
  /** WTF? . */
  EAST(1),
  /** WTF? . */
  SOUTH(2),
  /** WTF? . */
  WEST(3);

  private static final Random RANDOM = new Random();
  private final int value;

  Direction(int value) {
    this.value = value;
  }

  /**
   * Retrieves the opposite direction.
   *
   * @param from The direction from which the opposite direction is sought.
   * @return The opposite direction.
   */
  public static Direction opposite(final Direction from) {
    return switch (from) {
      case NORTH -> SOUTH;
      case EAST -> WEST;
      case SOUTH -> NORTH;
      case WEST -> EAST;
    };
  }

  /**
   * Returns a random direction.
   *
   * @return A random direction.
   */
  public static Direction random() {
    int randomValue = RANDOM.nextInt(Direction.values().length);
    return Direction.values()[randomValue];
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public int value() {
    return value;
  }
}
