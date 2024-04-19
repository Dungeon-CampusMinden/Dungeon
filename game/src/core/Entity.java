package core;

import core.game.ECSManagment;
import dsl.annotation.DSLContextPush;
import dsl.annotation.DSLType;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * An Entity is a container for {@link Component}s.
 *
 * <p>An entity needs to be registered with the {@link Game} via {@link Game#add}.
 *
 * <p>Add different components to an entity to define it. Based on the components inside an entity,
 * the {@link System}s will decide whether to process the entity.
 *
 * <p>Use {@link #add} to add a Component to this entity. Remember that an entity can only store one
 * component of each component class. For example, your entity can't have two {@link
 * core.components.DrawComponent}s.
 *
 * <p>If you want to remove a component from an entity, use {@link #remove} and provide the Class of
 * the component you want to remove as a parameter.
 *
 * <p>Use {@link #fetch(Class)} to retrieve the component of the given class in this entity.
 *
 * <p>With {@link #isPresent(Class)}, you can check if the entity has a component of the given
 * class.
 *
 * @see Component
 * @see System
 */
@DSLType(name = "entity")
@DSLContextPush(name = "entity")
public final class Entity implements Comparable<Entity> {
  private static final Logger LOGGER = Logger.getLogger(Entity.class.getSimpleName());
  private static int nextId = 0;
  private final int id;
  private final HashMap<Class<? extends Component>, Component> components;
  private String name;

  /**
   * Create a new Entity.
   *
   * <p>Remember to register it in {@link Game} using {@link Game#add}.
   *
   * @param name the name of the entity, used for better logging and debugging
   */
  public Entity(final String name) {
    id = nextId++;
    components = new HashMap<>();
    this.name = name;
    LOGGER.info("The entity '" + name + "' was created.");
  }

  /**
   * Create a new Entity.
   *
   * <p>Remember to register it in {@link Game} using {@link Game#add}.
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
   * ECSManagment#informAboutChanges}.
   *
   * <p>Remember that an entity can only store one component of each component class.
   *
   * @param component The component to add
   */
  public void add(final Component component) {
    components.put(component.getClass(), component);
    ECSManagment.informAboutChanges(this);
    LOGGER.info(component.getClass().getName() + " Components from " + this + " was added.");
  }

  /**
   * Remove a component from this entity.
   *
   * <p>Changes in the component map of the entity will trigger a call to {@link
   * ECSManagment#informAboutChanges}.
   *
   * @param klass the Class of the component
   */
  public void remove(final Class<? extends Component> klass) {
    if (components.remove(klass) != null) {
      ECSManagment.informAboutChanges(this);
      LOGGER.info(klass.getName() + " from " + name + " was removed.");
    }
  }

  /**
   * Get the component.
   *
   * @param klass Class of the component.
   * @param <T> The type of the (given and returned) component.
   * @return Optional that can contain the requested component, is empty if this entity does not
   *     store a component of the given class.
   * @see Optional
   */
  public <T extends Component> Optional<T> fetch(final Class<T> klass) {
    return Optional.ofNullable(klass.cast(components.get(klass)));
  }

  /**
   * Check if the entity has a component of the given class.
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
    return id;
  }

  /**
   * Set the name of this entity.
   *
   * @param name the new name of this entity
   */
  public void name(String name) {
    this.name = name;
  }

  /**
   * Get the name of this entity
   *
   * @return the name of this entity
   */
  public String name() {
    return name;
  }

  @Override
  public String toString() {
    if (name.contains("_" + id)) return name;
    else return name + "_" + id;
  }

  @Override
  public int compareTo(Entity o) {
    return id - o.id;
  }

  /**
   * Get a stream of components associated with this entity.
   *
   * @return Stream of components.
   */
  public Stream<Component> componentStream() {
    return components.values().stream();
  }
}
