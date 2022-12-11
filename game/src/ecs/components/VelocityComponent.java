package ecs.components;

import ecs.entitys.Entity;
import mydungeon.ECS;

/** VelocityComponent is a component that stores the x, y movement direction */
public class VelocityComponent implements Component {

    private float x;
    private float y;

    public VelocityComponent(Entity entity, int x, int y) {
        ECS.velocityComponentMap.put(entity, this);
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
