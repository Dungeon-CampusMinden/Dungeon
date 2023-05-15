package core;

import core.utils.DelayedSet;
import java.util.function.Consumer;
import java.util.logging.Logger;

/** Marks a Class as a System in the ECS */
public abstract class System implements Consumer<Entity> {
    protected boolean run;
    protected DelayedSet<Entity> entities;
    public Logger systemLogger = Logger.getLogger(this.getClass().getName());

    public System() {
        Game.systems.add(this);
        entities = new DelayedSet<>();
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

    /** Set this system on run */
    public void run() {
        run = true;
    }

    /** Set this system on pause */
    public void stop() {
        run = false;
    }
}
