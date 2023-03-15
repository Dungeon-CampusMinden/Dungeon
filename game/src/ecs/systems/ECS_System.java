package ecs.systems;

import java.util.logging.Logger;
import starter.Game;

/** Marks a Class as a System in the ECS */
public abstract class ECS_System {

    protected boolean run;
    public Logger systemLogger = Logger.getLogger(this.getClass().getName());

    public ECS_System() {
        Game.systems.add(this);
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
