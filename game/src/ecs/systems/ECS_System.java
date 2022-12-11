package ecs.systems;

import basiselements.Removable;
import mydungeon.ECS;

/** Marks a Class as a System in the ECS */
public abstract class ECS_System implements Removable {

    public ECS_System() {
        ECS.systems.add(this);
    }

    /** Gets called every Frame */
    public abstract void update();
}
