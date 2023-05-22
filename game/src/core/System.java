package core;

import core.utils.DelayedSet;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * A System implements a specific game logic (a gameplay mechanic).
 *
 * <p>This class is the abstract base class for each system. It implements the basic functionality
 * each system has. For example, it allows that the system can be paused and unpause.
 *
 * <p>A system will iterate over each {@link Entity} with specific {@link Component}s. The {@link
 * #accept}-Method checks, if the entity has the needed components for the system.
 *
 * <p>If an entity has all needed components for the system, the system will store the entity in its
 * local set,
 *
 * <p>The {@link #execute}-Method will execute the system-logic on each entity in the set.
 *
 * <p>The update-Method gets called every frame in the game loop from {@link Game}.
 *
 * <p>Systems are designed to be unique, so don't create two systems of the same type.
 */
public abstract class System {
    private final Set<Entity> entities;
    public Logger LOGGER = Logger.getLogger(this.getClass().getName());
    protected boolean run;

    public System() {
        LOGGER.info("A new " + this.getClass().getName() + " was created");
        Game.addSystem(this);
        entities = new HashSet<>();
        Game.getEntitiesStream().forEach(this::showEntity);
        run = true;
    }

    /**
     * Implements the functionality of the system.
     */
    public abstract void execute();

    /**
     * Check if the given entity has all components that are needed to get processed by this system.
     *
     * <p>If the entity has all components, the entity will be added to the internal set of this
     * system.
     *
     * <p>If the key-component is missing, this system will ignore the entity. The entity will be
     * removed from the internal set, if it is present.
     *
     * <p>If an addition-component is missing, this system will ignore the entity and create a
     * log-entry with the information of the missing component. The entity will be removed from the
     * internal set, if it is present.
     *
     * @param entity entity to add
     * @return true if the entity is accepted, false if not.
     */
    public boolean showEntity(Entity entity) {
        if (accept(entity)) {
            if (entities.add(entity))
                LOGGER.info("Entity " + entity + " will be added to the " + getClass().getName());
            return true;
        }
        removeEntity(entity);
        return false;
    }

    /**
     * Remove the given entity of the local set so that it will no longer be processed by the
     * system.
     *
     * @param entity Entity to remove
     */
    public void removeEntity(Entity entity) {
        if (entities.remove(entity))
            LOGGER.info(
                "Entity " + entity + " will be removed from to the " + getClass().getName());
    }

    /**
     * Remove all entities immediately from this system.
     *
     * <p>Do not call this function inside {@link #execute} or you risc a {@link
     * java.util.ConcurrentModificationException}
     *
     * @see java.util.ConcurrentModificationException
     */
    public void clearEntities() {
        LOGGER.info("All entities from " + this.getClass().getName() + " were removed");
        entities.clear();
    }

    /**
     * Toggle this system between run and pause.
     *
     * <p>A paused system will not be executed.
     *
     * <p>A paused system can still accept, add and remove entities. The internal set will be
     * executed, when the system will run.
     *
     * <p>A running system will be executed.
     */
    public void toggleRun() {
        if (run) stop();
        else run();
    }

    /**
     * Set this system on run.
     *
     * <p>A running system will be executed.
     */
    public void run() {
        run = true;
        LOGGER.info(this.getClass().getName() + " will run");
    }

    /**
     * Set this system on pause
     *
     * <p>A paused system will not be executed.
     *
     * <p>A paused system can still accept, add and remove entities. The internal set will be
     * executed, when the system will run.
     */
    public void stop() {
        run = false;
        LOGGER.info(this.getClass().getName() + " was paused");
    }

    /**
     * @return true if this system is running, false if it is in pause mode
     */
    public boolean isRunning() {
        return run;
    }

    /**
     * Check if the given entity has all components that are needed to get processed by this system.
     *
     * <p>If an addition-component is missing, this system will create a log-entry with the
     * information of the missing component.
     *
     * @param entity the input argument
     * @return true if the entity is accepted, false if not.
     * @see DelayedSet
     */
    protected abstract boolean accept(Entity entity);

    /**
     * Use this Stream to iterate over all active entities for this system in the {@link
     * #execute}-Method.
     *
     * @return active entities that will be processed by the system as stream
     */
    protected Stream<Entity> getEntityStream() {
        return entities.stream();
    }

    /**
     * Util function to log that the given entity will not be processed by the calling system,
     * because the given component is missing.
     *
     * @param entity           Entity that will not be processed
     * @param missingComponent the component that is missing
     */
    protected void logMissingComponent(Entity entity, Class<? extends Component> missingComponent) {
        LOGGER.info(
            "Entity: "
                + entity
                + " Not processed by the "
                + getClass().getName()
                + " because the component "
                + missingComponent.getName()
                + " is missing ");
    }
}
