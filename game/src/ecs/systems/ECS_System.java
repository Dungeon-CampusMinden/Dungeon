package ecs.systems;

import basiselements.Removable;
import mydungeon.ECS;

/** Marks a Class as a System in the ECS */
public abstract class ECS_System implements Removable {

    protected boolean run;

    public ECS_System() {
        ECS.systems.add(this);
        run = true;
    }

    /** Gets called every Frame */
    public abstract void update();

    /**
     * @return true if this system is running, false if it is in pause mode
     */
    public boolean isRunning() {
        return run;
    }

    /** Toggle this system between run and pause */
    public void toggleRun() {
        run = !run;
    }
}
