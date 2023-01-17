package ecs.components;

import ecs.entities.Entity;
import tools.Point;

/** PositionComponent is a component that stores the x, y (as Point) position of an entity */
public class PositionComponent extends Component {

    public static String name = "PositionComponent";

    private Point position;

    /**
     * @param entity associated entity
     * @param point position of the entity
     */
    public PositionComponent(Entity entity, Point point) {
        super(entity);
        this.position = point;
    }

    /**
     * @param entity associated entity
     */
    public PositionComponent(Entity entity) {
        super(entity);
        this.position = null;
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
