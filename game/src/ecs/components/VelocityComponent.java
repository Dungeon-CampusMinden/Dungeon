package ecs.components;

import ecs.entitys.Entity;
import mydungeon.ECS;

/** VelocityComponent is a component that stores the x, y movement direction */
public class VelocityComponent extends Component {

    private float x;
    private float y;

    public VelocityComponent(Entity entity, int x, int y) {
        super(entity);
        ECS.velocityStore.addComponent(this);
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
