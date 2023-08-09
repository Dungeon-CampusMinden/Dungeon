package core;

import semanticanalysis.types.DSLContextPush;
import semanticanalysis.types.DSLType;

import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * An Entity is a container for {@link Component}s.
 *
 * <p>A new entity will automatically register itself with the {@link Game} via {@link
 * Game#addEntity} and will be added to the game on the next frame.
 *
 * <p>Add different components to an entity to define it. Based on the components inside an entity,
 * the {@link System}s will decide whether to process the entity.
 *
 * <p>Use {@link #addComponent} to add a Component to this entity. Normally, a component will add
 * itself to its associated entity, so you will not have to do it manually. Remember that an entity
 * can only store one component of each component class. For example, your entity can't have two
 * {@link core.components.DrawComponent}s.
 *
 * <p>If you want to remove a component from an entity, use {@link #removeComponent} and provide the
 * Class of the component you want to remove as a parameter.
 *
 * <p>With {@link #fetch}, you can check if the entity has a component of the given class.
 *
 * @see Component
 * @see System
 * @see Optional
 */
@DSLType(name = "entity")
@DSLContextPush(name = "entity")
public final class Entity implements Comparable<Entity> {
    private static final Logger LOGGER = Logger.getLogger(Entity.class.getName());
    private static int nextLocaleID = 0;
    /* ID that is unique on local game state. */
    private final int localID;
    /* ID that is unique on global/multiplayer game state. */
    private int globalID;
    private final String name;
    private final HashMap<Class<? extends Component>, Component> components;

    /**
     * Create a new Entity and register it in {@link Game} using {@link Game#addEntity}.
     *
     * @param name the name of the entity, used for better logging and debugging
     */
    public Entity(final String name) {
        globalID = nextLocaleID;
        localID = nextLocaleID++;
        components = new HashMap<>();
        this.name = name;
        Game.addEntity(this);
        LOGGER.info("The entity '" + name + "' was created.");
    }

    /**
     * Create a new Entity and register it in {@link Game} using {@link Game#addEntity}.
     *
     * <p>The name of the entity will be its id
     */
    public Entity() {
        this("_" + nextLocaleID);
    }

    /**
     * Create a new Entity.
     *
     * <p>NOTE: Created instance will not be added to {@link Game}. Used for multiplayer state
     * handling.
     *
     * @param name
     * @param localeID
     * @param globalID
     */
    public Entity(final String name, final int localeID, final int globalID) {
        components = new HashMap<>();
        this.name = name;
        this.localID = localeID;
        this.globalID = globalID;
    }

    /**
     * Add a new component to this entity.
     *
     * <p>Changes in the component map of the entity will trigger a call to {@link
     * Game#informAboutChanges}.
     *
     * <p>Normally, a component will add itself to its associated entity, so you will not have to do
     * it manually. Remember that an entity can only store one component of each component class.
     *
     * @param component The component to add
     */
    public void addComponent(final Component component) {
        components.put(component.getClass(), component);
        Game.informAboutChanges(this);
        //        LOGGER.info(component.getClass().getName() + " Components from " + this + " was
        // added.");
    }

    /**
     * Remove a component from this entity.
     *
     * <p>Changes in the component map of the entity will trigger a call to {@link
     * Game#informAboutChanges}.
     *
     * @param klass the Class of the component
     */
    public void removeComponent(final Class<? extends Component> klass) {
        if (components.remove(klass) != null) {
            Game.informAboutChanges(this);
            //            LOGGER.info(klass.getName() + " from " + name + " was removed.");
        }
    }

    /**
     * @return Components of the entity.
     */
    public HashMap<Class<? extends Component>, Component> components() {
        return this.components;
    }

    /**
     * Get the component
     *
     * @param klass Class of the component
     * @return Optional that can contain the requested component
     * @see Optional
     */
    public <T extends Component> Optional<T> fetch(final Class<T> klass) {
        return Optional.ofNullable(klass.cast(components.get(klass)));
    }

    /**
     * Check if the entity has a component of the given class
     *
     * @param klass class of the component to check for
     * @return true if the component is present in the entity, false if not
     */
    public boolean isPresent(final Class<? extends Component> klass) {
        return components.containsKey(klass);
    }

    /**
     * @return The id of this entity
     */
    public int id() {
        return localID;
    }

    @Override
    public String toString() {
        if (name.contains("_" + localID)) return name;
        else return name + "_" + localID;
    }

    @Override
    public int compareTo(Entity o) {
        return localID - o.localID;
    }

    /**
     * Get a stream of components associated with this entity.
     *
     * @return Stream of components.
     */
    public Stream<Component> componentStream() {
        return components.values().stream();
    }

    /**
     * @return Name.
     */
    public String name() {
        return name;
    }

    /**
     * @return Local ID.
     */
    public int localID() {
        return localID;
    }

    /**
     * @return Global ID.
     */
    public int globalID() {
        return globalID;
    }

    /**
     * Set global ID at runtime.
     *
     * <p>Used for multiplayer mode, to synchronize global IDs between local and global state.
     *
     * @param globalID To be set ID.
     */
    public void globalID(final int globalID) {
        this.globalID = globalID;
    }
}
