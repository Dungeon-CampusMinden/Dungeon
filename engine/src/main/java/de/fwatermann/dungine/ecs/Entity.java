package de.fwatermann.dungine.ecs;

import java.util.*;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * The `Entity` class represents an entity in the ECS (Entity Component System) framework.
 * It holds components and provides methods to manage them, as well as methods to handle the entity's position, size, and rotation.
 */
public class Entity {

  private static final Logger LOGGER = LogManager.getLogger(Entity.class);

  private final Vector3f position;
  private final Vector3f size;
  private final Quaternionf rotation;

  private final Map<Class<? extends Component>, List<Component>> components = new HashMap<>();

  /**
   * Constructs a new `Entity` with the specified position, rotation, and scaling.
   *
   * @param position the position of the entity
   * @param rotation the rotation of the entity
   * @param scaling the scaling of the entity
   */
  public Entity(Vector3f position, Quaternionf rotation, Vector3f scaling) {
    this.position = position;
    this.rotation = rotation;
    this.size = scaling;
  }

  /**
   * Constructs a new `Entity` with default position, rotation, and scaling.
   */
  public Entity() {
    this(new Vector3f(), new Quaternionf(), new Vector3f(1.0f));
  }

  /**
   * Constructs a new `Entity` with the given components.
   *
   * @param components the components to add
   */
  public Entity(Component... components) {
    this();
    Arrays.stream(components)
        .forEach(
            c -> {
              this.components
                  .computeIfAbsent(
                      c.getClass(), (Class<? extends Component> clazz) -> new ArrayList<>())
                  .add(c);
            });
  }

  /**
   * Returns a stream of all components in this entity.
   *
   * @return a stream of components
   */
  public Stream<Component> components() {
    return this.components.values().stream().flatMap(List::stream);
  }

  /**
   * Returns a stream of all components of a specific type in this entity.
   *
   * @param clazz the class of the component
   * @param <T> the type of the component
   * @return a stream of components
   */
  public <T extends Component> Stream<T> components(Class<T> clazz) {
    return (Stream<T>) this.components.getOrDefault(clazz, Collections.emptyList()).stream();
  }

  /**
   * Adds a component to this entity.
   *
   * @param component the component to add
   * @return this entity
   */
  public Entity addComponent(Component component) {
    List<Component> list =
        this.components.computeIfAbsent(
            component.getClass(), (Class<? extends Component> clazz) -> new ArrayList<>());

    Entity oE = component.entity();
    if(oE != null && oE != this) {
      LOGGER.warn("Component {} was already added to entity {}! Removing it from previous entity!", component, oE);
      oE.removeComponent(component);
    }

    if (!component.canHaveMultiple()) {
      if (list.isEmpty()) {
        list.add(component);
      } else {
        list.set(0, component);
      }
    } else {
      list.add(component);
    }
    component.entity(this);
    return this;
  }

  /**
   * Returns the first component of a specific type in this entity.
   *
   * @param clazz the class of the component
   * @param <T> the type of the component
   * @return an optional containing the component if present, otherwise empty
   */
  public <T extends Component> Optional<T> component(Class<T> clazz) {
    List<T> comps = (List<T>) this.components.get(clazz);
    if(comps == null) return Optional.empty();
    return Optional.ofNullable(comps.isEmpty() ? null : comps.getFirst());
  }

  /**
   * Removes a component from this entity.
   *
   * @param component the component to remove
   * @return true if the component was removed, false if the component was not present
   */
  public boolean removeComponent(Component component) {
    if(component.entity() != this) {
      LOGGER.warn("Component {} is not part of entity {}!", component, this);
      return false;
    } else {
      component.entity(null);
    }
    return this.components
        .getOrDefault(component.getClass(), Collections.emptyList())
        .remove(component);
  }

  /**
   * Checks if this entity has a specific component.
   *
   * @param component the component to check
   * @return true if the component is present, false otherwise
   */
  public boolean hasComponents(Component component) {
    return this.components
        .getOrDefault(component.getClass(), Collections.emptyList())
        .contains(component);
  }

  /**
   * Checks if this entity has specific components of specific classes.
   *
   * @param componentClass the classes of the components to check
   * @return true if the components are present, false otherwise
   */
  @SafeVarargs
  public final boolean hasComponents(Class<? extends Component>... componentClass) {
    return Arrays.stream(componentClass).allMatch(this.components::containsKey);
  }

  /**
   * Checks if this entity has specific components of specific classes.
   *
   * @param componentClasses the classes of the components to check
   * @return true if the components are present, false otherwise
   */
  public final boolean hasComponents(Set<Class<? extends Component>> componentClasses) {
    return componentClasses.stream().allMatch(this.components::containsKey);
  }

  /**
   * Returns the position of this entity.
   *
   * @return the position of this entity
   */
  public Vector3f position() {
    return this.position;
  }

  /**
   * Sets the position of this entity.
   *
   * @param position the new position
   * @return this entity
   */
  public Entity position(Vector3f position) {
    this.position.set(position);
    return this;
  }

  /**
   * Returns the size of this entity.
   *
   * @return the size of this entity
   */
  public Vector3f size() {
    return this.size;
  }

  /**
   * Sets the size of this entity.
   *
   * @param size the new size
   * @return this entity
   */
  public Entity size(Vector3f size) {
    this.size.set(size);
    return this;
  }

  /**
   * Returns the rotation of this entity.
   *
   * @return the rotation of this entity
   */
  public Quaternionf rotation() {
    return this.rotation;
  }

  /**
   * Sets the rotation of this entity.
   *
   * @param rotation the new rotation
   * @return this entity
   */
  public Entity rotation(Quaternionf rotation) {
    this.rotation.set(rotation);
    return this;
  }
}
