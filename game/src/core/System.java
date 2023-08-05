package core;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * A System implements a specific game logic (a gameplay mechanic).
 *
 * <p>A System needs to be registered with the Game via {@link Game#addSystem(System)}.
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
    protected static Logger LOGGER = Logger.getLogger(System.class.getName());
    private final Set<Class<? extends Component>> filterRules;
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
     * Create a new system.
     *
     * <p>A System needs to be registered with the Game via {@link Game#addSystem(System)}.
     *
     * <p>For each already existing entity in the game, check if the entity is accepted by {@link
     * #accept} and add it to the local set if so.
     *
     * @param keyComponent The Class of the key-component for the system. Each entity without this
     *     component will be ignored.
     * @param filterRules Additional needed Component-Classes. Entities with the key component but
     *     without all additional components will not be processed by this system.
     */
    public System(Class<? extends Component>... filterRules) {
        if (filterRules != null) this.filterRules = Set.of(filterRules);
        else this.filterRules = new HashSet<>();
        run = true;
        LOGGER.info("A new " + this.getClass().getName() + " was created");
    }

    /** Implements the functionality of the system. */
    public abstract void execute();

    public void triggerOnAdd(Entity entity) {
        onEntityAdd.accept(entity);
    }

    public void triggerOnRemove(Entity entity) {
        onEntityRemove.accept(entity);
    }

    public Set<Class<? extends Component>> filterRules() {
        return new HashSet<>(filterRules);
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
     * Use this Stream to iterate over all active entities for this system in the {@link #execute}
     * method.
     *
     * @return a stream of active entities that will be processed by the system
     */
    public final Stream<Entity> entityStream() {
        return Game.entityStream(this);
    }
}
