package ecs.entitys;

import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import tools.Point;

public class Hero extends Entity {
    /**
     * Entity with Components
     *
     * @param startPosition
     */
    public Hero(Point startPosition) {
        super();
        new PositionComponent(this, startPosition);
        new VelocityComponent(this, 0, 0);
    }
}
