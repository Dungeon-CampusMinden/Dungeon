package core;

import semanticAnalysis.types.DSLContextPush;
import semanticAnalysis.types.DSLType;

import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

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
 * <p>With {@link #getComponent}, you can check if the entity has a component of the given class.
 *
 * @see Component
 * @see System
 * @see Optional
 */
@DSLType(name = "game_object")
@DSLContextPush(name = "entity")
public final class Entity {
    private static final Logger LOGGER = Logger.getLogger(Entity.class.getName());
    private static int nextId = 0;
    private final int id;
    private final String name;
    private final HashMap<Class<? extends Component>, Component> components;

    /**
     * Create a new Entity and register it in {@link Game} using {@link Game#addEntity}.
     *
     * @param name the name of the entity, used for better logging and debugging
     */
    public Entity(String name) {
        id = nextId++;
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
        this("_" + nextId);
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
    public void addComponent(Component component) {
        components.put(component.getClass(), component);
        LOGGER.info(
                component.getClass().getName()
                        + " Components from "
                        + this.toString()
                        + " was added.");
        Game.informAboutChanges(this);
    }

    /**
     * Remove a component from this entity.
     *
     * <p>Changes in the component map of the entity will trigger a call to {@link
     * Game#informAboutChanges}.
     *
     * @param klass the Class of the component
     */
    public void removeComponent(Class<? extends Component> klass) {
        if (components.remove(klass) != null) {
            LOGGER.info(klass.getName() + " from " + name + " was removed.");
            Game.informAboutChanges(this);
        }
    }

    /**
     * Get the component
     *
     * @param klass Class of the component
     * @return Optional that can contain the requested component
     * @see Optional
     */
    public Optional<Component> getComponent(Class<? extends Component> klass) {
        return Optional.ofNullable(components.get(klass));
    }

    /**
     * @return The id of this entity
     */
    public int id() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
