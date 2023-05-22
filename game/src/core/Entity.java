package core;

import semanticAnalysis.types.DSLContextPush;
import semanticAnalysis.types.DSLType;

import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Entity is a unique identifier for an object in the game world
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
     * Create a new Entity and register it in {@link Game}.
     *
     * @param name name of the entity, use it for better logging and debugging
     */
    public Entity(String name) {
        id = nextId++;
        components = new HashMap<>();
        this.name = name;
        Game.addEntity(this);
        LOGGER.info("The entity '" + name + "' was created.");
    }

    /**
     * Create a new Entity and register it in {@link Game}.
     *
     * <p>The name of the entity, will be the id.
     */
    public Entity() {
        this("" + nextId);
    }

    /**
     * Add a new component to this entity
     *
     * @param component The component
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
     * Remove a component from this entity
     *
     * @param klass Class of the component
     */
    public void removeComponent(Class<? extends Component> klass) {
        components.remove(klass);
        LOGGER.info(klass.getName() + " from " + name + " was removed.");
        Game.informAboutChanges(this);
    }

    /**
     * Get the component
     *
     * @param klass Class of the component
     * @return Optional that can contain the requested component
     */
    public Optional<Component> getComponent(Class<? extends Component> klass) {
        return Optional.ofNullable(components.get(klass));
    }

    /**
     * @return The id of this Entity
     */
    public int id() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
