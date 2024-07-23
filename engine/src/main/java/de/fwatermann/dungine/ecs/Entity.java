package de.fwatermann.dungine.ecs;

import java.util.*;
import java.util.stream.Stream;

public abstract class Entity<T extends Entity<?>> {

  private final Map<Class<? extends Component>, List<Component>> components = new HashMap<>();

  public Entity() {}

  /**
   * Create a new entity with the given components.
   *
   * @param components the components to add
   */
  public Entity(Component... components) {
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
   * Get all components.
   *
   * @return a stream of components
   */
  public Stream<Component> components() {
    return this.components.values().stream().flatMap(List::stream);
  }

  /**
   * Get all components of a specific type.
   *
   * @param clazz the class of the component
   * @return a stream of components
   */
  public Stream<Component> components(Class<? extends Component> clazz) {
    return this.components.getOrDefault(clazz, Collections.emptyList()).stream();
  }

  /**
   * Add a component to this entity.
   * @param component the component to add
   * @return this entity
   */
  public T addComponent(Component component) {
    this.components
        .computeIfAbsent(
            component.getClass(), (Class<? extends Component> clazz) -> new ArrayList<>())
        .add(component);
    return (T) this;
  }

  /**
   * Remove a component from this entity.
   * @param component the component to remove
   * @return true if the component was removed, false if the component was not present
   */
  public boolean removeComponent(Component component) {
    return this.components
        .getOrDefault(component.getClass(), Collections.emptyList())
        .remove(component);
  }

  /**
   * Check if this entity has a specific component.
   * @param component the component to check
   * @return true if the component is present, false otherwise
   */
  public boolean hasComponent(Component component) {
    return this.components
        .getOrDefault(component.getClass(), Collections.emptyList())
        .contains(component);
  }
}
