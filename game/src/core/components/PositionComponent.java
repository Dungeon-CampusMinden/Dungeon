package core.components;

import core.Component;
import core.Entity;
import core.Game;
import core.level.Tile;
import core.level.utils.LevelElement;
import core.utils.Point;
import core.utils.logging.CustomLogLevel;

import semanticanalysis.types.DSLContextMember;
import semanticanalysis.types.DSLType;

import java.util.logging.Logger;

/**
 * The PositionComponent stores the {@link Point} of an Entity that represents the position of the
 * associated entity in the level.
 *
 * <p>The {@link core.systems.DrawSystem} uses that position to draw the Entity at the correct
 * location.
 *
 * <p>The {@link core.systems.VelocitySystem} will update the position values.
 *
 * <p>Other Systems will use the position for other calculations, like AI movement.
 *
 * @see Point
 * @see core.systems.DrawSystem
 * @see core.systems.VelocitySystem
 */
@DSLType(name = "position_component")
public final class PositionComponent extends Component {

    private final Logger LOGGER = Logger.getLogger(this.getClass().getName());
    private Point position;

    /**
     * Creates a new PositionComponent and adds it to the associated entity.
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
     * Creates a new PositionComponent and adds it to the associated entity.
     *
     * <p>Sets the position of this entity to a point with the given x and y positions.
     *
     * @param entity associated entity
     * @param x x-position of the entity
     * @param y y-position of the entity
     */
    public PositionComponent(final Entity entity, final float x, final float y) {
        this(entity, new Point(x, y));
    }

    /**
     * Creates a new PositionComponent and adds it to the associated entity.
     *
     * <p>Sets the position of this entity on a random floor tile in the level.If no level is
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
