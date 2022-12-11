package ecs.components;

import ecs.entitys.Entity;
import mydungeon.ECS;

/** VelocityComponent is a component that stores the x, y movement direction */
public class VelocityComponent implements Component {

    private float x;
    private float y;

    /**
     * @param entity associated entity
     * @param x x coordinate
     * @param y y coordinate
     */
    public VelocityComponent(Entity entity, int x, int y) {
        ECS.velocityComponentMap.put(entity, this);
        this.x = x;
        this.y = y;
    }

    /**
     * @return x movement
     */
    public float getX() {
        return x;
    }

    /**
     * @return y movement
     */
    public float getY() {
        return y;
    }
}
