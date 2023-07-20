package core.components;

import core.Component;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.logging.CustomLogLevel;
import core.utils.logging.LoggerConfig;

import semanticanalysis.types.DSLContextMember;
import semanticanalysis.types.DSLType;

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
public final class PositionComponent extends Component {

    private final Logger LOGGER = LoggerConfig.getLogger(this.getClass().getName());
    private Point position;

    /**
     * Create a new PositionComponent with given position and add it to the associated entity.
     *
     * <p>Sets the position of this entity to the given point.
     *
     * @param entity The associated entity.
     * @param position The position of the entity in the level.
     */
    public PositionComponent(final Entity entity, final Point position) {
        super(entity);
        this.position = position;
    }

    /**
     * Create a new PositionComponent and add it to the associated entity.
     *
     * <p>Sets the position of this entity to a point with the given x and y positions.
     *
     * @param entity associated entity
     * @param x x-position of the entity
     * @param y y-position of the entity
     */
    public PositionComponent(final Entity entity, float x, float y) {
        this(entity, new Point(x, y));
    }

    /**
     * Create a new PositionComponent with random position and add it to the associated entity.
     *
     * <p>Sets the position of this entity on a random floor tile in the level. If no level is
     * loaded, set the position to (0,0). Beware that (0,0) may not necessarily be a playable area
     * within the level, it could be a wall or an "out of level" area.
     *
     * @param entity associated entity
     */
    public PositionComponent(@DSLContextMember(name = "entity") final Entity entity) {
        super(entity);

        if (Game.currentLevel() != null) {
            position = Game.randomTilePoint(LevelElement.FLOOR);
        } else {
            position = new Point(0, 0);
        }
    }

    /**
     * Get the position of the associated entity.
     *
     * @return The position of the associated entity.
     */
    public Point position() {
        LOGGER.log(
                CustomLogLevel.DEBUG,
                "Fetching position for entity '"
                        + entity
                        + "': x = "
                        + position.x
                        + " --- y = "
                        + position.y);
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
