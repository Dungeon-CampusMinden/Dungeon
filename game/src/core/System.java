package core;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
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
    protected static Logger LOGGER = Logger.getLogger("System");
    private final Set<Entity> entities;
    private final Class<? extends Component> keyComponent;
    private final Set<Class<? extends Component>> additionalComponents;
    protected boolean run;

    /**
     * Will be called after an entity was added to the internal set of the system.
     *
     * <p>Use this in your own system to implement logic that should be executed after an entity was
     * added.
     *
     * <p>The default implementation is just empty.
     */
    protected Consumer<Entity> onEntityAdd = (e) -> {};
    /**
     * Will be called after an entity was removed from the internal set of the system.
     *
     * <p>Use this in your own system to implement logic that should be executed after an entity was
     * removed.
     *
     * <p>The default implementation is just empty.
     */
    protected Consumer<Entity> onEntityRemove = (e) -> {};

    /**
     * Will be called if an entity is shown to the system.
     *
     * <p>Use this in your own system to implement logic that should be executed after an entity is
     * shown to the system.
     *
     * <p>This function will be called after the entity is added/removed to/from the set. It is the
     * final call in {@link #showEntity(Entity)}. So {@link #onEntityAdd} or {@link #onEntityRemove}
     * could have been executed already.
     *
     * <p>The default implementation is just empty.
     */
    protected Consumer<Entity> onEntityShow = (e) -> {};

    /**
     * Create a new system and add it to the game. {@link Game#addSystem}
     *
     * <p>For each already existing entity in the game, check if the entity is accepted by {@link
     * #accept} and add it to the local set if so.
     *
     * @param keyComponent The Class of the key-component for the system. Each entity without this
     *     component will be ignored.
     * @param additionalComponents Additional needed Component-Classes. Entities with the key
     *     component but without all additional components will not be processed by this system.
     */
    public System(
            Class<? extends Component> keyComponent,
            Class<? extends Component>... additionalComponents) {
        this.keyComponent = keyComponent;
        if (additionalComponents != null) this.additionalComponents = Set.of(additionalComponents);
        else this.additionalComponents = new HashSet<>();
        entities = new HashSet<>();
        Game.addSystem(this);
        Game.entityStream().forEach(this::showEntity);
        run = true;
        LOGGER.info("A new " + this.getClass().getName() + " was created");
    }

    /**
     * Create a new system and add it to the game. {@link Game#addSystem}
     *
     * <p>For each already existing entity in the game, check if the entity is accepted by {@link
     * #accept} and add it to the local set if so.
     *
     * @param keyComponent The Class of the key-component for the system. Each entity without this
     *     component will be ignored.
     */
    public System(Class<? extends Component> keyComponent) {
        this(keyComponent, null);
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
     * <p>If one or more additionally component are missing, this system will ignore the entity and
     * create a log entry with information about the missing components. If the entity is present in
     * the internal set, it will be removed.
     *
     * @param entity the entity to add
     */
    public final void showEntity(Entity entity) {
        if (accept(entity)) addEntity(entity);
        else removeEntity(entity);
        onEntityShow.accept(entity);
    }

    /**
     * Add the given entity from the local set so that it will be processed by the system.
     *
     * @param entity the entity to remove
     */
    private void addEntity(Entity entity) {
        if (entities.add(entity)) {
            onEntityAdd.accept(entity);
            LOGGER.info("Entity " + entity + "will be added to " + getClass().getName());
        }
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
        if (entities.remove(entity)) {
            onEntityRemove.accept(entity);
            LOGGER.info("Entity " + entity + " will be removed from " + getClass().getName());
        }
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
        new HashSet<>(entities).forEach(this::removeEntity);
        LOGGER.info("All entities from " + this.getClass().getName() + " were removed");
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
        if (!run) LOGGER.info(this.getClass().getName() + " is running");
        run = true;
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
        if (run) LOGGER.info(this.getClass().getName() + " is paused");
        run = false;
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
     * <p>If one or more additionally components are missing, this system will create a log entry
     * with information about the missing components.
     *
     * @param entity the entity to check
     * @return true if the entity is accepted, false if not.
     */
    protected boolean accept(Entity entity) {
        if (entity.isPresent(keyComponent)) {
            for (Class<? extends Component> klass : additionalComponents)
                if (!entity.isPresent(klass)) {
                    // will log also other missing components, so we can stop here
                    logMissingComponent(entity);
                    return false;
                }
            return true;
        }
        return false;
    }

    /**
     * Use this Stream to iterate over all active entities for this system in the {@link #execute}
     * method.
     *
     * @return a stream of active entities that will be processed by the system
     */
    public final Stream<Entity> entityStream() {
        return entities.stream();
    }

    /**
     * Utility function to log that the given entity will not be processed by the calling system
     * because the additional component is missing.
     *
     * @param entity the entity that will not be processed
     */
    protected void logMissingComponent(Entity entity) {
        StringBuilder info =
                new StringBuilder(
                        "Entity: "
                                + entity
                                + " Not processed by the "
                                + getClass().getName()
                                + " because following Components are missing: ");

        for (Class<? extends Component> klass : additionalComponents) {
            if (!entity.isPresent(klass)) info.append(klass.getName());
        }
        LOGGER.info(info.toString());
    }
}
