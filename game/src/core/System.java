package core;

import core.utils.DelayedSet;
import core.utils.logging.CustomLogLevel;

import java.util.function.Consumer;
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
 * {@link DelayedSet},
 *
 * <p>The {@link #update}-Method will first update the internal set and then will execute the
 * system-logic on each entity in the set.
 *
 * <p>The update-Method gets called every frame in the game loop from {@link Game}.
 *
 * <p>Systems are designed to be unique, so don't create two systems of the same type.
 */
public abstract class System {
    private final DelayedSet<Entity> entities;
    public Logger LOGGER = Logger.getLogger(this.getClass().getName());
    protected boolean run;

    public System() {
        Game.systems.add(this);
        entities = new DelayedSet<>();
        Game.getEntities().forEach(this::addEntity);
        run = true;
    }

    /** Update the entities-set of the system and executes the system functionality. */
    public void update() {
        entities.update();
        systemUpdate();
    }

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
     * @see Consumer
     * @see DelayedSet
     * @param entity entity to add
     * @return true if the entity is accepted, false if not.
     */
    public boolean addEntity(Entity entity) {
        if (accept(entity)) {
            if (entities.add(entity))
                LOGGER.log(
                        CustomLogLevel.INFO,
                        "Entity " + entity + " will be added to the " + getClass().getName());
            return true;
        }
        removeEntity(entity);
        return false;
    }

    /**
     * Remove the given entity to the {@link DelayedSet} of entities that will no longer be
     * processed by the system.
     *
     * @see DelayedSet
     * @param entity Entity to remove
     */
    public void removeEntity(Entity entity) {
        if (entities.remove(entity))
            LOGGER.log(
                    CustomLogLevel.INFO,
                    "Entity " + entity + " will be removed from to the " + getClass().getName());
    }

    /**
     * Remove all entities immediately from this system.
     *
     * <p>Will clear each internal list of {@link DelayedSet}
     *
     * <p>Do not call this function inside {@link #systemUpdate()} or you risc a {@link
     * java.util.ConcurrentModificationException}
     *
     * @see DelayedSet
     * @see java.util.ConcurrentModificationException
     */
    public void clearEntities() {
        entities.clear();
    }

    /**
     * Toggle this system between run and pause.
     *
     * <p>A paused system will not be updated.
     *
     * <p>A paused system can still accept, add and remove entities. The internal set will be
     * updated, when the system will run.
     *
     * <p>A running system will be updated.
     */
    public void toggleRun() {
        run = !run;
    }

    /**
     * Set this system on run.
     *
     * <p>A running system will be updated.
     */
    public void run() {
        run = true;
    }

    /**
     * Set this system on pause
     *
     * <p>A paused system will not be updated.
     *
     * <p>A paused system can still accept, add and remove entities. The internal set will be
     * updated, when the system will run.
     */
    public void stop() {
        run = false;
    }

    /**
     * @return true if this system is running, false if it is in pause mode
     */
    public boolean isRunning() {
        return run;
    }

    /** Implements the functionality of the system. */
    protected abstract void systemUpdate();

    /**
     * Check if the given entity has all components that are needed to get processed by this system.
     *
     * <p>If an addition-component is missing, this system will create a log-entry with the
     * information of the missing component.
     *
     * @see DelayedSet
     * @param entity the input argument
     * @return true if the entity is accepted, false if not.
     */
    protected abstract boolean accept(Entity entity);

    /**
     * Use this Stream to iterate over all active entities for this system in the {@link
     * #systemUpdate()}-Method.
     *
     * @return active entities that will be processed by the system as stream
     */
    protected Stream<Entity> getEntityStream() {
        return entities.getSetAsStream();
    }

    /**
     * Util function to log that the given entity will not be processed by the calling system,
     * because the given component is missing.
     *
     * @param entity Entity that will not be processed
     * @param missingComponent the component that is missing
     */
    protected void logMissingComponent(Entity entity, Class<? extends Component> missingComponent) {
        LOGGER.log(
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
