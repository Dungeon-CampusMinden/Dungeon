package core.components;

import core.Component;
import core.level.Tile;
import core.utils.Point;

import dsl.semanticanalysis.typesystem.typebuilding.annotation.DSLType;

import java.util.logging.Logger;

/**
 * Store the position of the associated entity in the level.
 *
 * <p>Various systems access the position of an entity through this component, e.g. the {@link
 * core.systems.DrawSystem} uses the position to draw an entity in the right place and the {@link
 * core.systems.VelocitySystem} updates the position values based on the velocity and the previous
 * position of an entity. See <a
 * href="https://github.com/Programmiermethoden/Dungeon/tree/master/doc/ecs/systems">System-Overview</a>.
 *
 * @see Point
 */
@DSLType(name = "position_component")
public final class PositionComponent implements Component {

    public static final Point ILLEGAL_POSITION = new Point(-100, -100);
    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());
    private Point position;

    /**
     * Create a new PositionComponent with given position.
     *
     * <p>Sets the position of this entity to the given point.
     *
     * @param position The position of the entity in the level.
     */
    public PositionComponent(final Point position) {
        this.position = position;
    }

    /**
     * Create a new PositionComponent.
     *
     * <p>Sets the position of this entity to a point with the given x and y positions.
     *
     * @param x x-position of the entity
     * @param y y-position of the entity
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
     * Get the position of the associated entity.
     *
     * @return The position of the associated entity.
     */
    public Point position() {
        return position;
    }

    /**
     * Set the position of the associated entity
     *
     * @param position new Position of the associated entity
     */
    public void position(final Point position) {
        this.position = position;
    }

    /**
     * Set the position of the associated entity.
     *
     * @param tile The tile where the new position of the associated entity is located.
     * @see Tile
     */
    public void position(final Tile tile) {
        position(tile.position());
    }
}
