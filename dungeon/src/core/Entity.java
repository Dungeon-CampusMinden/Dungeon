package core;

import core.game.ECSManagment;
import core.utils.EntityIdProvider;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * An Entity is a container for {@link Component}s.
 *
 * <p>An entity needs to be registered with the {@link Game} via {@link Game#add}. Each Entity has a
 * unique id, which is automatically generated or can be explicitly set.
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
 * @see EntityIdProvider
 */
public final class Entity implements Comparable<Entity> {
  private static final Logger LOGGER = Logger.getLogger(Entity.class.getSimpleName());
  private final int id;
  private final HashMap<Class<? extends Component>, Component> components;
  private String name;

  private boolean persistent = false;

  /**
   * Create a new Entity with a generated id.
   *
   * <p>Remember to register it in {@link Game} using {@link Game#add}.
   *
   * @param name the name of the entity, used for better logging and debugging
   */
  public Entity(final String name) {
    this.id = EntityIdProvider.nextId();
    this.components = new HashMap<>();
    this.name = name;
    LOGGER.info(this + " was created.");
  }

  /**
   * Create a new Entity with a generated id. The name of the entity will be its id, prefixed by an
   * underscore.
   *
   * <p>Remember to register it in {@link Game} using {@link Game#add}.
   */
  public Entity() {
    this.id = EntityIdProvider.nextId();
    this.components = new HashMap<>();
    this.name = "_" + this.id;
    LOGGER.info(this + " was created.");
  }

  /**
   * Create a new Entity with an explicit id.
   *
   * <p>Will throw if the id is already in use.
   *
   * @param id the explicit id to use
   * @param name the name of the entity
   * @throws IllegalArgumentException if the id is already in use or invalid
   */
  public Entity(final int id, final String name) {
    EntityIdProvider.registerOrThrow(id);
    this.id = id;
    this.components = new HashMap<>();
    this.name = name;
    LOGGER.info(this + " was created.");
  }

  /**
   * Create a new Entity with an explicit id and default name of "_id".
   *
   * @param id the explicit id to use
   * @throws IllegalArgumentException if the id is already in use or invalid
   */
  public Entity(final int id) {
    this(id, "_" + id);
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
   * Removes a component of the specified class from this entity.
   *
   * <p>Changes to the entity's component map will trigger a call to {@link
   * ECSManagment#informAboutChanges}.
   *
   * @param klass the class of the component to remove
   * @return true if a component of the given class was removed; false otherwise (typically means
   *     the entity does not have this component)
   */
  public boolean remove(final Class<? extends Component> klass) {
    if (components.remove(klass) != null) {
      ECSManagment.informAboutChanges(this);
      LOGGER.info(klass.getName() + " from " + name + " was removed.");
      return true;
    }
    return false;
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
   * Get the name of this entity.
   *
   * @return the name of this entity
   */
  public String name() {
    return name;
  }

  /**
   * Sets whether this entity is persistent across level loads.
   *
   * <p>If an entity is persistent, it will be carried over to the next level when the current level
   * is loaded.
   *
   * @param persistent true to make the entity persistent across levels, false otherwise
   */
  public void persistent(boolean persistent) {
    this.persistent = persistent;
  }

  /**
   * Checks whether this entity is persistent across level loads.
   *
   * @return true if the entity is persistent and will move to the next level, false otherwise
   */
  public boolean isPersistent() {
    return this.persistent;
  }

  @Override
  public String toString() {
    return "Entity{" + "id=" + id + ", name='" + name + '\'' + '}';
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
