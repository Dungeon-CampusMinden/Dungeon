package ecs.systems;

import mydungeon.ECS;

/** Marks an Class as a System in the ECS */
public abstract class ECS_System {

    public ECS_System() {
        ECS.systems.add(this);
    }

    /** Get called every Frame */
    public abstract void update();
}
