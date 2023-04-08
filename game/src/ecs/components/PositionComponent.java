package ecs.components;

import ecs.entities.Entity;
import java.util.logging.Logger;
import logging.CustomLogLevel;
import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;
import starter.Game;
import tools.Point;

/** A PositionComponent stores the associated entity's position in the level */
@DSLType(name = "position_component")
public class PositionComponent extends Component {

    private final Logger positionCompLogger = Logger.getLogger(this.getClass().getName());
    private /*@DSLTypeMember(name="position")*/ Point position;

    /**
     * Creates a new PositionComponent at a given point.
     *
     * <p>Creates a new PositionComponent to store the associated entity's position in the level and
     * add this component to the associated entity.
     *
     * <p>Sets the position of this entity to the given point.
     *
     * @param entity associated entity
     * @param point position of the entity
     */
    public PositionComponent(@DSLContextMember(name = "entity") Entity entity, Point point) {
        super(entity);

        this.position = point;
    }

    /**
     * Creates a new PositionComponent at a given point.
     *
     * <p>Creates a new PositionComponent to store the associated entity's position in the level and
     * add this component to the associated entity.
     *
     * <p>Sets the position of this entity to a point with the given x and y positions.
     *
     * @param entity associated entity
     * @param x x-position of the entity
     * @param y y-position of the entity
     */
    public PositionComponent(@DSLContextMember(name = "entity") Entity entity, float x, float y) {
        this(entity, new Point(x, y));
    }

    /**
     * Creates a new PositionComponent at a random point.
     *
     * <p>Creates a new PositionComponent to store the associated entity's position in the level and
     * add this component to the associated entity.
     *
     * <p>Sets the position of this entity on a random floor tile in the level. If no level is
     * loaded, set the position to (0,0). Beware that (0,0) may not necessarily be a playable area
     * within the level, it could be a wall or an "out of level" area.
     *
     * @param entity associated entity
     */
    public PositionComponent(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);

        if (Game.currentLevel != null) {
            position = Game.currentLevel.getRandomFloorTile().getCoordinateAsPoint();
        } else {
            position = new Point(0, 0);
        }
    }

    /**
     * @return the position of the associated entity
     */
    public Point getPosition() {
        positionCompLogger.log(
                CustomLogLevel.DEBUG,
                "Fetching position for entity '"
                        + entity.getClass().getSimpleName()
                        + "': x = "
                        + position.x
                        + " --- y = "
                        + position.y);
        return position;
    }

    /**
     * @param position new Position of the associated entity
     */
    public void setPosition(Point position) {
        this.position = position;
    }
}
