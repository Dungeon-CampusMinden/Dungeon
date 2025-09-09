package core.components;

import core.Component;
import core.level.Tile;
import core.level.utils.Coordinate;
import core.utils.Direction;
import core.utils.Point;

/**
 * Store the position of the associated entity in the level.
 *
 * <p>Various systems access the position of an entity through this component, e.g., the {@link
 * core.systems.DrawSystem} uses the position to draw an entity in the right place, and the {@link
 * core.systems.VelocitySystem} updates the position and direction of view values based on the
 * velocity and the previous position of an entity.
 *
 * <p>If the position is the {@link #ILLEGAL_POSITION}, the {@link core.systems.PositionSystem} will
 * change the position to a random position of an accessible tile in the current level.
 *
 * <p>Use {@link #position(Tile)} to set the position to the position of the given tile.
 *
 * <p>Use {@link #position(Point)} to set the position to the given position.
 *
 * <p>Use {@link #position()} to get a copy of the position.
 *
 * <p>Use {@link #viewDirection} to get the direction the entity is currently looking in.
 *
 * <p>Use {@link #viewDirection(Direction)} to set the direction the entity should look towards.
 *
 * @see core.systems.PositionSystem
 * @see Point
 */
public final class PositionComponent implements Component {

  /** The position of the entity in the level. */
  public static final Point ILLEGAL_POSITION = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);

  private Point position;
  private Direction viewDirection;
  private float rotation;

  /**
   * Create a new PositionComponent with given position.
   *
   * <p>Sets the position to the given point.
   *
   * @param position The position in the level.
   * @param viewDirection Direction the entity is looking to.
   */
  public PositionComponent(final Point position, final Direction viewDirection) {
    this.position = position;
    this.viewDirection = viewDirection;
  }

  /**
   * Create a new PositionComponent with given position.
   *
   * <p>Sets the position to the given point.
   *
   * <p>The Entity will look down.
   *
   * @param position The position in the level.
   */
  public PositionComponent(final Point position) {
    this(position, Direction.DOWN);
  }

  /**
   * Create a new PositionComponent with given position.
   *
   * <p>Sets the position to the given point.
   *
   * <p>The Entity will look down.
   *
   * @param x x-position
   * @param y y-position
   * @param viewDirection Direction the entity is looking to.
   */
  public PositionComponent(float x, float y, final Direction viewDirection) {
    this(new Point(x, y), viewDirection);
  }

  /**
   * Create a new PositionComponent.
   *
   * <p>Sets the position to a point with the given x and y positions.
   *
   * <p>The Entity will look down.
   *
   * @param x x-position
   * @param y y-position
   */
  public PositionComponent(float x, float y) {
    this(new Point(x, y), Direction.DOWN);
  }

  /**
   * Creates a new PositionComponent with a random position.
   *
   * <p>Sets the position of this entity to {@link #ILLEGAL_POSITION}. Keep in mind that if the
   * associated entity is processed by the {@link core.systems.PositionSystem}, {@link
   * #ILLEGAL_POSITION} will be replaced with a random accessible position.
   *
   * <p>The Entity will look down.
   */
  public PositionComponent() {
    position = ILLEGAL_POSITION;
    viewDirection = Direction.DOWN;
  }

  /**
   * Get the position.
   *
   * @return The position.
   */
  public Point position() {
    return new Point(position);
  }

  /**
   * Set the position.
   *
   * @param position new Position
   * @return the given position
   */
  public Point position(final Point position) {
    this.position = new Point(position);
    return position;
  }

  /**
   * Set the position.
   *
   * <p>Will center the entity on the given tile.
   *
   * @param tile The tile where the new position is located.
   * @see Tile
   */
  public void position(final Tile tile) {
    position(tile.position());
  }

  /**
   * Set the position to the corner of the current tile.
   *
   * <p>This basically rounds down the x and y coordinates.
   */
  public void toTileCorner() {
    position = position.toCoordinate().toPoint();
  }

  /**
   * Get the position as coordinate.
   *
   * @return The position as coordinate.
   */
  public Coordinate coordinate() {
    return position.toCoordinate();
  }

  /**
   * Get the direction of view.
   *
   * @return current direction of view
   */
  public Direction viewDirection() {
    return viewDirection;
  }

  /**
   * Set direction of view.
   *
   * @param direction new direction of view
   */
  public void viewDirection(final Direction direction) {
    this.viewDirection = direction;
  }

  /**
   * Get the rotation.
   *
   * @return The rotation.
   */
  public float rotation() {
    return rotation;
  }

  /**
   * Set the rotation.
   *
   * @param rotation new rotation
   */
  public void rotation(final float rotation) {
    this.rotation = rotation;
  }
}
