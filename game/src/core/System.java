package core;

import core.utils.DelayedSet;
import core.utils.logging.CustomLogLevel;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;

/** Marks a Class as a System in the ECS */
public abstract class System implements Consumer<Entity> {
    protected boolean run;
    private DelayedSet<Entity> entities;
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

    /**
     * Use this Stream to iterate over all active entities for this system in the {@link
     * #update()}-Method.
     *
     * @return active entities that will be processed by the system as stream
     */
    protected Stream<Entity> getEntityStream() {
        return entities.getSet().stream();
    }

    /**
     * Add the given entity to the {@link DelayedSet} of entities that will be processed by the
     * system.
     *
     * @see DelayedSet
     * @param entity Entity to add
     */
    protected void addEntity(Entity entity) {
        if (entities.add(entity))
            systemLogger.log(
                    CustomLogLevel.INFO,
                    "Entity " + entity + " will be added to the " + getClass().getName());
    }

    /**
     * Remove the given entity to the {@link DelayedSet} of entities that will no longer be
     * processed by the system.
     *
     * @see DelayedSet
     * @param entity Entity to remove
     */
    protected void removeEntity(Entity entity) {
        entities.remove(entity);
        systemLogger.log(
                CustomLogLevel.INFO,
                "Entity " + entity + " will be removed from to the " + getClass().getName());
    }

    /**
     * Util function to log that the given entity will not be processed by the calling system,
     * because the given component is missing.
     *
     * @param entity Entity that will not be processed
     * @param missingComponent the component that is missing
     */
    protected void logMissingComponent(Entity entity, Class missingComponent) {
        systemLogger.log(
                CustomLogLevel.INFO,
                "Entity: "
                        + entity
                        + " Not processed by the "
                        + getClass().getName()
                        + " because the component "
                        + missingComponent.getName()
                        + " is missing ");
    }
}
