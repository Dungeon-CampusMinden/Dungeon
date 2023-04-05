package ecs.components;

import ecs.entities.Entity;
import java.util.logging.Logger;
import level.tools.LevelElement;
import logging.CustomLogLevel;
import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;
import starter.Game;
import tools.Point;

/**
 * The PositionComponent class represents a component that stores the x and y coordinates of an
 * entity as a Point object. It is used to associate entities with their positions in the game
 * world.
 */
@DSLType(name = "position_component")
public class PositionComponent extends Component {

    private /*@DSLTypeMember(name="position")*/ Point position;
    private final Logger positionCompLogger = Logger.getLogger(this.getClass().getName());

    /**
     * This constructor creates a new instance of the PositionComponent class with the given entity
     * and point parameters.
     *
     * @param entity The entity that is associated with this PositionComponent.
     * @param point The position of the entity, expressed as a Point object
     */
    public PositionComponent(@DSLContextMember(name = "entity") Entity entity, Point point) {
        super(entity);
        this.position = point;
    }

    /**
     * This constructor creates a new instance of the PositionComponent class with the given entity
     * and a random accessible position in the game world.
     *
     * @param entity The entity that is associated with this PositionComponent.
     */
    public PositionComponent(@DSLContextMember(name = "entity") Entity entity) {
        super(entity);
        this.position =
                Game.currentLevel.getRandomTile(LevelElement.FLOOR).getCoordinate().toPoint();
    }

    /**
     * Returns the position of the associated entity.
     *
     * @return The position of the associated entity as a Point object.
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
     * Sets the position of the associated entity.
     *
     * @param position The new position of the associated entity as a Point object.
     */
    public void setPosition(Point position) {
        this.position = position;
    }
}
