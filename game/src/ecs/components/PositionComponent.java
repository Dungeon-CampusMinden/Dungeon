package ecs.components;

import ecs.entities.Entity;
import level.tools.LevelElement;
import mydungeon.ECS;
import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;
import semanticAnalysis.types.DSLTypeMember;
import tools.Point;

/** PositionComponent is a component that stores the x, y (as Point) position of an entity */
@DSLType
public class PositionComponent extends Component {

    public static String name = "PositionComponent";

    private @DSLTypeMember Point position;

    /**
     * @param entity associated entity
     * @param point position of the entity
     */
    public PositionComponent(@DSLContextMember(name = "entity") Entity entity, Point point) {
        super(entity, name);
        this.position = point;
    }

    /**
     * @param entity associated entity
     */
    public PositionComponent(@DSLContextMember(name = "entity") Entity entity) {
        super(entity, name);
        this.position =
                ECS.currentLevel.getRandomTile(LevelElement.FLOOR).getCoordinate().toPoint();
    }

    /**
     * @return the position of the associated entity
     */
    public Point getPosition() {
        return position;
    }

    /**
     * @param position new Position of the associated entity
     */
    public void setPosition(Point position) {
        this.position = position;
    }
}
