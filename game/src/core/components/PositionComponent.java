package core.components;

import core.Component;
import core.level.Tile;
import core.utils.Point;
import dsl.annotation.DSLType;

/**
 * Store the position of the associated entity in the level.
 *
 * <p>Various systems access the position of an entity through this component, e.g., the {@link
 * core.systems.DrawSystem} uses the position to draw an entity in the right place, and the {@link
 * core.systems.VelocitySystem} updates the position values based on the velocity and the previous
 * position of an entity.
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
 * @see core.systems.PositionSystem
 * @see Point
 */
@DSLType(name = "position_component")
public final class PositionComponent implements Component {

  /** The position of the entity in the level. */
  public static final Point ILLEGAL_POSITION = new Point(Integer.MIN_VALUE, Integer.MIN_VALUE);

  private Point position;

  /**
   * Create a new PositionComponent with given position.
   *
   * <p>Sets the position to the given point.
   *
   * @param position The position in the level.
   */
  public PositionComponent(final Point position) {
    this.position = position;
  }

  /**
   * Create a new PositionComponent.
   *
   * <p>Sets the position to a point with the given x and y positions.
   *
   * @param x x-position
   * @param y y-position
   */
  public PositionComponent(float x, float y) {
    this(new Point(x, y));
  }

  /**
   * Creates a new PositionComponent with a random position.
   *
   * <p>Sets the position of this entity to {@link #ILLEGAL_POSITION}. Keep in mind that if the
   * associated entity is processed by the {@link core.systems.PositionSystem}, {@link
   * #ILLEGAL_POSITION} will be replaced with a random accessible position.
   */
  public PositionComponent() {
    position = ILLEGAL_POSITION;
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
   */
  public void position(final Point position) {
    this.position = new Point(position);
  }

  /**
   * Set the position.
   *
   * @param tile The tile where the new position is located.
   * @see Tile
   */
  public void position(final Tile tile) {
    position(tile.position());
  }
}
