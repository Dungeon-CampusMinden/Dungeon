package core;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * A System implements a specific game logic (a gameplay mechanic).
 *
 * <p>This class is the abstract base class for each system. It implements the basic functionality
 * each system has. For example, it allows the system to be paused and unpause.
 *
 * <p>A system will iterate over each {@link Entity} with specific {@link Component}s. The {@link
 * #accept} method checks if the entity has the needed components for the system.
 *
 * <p>If an entity has all the needed components for the system, the system will store the entity in
 * its local set.
 *
 * <p>The {@link #execute} method will execute the system logic on each entity in the set.
 *
 * <p>The execute method gets called every frame in the game loop from {@link Game#render}.
 *
 * <p>Systems are designed to be unique, so don't create two systems of the same type.
 *
 * @see Game
 * @see Entity
 * @see Component
 */
public abstract class System {
    private final Set<Entity> entities;
    protected static Logger LOGGER = Logger.getLogger("System");
    protected boolean run;

    /**
     * Create a new system and add it to the game. {@link Game#addSystem}
     *
     * <p>For each already existing entity in the game, check if the entity is accepted by {@link
     * #accept} and add it to the local set if so.
     */
    public System() {
        LOGGER.info("A new " + this.getClass().getName() + " was created");
        Game.addSystem(this);
        entities = new HashSet<>();
        Game.getEntitiesStream().forEach(this::showEntity);
        run = true;
    }

    /** Implements the functionality of the system. */
    public abstract void execute();

    /**
     * Check if the given entity has all the components needed to be processed by this system.
     *
     * <p>If the entity has all the components, it will be added to the internal set of this system.
     *
     * <p>If the key component is missing, this system will ignore the entity. If the entity is
     * present in the internal set, it will be removed.
     *
     * <p>If an addition component is missing, this system will ignore the entity and create a log
     * entry with information about the missing component. If the entity is present in the internal
     * set, it will be removed.
     *
     * @param entity the entity to add
     */
    public final void showEntity(Entity entity) {
        if (accept(entity)) addEntity(entity);
        else removeEntity(entity);
    }

    /**
     * Add the given entity from the local set so that it will be processed by the system.
     *
     * @param entity the entity to remove
     */
    private void addEntity(Entity entity) {
        if (entities.add(entity))
            LOGGER.info("Entity " + entity + "will be added to the" + getClass().getName());
    }

    /**
     * Remove the given entity from the local set so that it will no longer be processed by the
     * system.
     *
     * <p>Do not call this function inside {@link #execute} or you risk a {@link
     * java.util.ConcurrentModificationException}.
     *
     * @param entity the entity to remove
     */
    public final void removeEntity(Entity entity) {
        if (entities.remove(entity))
            LOGGER.info(
                    "Entity " + entity + " will be removed from to the " + getClass().getName());
    }

    /**
     * Remove all entities immediately from this system.
     *
     * <p>Do not call this function inside {@link #execute} or you risk a {@link
     * java.util.ConcurrentModificationException}.
     *
     * @see java.util.ConcurrentModificationException
     */
    public final void clearEntities() {
        LOGGER.info("All entities from " + this.getClass().getName() + " were removed");
        entities.clear();
    }

    /**
     * Toggle this system between running and paused states.
     *
     * <p>A paused system will not be executed.
     *
     * <p>A paused system can still accept, add, and remove entities. The internal set will be
     * processed when the system is running.
     *
     * <p>A running system will be executed.
     */
    public void toggleRun() {
        if (run) stop();
        else run();
    }

    /**
     * Set this system to the running state.
     *
     * <p>A running system will be executed.
     */
    public void run() {
        run = true;
        LOGGER.info(this.getClass().getName() + " will run");
    }

    /**
     * Set this system to the paused state.
     *
     * <p>A paused system will not be executed.
     *
     * <p>A paused system can still accept, add, and remove entities. The internal set will be
     * processed when the system is running.
     */
    public void stop() {
        run = false;
        LOGGER.info(this.getClass().getName() + " was paused");
    }

    /**
     * @return true if this system is running, false if it is in paused mode
     */
    public final boolean isRunning() {
        return run;
    }

    /**
     * Check if the given entity has all the components needed to be processed by this system.
     *
     * <p>If an addition component is missing, this system will create a log entry with information
     * about the missing component.
     *
     * @param entity the entity to check
     * @return true if the entity is accepted, false if not.
     */
    protected abstract boolean accept(Entity entity);

    /**
     * Use this Stream to iterate over all active entities for this system in the {@link #execute}
     * method.
     *
     * @return a stream of active entities that will be processed by the system
     */
    protected final Stream<Entity> getEntityStream() {
        return entities.stream();
    }

    /**
     * Utility function to log that the given entity will not be processed by the calling system
     * because the given component is missing.
     *
     * @param entity the entity that will not be processed
     * @param missingComponent the missing component
     */
    protected final void logMissingComponent(
            Entity entity, Class<? extends Component> missingComponent) {
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
