package contrib.utils;

import core.components.PositionComponent;
import core.level.Tile;
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
      case DOWN -> PositionComponent.Direction.DOWN;
      default ->
          throw new IllegalArgumentException(
              "Can not convert " + viewDirection + " to PositionComponent.Direction.");
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
      case DOWN -> Direction.DOWN;
    };
  }

  /**
   * Convert a {@link PositionComponent.Direction} into a {@link Point}.
   *
   * <p>This is a convenience method to avoid unnecessary method calls/method chaining, i.e. instead
   * of writing <code>
   * Direction.fromPositionCompDirection(EntityUtils.getViewDirection(hero)).toPoint()</code> all
   * the time we can just use <code>
   * Direction.asPoint(EntityUtils.getViewDirection(hero))</code> now.
   *
   * @param viewDirection Direction to convert.
   * @return Converted direction.
   */
  public static Point asPoint(PositionComponent.Direction viewDirection) {
    return fromPositionCompDirection(viewDirection).toPoint();
  }

  /**
   * Transforms a relative direction into a world direction based on this view direction.
   *
   * <p>For example, if the hero is looking to the RIGHT and wants to check LEFT (relative to their
   * view), the resulting direction would be UP in world coordinates.
   *
   * @param relativeDirection The relative direction to check.
   * @return The translated direction in world coordinates.
   */
  public Direction relativeToAbsoluteDirection(Direction relativeDirection) {
    return switch (relativeDirection) {
      case LEFT ->
          switch (this) {
            case UP -> Direction.LEFT;
            case DOWN -> Direction.RIGHT;
            case RIGHT -> Direction.UP;
            case LEFT -> Direction.DOWN;
            case HERE -> this;
          };
      case RIGHT ->
          switch (this) {
            case UP -> Direction.RIGHT;
            case DOWN -> Direction.LEFT;
            case RIGHT -> Direction.DOWN;
            case LEFT -> Direction.UP;
            case HERE -> this;
          };
      case DOWN ->
          switch (this) {
            case UP -> Direction.DOWN;
            case DOWN -> Direction.UP;
            case RIGHT -> Direction.LEFT;
            case LEFT -> Direction.RIGHT;
            case HERE -> this;
          };
      case UP ->
          switch (this) {
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            case RIGHT -> Direction.RIGHT;
            case LEFT -> Direction.LEFT;
            case HERE -> this;
          };
      case HERE -> Direction.HERE;
    };
  }

  /**
   * Converts a {@link Tile.Direction} to a {@link PositionComponent.Direction}.
   *
   * @param direction The direction to convert.
   * @return The converted direction.
   */
  public static PositionComponent.Direction convertTileDirectionToPosDirection(
      Tile.Direction direction) {
    return switch (direction) {
      case W -> PositionComponent.Direction.LEFT;
      case E -> PositionComponent.Direction.RIGHT;
      case N -> PositionComponent.Direction.UP;
      case S -> PositionComponent.Direction.DOWN;
    };
  }
}
