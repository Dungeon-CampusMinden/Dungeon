package ecs.components;

import ecs.entitys.Entity;
import mydungeon.ECS;
import tools.Point;

/** PositionComponent is a component that stores the x, y (as Point) position of an entity */
public class PositionComponent implements Component {

    private Point position;

    /**
     * @param entity associated entity
     * @param point position of the entity
     */
    public PositionComponent(Entity entity, Point point) {
        ECS.positionComponentMap.put(entity, this);
        this.position = point;
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
