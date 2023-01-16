package ecs.components;

import ecs.entitys.Entity;
import tools.Point;

/** PositionComponent is a component that stores the x, y (as Point) position of an entity */
public class PositionComponent extends Component {

    private Point position;

    /**
     * @param entity associated entity
     * @param point position if the entity
     */
    public PositionComponent(Entity entity, Point point) {
        super(entity);
        this.position = point;
    }

    /**
     * @return the position of the associated entity
     */
    public Point getPosition() {
        return position;
    }

    /**
     * @param position new Position of the accociated entity
     */
    public void setPosition(Point position) {
        this.position = position;
    }
}
